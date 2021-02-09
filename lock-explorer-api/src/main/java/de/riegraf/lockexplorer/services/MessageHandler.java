package de.riegraf.lockexplorer.services;

import de.riegraf.lockexplorer.models.Message;
import de.riegraf.lockexplorer.utils.KeyValueTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

@Service
public class MessageHandler {

  Logger logger = LoggerFactory.getLogger(JdbcSessionRegister.class);


  @Autowired
  JdbcSessionRegister sessionRegister;

  @Autowired
  SqlExecutor sqlExecutor;

  @Autowired
  JdbcTemplate systemConnection;

  public KeyValueTuple handle(Message message) throws Exception {

    switch (message.getType()) {

      case REGISTER:
        return register();
      case GET_SESSIONS:
        return getSessions(message);
      case OPEN_SESSION:
        return openSession(message);
      case CLOSE_SESSION:
        return closeSession(message);
      case EXECUTE_SQL:
        return executeSql(message);
      case GET_TABLE:
        return getTable(message);
      case GET_AVAILABLE_VIEWS:
        return getAvailableViews(message);
      case GET_VIEW:
        return getView(message);
      case GET_AVAILABLE_TABLES:
        return getAvailableTables(message);
      default:
        throw new IllegalStateException("Unexpected value: " + message.getType());

    }
  }

  private KeyValueTuple getSessions(Message message) {
    final String userId = message.getUser().orElseThrow(() -> new NoSuchElementException("No such user"));
    return new KeyValueTuple("sessions", sessionRegister.getSessions(userId));
  }

  private KeyValueTuple getAvailableViews(Message message) {
    return new KeyValueTuple("availableViews", Stream.of("v$lock", "v$locked_object", "v$session", "V$LOCKED_TABLES")
        .map(String::toUpperCase)
        .collect(Collectors.toList()));
  }

  private KeyValueTuple getAvailableTables(Message message) {
    final String userId = message.getUser().orElseThrow(() -> new NoSuchElementException("No such user")).toUpperCase();
    final List<String> tables = systemConnection.query(
        format("SELECT table_name FROM all_tables WHERE owner = '%s'", userId),
        (rs, rowNum) -> rs.getString("table_name")
    );
    logger.info("Return available tables for user {}: {}", userId, tables);
    return new KeyValueTuple("availableTables", tables);
  }

  private KeyValueTuple getView(Message message) throws SQLException {
    final String viewName = (String) message.getPayloadValue("viewName");
    final String userId = message.getUser().orElseThrow(() -> new NoSuchElementException("No such user."));
    final String sql;
    if (viewName.equalsIgnoreCase("V$LOCK")) {
      sql = format("SELECT * FROM V$LOCK WHERE sid IN (%s)", sessionRegister.getSessions(userId).stream()
          .map(String::valueOf)
          .collect(Collectors.joining(", ")));
    } else if (viewName.equalsIgnoreCase("V$SESSION")) {
      sql = format("SELECT * FROM V$SESSION WHERE username = '%S'", userId);
    } else if (viewName.equalsIgnoreCase("V$LOCKED_OBJECT")) {
      sql = format("SELECT * FROM V$LOCKED_OBJECT WHERE oracle_username = '%S'", message.getUser().orElseThrow());

    } else if (viewName.equalsIgnoreCase("V$LOCKED_TABLES")) {
      sql = format("SELECT c.object_name, c.object_type, b.sid, b.serial#, b.status " +
          "FROM v$locked_object a, v$session b, all_objects c " +
          "WHERE b.sid = a.session_id " +
          "AND b.sid IN (%s) " +
          "AND a.object_id = c.object_id", sessionRegister.getSessions(userId).stream()
          .map(String::valueOf)
          .collect(Collectors.joining(", ")));
    } else {
      sql = "SELECT * FROM " + viewName;
    }

    final Map<String, Object> map = sqlExecutor.executeSql(
        systemConnection.getDataSource().getConnection(), sql);
    return new KeyValueTuple("viewData", map);
  }

  private KeyValueTuple getTable(Message message) throws SQLException {
    final String tableName = (String) message.getPayloadValue("tableName");
    final String user = message.getUser().orElseThrow(() -> new NoSuchElementException("No such user"));
    final Connection connection = sessionRegister.getConnection(user, (int) message.getPayloadValue("sessionNr"));
    final Map<String, Object> map = sqlExecutor.executeSql(connection, format("SELECT ROWID, A.* FROM %s A", tableName));
    map.put("lockedRows", sqlExecutor.getLockedRows(format("%s.%s", user, tableName), systemConnection.getDataSource().getConnection()));
    return new KeyValueTuple("tableData", map);
  }


  private KeyValueTuple executeSql(Message message) throws Exception {
    try {
      String userId = message.getUser().orElseThrow();
      int sessionNr = (Integer) message.getPayloadValue("sessionNr");
      String sql = (String) message.getPayloadValue("sql");
      Connection conn = sessionRegister.getConnection(userId, sessionNr);
      return new KeyValueTuple("result", sqlExecutor.executeSql(conn, sql));
    } catch (Exception e) {
      logger.error("{}", e);
      throw e;
    }
  }

  private KeyValueTuple closeSession(Message message) throws Exception {
    String userId = message.getUser().orElseThrow();
    int sessionNr = (int) message.getPayloadValue("sessionNr");
    sessionRegister.closeSession(userId, sessionNr);
    return new KeyValueTuple("message", "Session closed.");
  }

  private KeyValueTuple openSession(Message message) throws SQLException {
    return new KeyValueTuple(
        "sessionNr",
        sessionRegister.newSession(message.getUser().orElseThrow(() -> new NoSuchElementException("No such user.")))
    );
  }

  private KeyValueTuple register() throws SQLException {
    return new KeyValueTuple("userId", sessionRegister.registerUser());
  }
}
