package de.riegraf.lockexplorer.services;

import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcSessionRegisterTest {

  @Mock
  private OracleDataSource oracleDataSource;

  @Mock
  Connection connection;

  @Mock
  ResultSet resultSetMock;

  @Mock
  Statement statementMock;

  @MockBean
  private JdbcTemplate jdbcTemplate;

  @MockBean
  private UserIdGenerator idGeneratorMock;

  @Autowired
  JdbcSessionRegister sessionRegister;

  @BeforeAll
  public void setup() {
    sessionRegister.setDatasource(oracleDataSource);
  }

  @Test
  public void registerUser() throws SQLException {
    String userId = "userId";
    Integer sid = 4;

    given(idGeneratorMock.generateId()).willReturn(userId);
    given(oracleDataSource.getConnection()).willReturn(connection);
    given(connection.createStatement()).willReturn(statementMock);
    given(statementMock.executeQuery(anyString())).willReturn(resultSetMock);
    given(resultSetMock.next()).willReturn(true);
    given(resultSetMock.getInt(anyInt())).willReturn(sid);

    assertThat(sessionRegister.registerUser(), equalTo(userId));

    final List<Integer> sessions = sessionRegister.getSessions(userId);
    assertThat(sessions, hasSize(1));
    assertThat(sessions, hasItem(sid));
  }


}