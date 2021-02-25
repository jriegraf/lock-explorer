package de.riegraf.lockexplorer.services.SessionRegistry;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("SessionRegistryProxy")
public class SessionRegistryProxy implements SessionRegisterService {

  final JdbcSessionRegister sessionRegister;

  public SessionRegistryProxy(JdbcSessionRegister sessionRegister) {
    this.sessionRegister = sessionRegister;
  }

  @Override
  public Optional<Integer> newSession(String userId) throws SQLException {
    sessionRegister.setLastActionTime(userId);
    return sessionRegister.newSession(userId);
  }

  @Override
  public void disconnectSessions(String userId) {
    sessionRegister.setLastActionTime(userId);
    sessionRegister.disconnectSessions(userId);
  }

  @Override
  public String registerUser() throws SQLException {
    return sessionRegister.registerUser();
  }

  @Override
  public void closeSession(String userId, int sessionNr) throws SQLException {
    sessionRegister.setLastActionTime(userId);
    sessionRegister.closeSession(userId, sessionNr);
  }

  @Override
  public Connection getConnection(String userId, int sessionNr) {
    sessionRegister.setLastActionTime(userId);
    return sessionRegister.getConnection(userId, sessionNr);
  }

  @Override
  public List<Integer> getSessions(String userId) {
    sessionRegister.setLastActionTime(userId);
    return sessionRegister.getSessions(userId);
  }

  @Override
  public Optional<Connection> getJdbcConnection(String userId, int sessionNr) {
    sessionRegister.setLastActionTime(userId);
    return sessionRegister.getJdbcConnection(userId, sessionNr);
  }

  @Override
  public void setLastActionTime(String userId) {
    sessionRegister.setLastActionTime(userId);
  }

  @Override
  public int dropExpiredUsers() {
    return sessionRegister.dropExpiredUsers();
  }
}
