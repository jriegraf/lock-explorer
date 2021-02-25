package de.riegraf.lockexplorer.services.SessionRegistry;

import de.riegraf.lockexplorer.models.UserData;
import de.riegraf.lockexplorer.services.UserIdGenerator;
import oracle.jdbc.pool.OracleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
class JdbcSessionRegister implements SessionRegisterService {

  // Bean dependencies
  final UserIdGenerator idGenerator;
  final JdbcTemplate jdbcTemplate;

  // Inject value from environment variable
  @Value("${DATABASE_URL}")
  private String url;

  private static final int USER_EXPIRE_DURATION = 120;
  private static final Logger logger = LoggerFactory.getLogger(JdbcSessionRegister.class);
  private final List<UserData> DB = new ArrayList<>();
  private OracleDataSource dataSource = new OracleDataSource();

  @Autowired
  public JdbcSessionRegister(UserIdGenerator idGenerator, JdbcTemplate jdbcTemplate) throws SQLException {
    this.idGenerator = idGenerator;
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * For testing purposes: inject database mock here.
   */
  public void setDatasource(OracleDataSource ds) {
    dataSource = ds;
  }

  @Override
  public Optional<Integer> newSession(String userId) throws SQLException {
    Optional<UserData> userOption = DB.stream().filter(d -> d.getUserId().equals(userId)).findFirst();
    if (userOption.isEmpty()) {
      return Optional.empty();
    }
    UserData user = userOption.get();

    dataSource.setURL(format("jdbc:oracle:thin:%s/%s@%s", userId, user.getPassword(), url));
    java.util.Properties props = new java.util.Properties();
    props.put("v$session.osuser", userId);
    props.put("v$session.program", "LockExplorer");
    dataSource.setConnectionProperties(props);
    Connection conn = dataSource.getConnection();
    conn.setAutoCommit(false);
    Optional<Integer> sessionId = Optional.empty();
    try (ResultSet resultSet = conn.createStatement().executeQuery("SELECT sys_context('USERENV','SID') FROM DUAL")) {
      if (resultSet.next()) {
        final int sid = resultSet.getInt(1);
        if (sid != 0) {
          sessionId = Optional.of(sid);
        }
      }
    }
    user.getConnections().put(sessionId.orElseThrow(() -> new NoSuchElementException("Could not get a SID")), conn);

    logger.info("Created session {} for user {}", sessionId.get(), user.getUserId());
    return sessionId;
  }

  @Override
  public void disconnectSessions(String userId) {
    final UserData userData = DB.stream().filter(d -> d.getUserId().equals(userId)).findFirst().orElseThrow();
    userData.getConnections().values().forEach(connection -> {
      try {
        connection.close();
      } catch (SQLException throwables) {
        logger.error("{}", throwables);
      }
    });
  }

  @Override
  public String registerUser() throws SQLException {
    final UserData newUser = new UserData(idGenerator.generateId(), idGenerator.generateId());
    DB.add(newUser);
    logger.info("Added user {}. Current users: {}", newUser.getUserId(), DB.stream().map(UserData::getUserId).collect(Collectors.joining(", ")));

    jdbcTemplate.execute(format("CREATE USER %s IDENTIFIED BY %s", newUser.getUserId(), newUser.getPassword()));
    jdbcTemplate.execute(format("GRANT CONNECT, CREATE TABLE, CREATE SEQUENCE TO %s", newUser.getUserId()));
    jdbcTemplate.execute(format("alter user %s quota 10M on users", newUser.getUserId()));

    newSession(newUser.getUserId()).get();
    return newUser.getUserId();
  }

  @Override
  public void closeSession(String userId, int sessionNr) {
    UserData userData = DB.stream().filter(u -> u.getUserId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No such user."));

    try {
      Optional.ofNullable(userData.getConnections().remove(sessionNr))
          .orElseThrow(() -> new NoSuchElementException("No such connection"))
          .close();
      logger.info("Closed connection {} of user {}.", sessionNr, userId);
    } catch (SQLException e) {
      logger.error("Closing session failed: {}", e);
    }
  }

  @Override
  public Connection getConnection(String userId, int sessionNr) {
    return Optional.ofNullable(
        DB.stream().filter(userData -> userData.getUserId().equalsIgnoreCase(userId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No such user."))
            .getConnections()
            .get(sessionNr)
    ).orElseThrow(() -> new NoSuchElementException("No such connection."));
  }

  @Override
  public List<Integer> getSessions(String userId) {
    logger.info("Get sessions for user {}.", userId);
    return new ArrayList<>(
        DB.stream().filter(userData -> userData.getUserId().equalsIgnoreCase(userId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No such user."))
            .getConnections()
            .keySet());
  }

  @Override
  public Optional<Connection> getJdbcConnection(String userId, int sessionNr) {
    return DB.stream().filter(d -> d.getUserId().equals(userId))
        .map(d -> d.getConnections().get(sessionNr))
        .findFirst();
  }

  @Override
  public void setLastActionTime(String userId) {
    final UserData user = DB.stream().filter(d -> d.getUserId().equalsIgnoreCase(userId)).findFirst()
        .orElseThrow(() -> new NoSuchElementException("Can not set last action because userId is not valid: " + userId));
    final LocalDateTime now = LocalDateTime.now();
    logger.info("Set last action of user {} to {}", userId, now);
    user.setLastAction(now);
  }

  /**
   * Deletes all users whose last action is further back than the expiration time.
   * @return the number of dropped users
   */
  @Override
  public int dropExpiredUsers() {
    LocalDateTime now = LocalDateTime.now();
    final List<UserData> toBeRemoved = DB.stream().
        filter(u -> u.getLastAction().isBefore(now.minusMinutes(USER_EXPIRE_DURATION))).
        peek(u -> getSessions(u.getUserId()).forEach(sessionNr -> closeSession(u.getUserId(), sessionNr))).
        peek(u -> dropUser(u.getUserId())).
        collect(Collectors.toList());

    DB.removeAll(toBeRemoved);
    return toBeRemoved.size();
  }

  /**
   * Drop user and all it's data from the database.
   * @param userId the database name of the user
   */
  private void dropUser(String userId) {
    final String dropSql = format("DROP USER %s CASCADE", userId.toUpperCase());
    logger.debug(dropSql);
    jdbcTemplate.execute(dropSql);
    logger.info("Dropped user {}.", userId);
  }

}
