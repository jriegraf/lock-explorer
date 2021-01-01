package de.riegraf.lockexplorer.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class JdbcSessionRegister {

  private Logger logger = LoggerFactory.getLogger(JdbcSessionRegister.class);
  private List<UserData> DB = new ArrayList<>();
  private OracleDataSource dataSource = new OracleDataSource();

  @Autowired
  UserIdGenerator idGenerator;

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Value("${DATABASE_URL}")
  private String url;

  public JdbcSessionRegister() throws SQLException {
  }

  public void setDatasource(OracleDataSource ds){
    dataSource = ds;
  }

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

  public String registerUserFakeImpl() throws SQLException {

    final String userId = "OwhZG";
    final String userPassword = "OwhZG";
    final Optional<UserData> userDataOptional = DB.stream().filter(u -> u.getUserId().equals(userId)).findFirst();
    final UserData newUser;
    if (userDataOptional.isEmpty()) {
      newUser = new UserData(userId, userPassword);
      DB.add(newUser);
      logger.info("Added user {}. Current users: {}", newUser.getUserId(), DB.stream().map(UserData::getUserId).collect(Collectors.joining(", ")));

      try {
        jdbcTemplate.execute(format("DROP USER %s CASCADE", newUser.getUserId()));
      } catch (Exception exception) {
        logger.error("{}", exception);
      }
      try {
        jdbcTemplate.execute(format("CREATE USER %s IDENTIFIED BY %s", newUser.getUserId(), newUser.getPassword()));
        jdbcTemplate.execute(format("GRANT CONNECT, CREATE TABLE, CREATE SEQUENCE TO %s", newUser.getUserId()));
        jdbcTemplate.execute(format("alter user %s quota 10M on users", newUser.getUserId()));
        newSession(newUser.getUserId()).get();
      } catch (SQLException e) {
        logger.warn("FakeImpl: " + e.getMessage());
      }
    } else {
      newUser = userDataOptional.get();
    }
    return newUser.getUserId();
  }

  public int dropAllUsers() {
    String sql = "SELECT username FROM all_users WHERE oracle_maintained = 'N' and username not in ('PDBADMIN', 'HR', 'JULIAN')";
    final List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
    final int amount = (int) maps.stream().map(Map::values)
        .flatMap(Collection::stream)
        .peek(user -> {
          final String dropSql = format("DROP USER %s CASCADE", user);
          logger.debug(dropSql);
          jdbcTemplate.execute(dropSql);
        })
        .count();
    logger.info("Dropped {} users.", amount);
    return amount;


  }

  public void closeSession(String userId, int sessionNr) throws SQLException {
    UserData userData = DB.stream().filter(u -> u.getUserId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No such user."));

    Optional.ofNullable(userData.getConnections().remove(sessionNr))
        .orElseThrow(() -> new NoSuchElementException("No such connection"))
        .close();
  }

  public Connection getConnection(String userId, int sessionNr) {
    return Optional.ofNullable(
        DB.stream().filter(userData -> userData.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No such user."))
            .getConnections()
            .get(sessionNr)
    ).orElseThrow(() -> new NoSuchElementException("No such connection."));
  }

  public List<Integer> getSessions(String userId) {
    logger.info("Get sessions for user {}.", userId);
    return new ArrayList<>(
        DB.stream().filter(userData -> userData.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No such user."))
            .getConnections()
            .keySet());
  }

  @RequiredArgsConstructor
  @Getter
  static class UserData {
    final String userId;
    final String password;
    final Map<Integer, Connection> connections = new HashMap<>(2);
  }

  public Optional<Connection> getJdbcConnection(String userId, int sessionNr) {
    return DB.stream().filter(d -> d.getUserId().equals(userId))
        .map(d -> d.getConnections().get(sessionNr))
        .findFirst();
  }
}
