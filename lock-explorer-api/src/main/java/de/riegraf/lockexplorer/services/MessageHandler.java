package de.riegraf.lockexplorer.services;

import de.riegraf.lockexplorer.models.Message;
import de.riegraf.lockexplorer.utils.KeyValueTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MessageHandler {

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
      case OPEN_SESSION:
        return openSession(message);
      case CLOSE_SESSION:
        return closeSession(message);
      case EXECUTE_SQL:
        return executeSql(message);
      case GET_SID:
        return getSid(message);
      case GET_TABLE:
        break;
      case GET_VIEW:
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + message.getType());


    }
    return new KeyValueTuple("message", message.toString());
  }

  private KeyValueTuple getSid(Message message) {
    final Integer sid = systemConnection.query(
        conn -> conn.prepareStatement("SELECT SID FROM V$SESSION WHERE osuser = ? AND program = ?"),
        ps -> {
          ps.setString(1, message.getUser().orElseThrow(() -> new NoSuchElementException("No such user.")));
          ps.setString(2, String.valueOf((int) message.getPayloadValue("sessionNr")));
        }, rs -> {
          rs.next();
          return rs.getInt(1);
        }
    );
    return new KeyValueTuple("sid", sid);
  }

  private KeyValueTuple executeSql(Message message) throws Exception {
    String userId = message.getUser().orElseThrow();
    int sessionNr = (Integer) message.getPayloadValue("sessionNr");
    String sql = (String) message.getPayloadValue("sql");
    Connection conn = sessionRegister.getConnection(userId, sessionNr);
    return new KeyValueTuple("result", sqlExecutor.executeSql(conn, sql));
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
