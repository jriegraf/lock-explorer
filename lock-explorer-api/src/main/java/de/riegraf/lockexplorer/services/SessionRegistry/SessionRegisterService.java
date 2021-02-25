package de.riegraf.lockexplorer.services.SessionRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SessionRegisterService {

  Optional<Integer> newSession(String userId) throws SQLException;

  void disconnectSessions(String userId);

  String registerUser() throws SQLException;

  void closeSession(String userId, int sessionNr) throws SQLException;

  Connection getConnection(String userId, int sessionNr);

  List<Integer> getSessions(String userId);

  Optional<Connection> getJdbcConnection(String userId, int sessionNr);

  void setLastActionTime(String userId);

  int dropExpiredUsers();
}
