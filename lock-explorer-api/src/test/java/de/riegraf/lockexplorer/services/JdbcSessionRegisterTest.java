package de.riegraf.lockexplorer.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
class JdbcSessionRegisterTest {

  @Autowired
  JdbcSessionRegister jdbcSessionRegister;

  @Test
  public void dropAllTest(){
    jdbcSessionRegister.dropAllUsers();
  }

  @Test
  public void getConnectionsAfterRegistrationTest() throws SQLException {
    final String userId = jdbcSessionRegister.registerUserFakeImpl();
    assertThat(jdbcSessionRegister.getSessions(userId), hasSize(1));
  }

  @Test
  public void getConnectionsWithMultipleOpenConnections() throws SQLException {
    final String userId = jdbcSessionRegister.registerUserFakeImpl();
    final int sessionNr = jdbcSessionRegister.newSession(userId).get();
    assertThat(jdbcSessionRegister.getSessions(userId), hasItem(sessionNr));
  }

}