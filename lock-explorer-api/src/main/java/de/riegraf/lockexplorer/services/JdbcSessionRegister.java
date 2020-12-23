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
import java.sql.SQLException;
import java.util.*;

@Service
public class JdbcSessionRegister {

  Logger logger = LoggerFactory.getLogger(JdbcSessionRegister.class);

  List<UserData> DB = new ArrayList<>();

  @Autowired
  UserIdGenerator idGenerator;

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Value("${database.host}")
  String host;

  @Value("${database.container}")
  String container;

  public Optional<Integer> newSession(String userId) throws SQLException {
    Optional<UserData> userOption = DB.stream().filter(d -> d.getUserId().equals(userId)).findFirst();
    if (userOption.isEmpty()) {
      return Optional.empty();
    }
    UserData user = userOption.get();
    Integer sessionId = user.sessionCount++;

    OracleDataSource ds = new OracleDataSource();
    ds.setURL(String.format("jdbc:oracle:thin:%s/%s@%s/%s", userId, user.getPassword(), host, container));
    java.util.Properties props = new java.util.Properties();
    props.put("v$session.osuser", userId);
    props.put("v$session.program", sessionId.toString());
    ds.setConnectionProperties(props);
    Connection conn = ds.getConnection();
    user.getConnections().put(sessionId, conn);
    return Optional.of(sessionId);
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

    jdbcTemplate.execute(String.format("CREATE USER %s IDENTIFIED BY %s", newUser.getUserId(), newUser.getPassword()));
    jdbcTemplate.execute(String.format("GRANT CONNECT TO %s", newUser.getUserId()));
    jdbcTemplate.execute(String.format("alter user %s quota 10M on users", newUser.getUserId()));

    newSession(newUser.getUserId()).get();
    return newUser.getUserId();
  }

  public int dropAllUsers() {
    String sql = "SELECT username FROM all_users WHERE oracle_maintained = 'N' and username not in ('PDBADMIN', 'HR', 'JULIAN')";
    final List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
    maps.stream().map(Map::values)
        .flatMap(Collection::stream)
        .peek(s -> logger.debug("Drop user {}", s))
        .forEach(user -> jdbcTemplate.execute("DROP USER " + user));
    return maps.size();


  }

  public void closeSession(String userId, int sessionNr) throws SQLException {
    DB.stream().filter(u -> u.getUserId().equals(userId))
        .findFirst()
        .map(UserData::getConnections)
        .map(connections -> connections.get(sessionNr))
        .orElseThrow(() -> new NoSuchElementException("No such connection"))
        .close();
  }

  private Optional<Object> toOption(Object o) {
    return Optional.ofNullable(o);
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

  @RequiredArgsConstructor
  @Getter
  static class UserData {
    final String userId;
    final String password;
    int sessionCount = 0;
    final Map<Integer, Connection> connections = new HashMap<>(2);
  }

  public Optional<Connection> getJdbcConnection(String userId, int sessionNr) {
    return DB.stream().filter(d -> d.getUserId().equals(userId))
        .map(d -> d.getConnections().get(sessionNr))
        .findFirst();
  }
}
