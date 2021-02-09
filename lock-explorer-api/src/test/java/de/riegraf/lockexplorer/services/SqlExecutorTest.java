package de.riegraf.lockexplorer.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SqlExecutorTest {

  @Mock
  Connection connection;

  @Mock
  ResultSet allTableResultSet;

  @Mock
  ResultSet freeTableResultSet;

  @Mock
  Statement statementMock;

  @Autowired
  SqlExecutor sqlExecutor;

  @Test
  public void thereAreNoLockedRows() throws SQLException {
    final String tableName = "tableA";
    final String[] rows = {"A", "B"};

    when(connection.createStatement()).thenReturn(statementMock);
    when(statementMock.executeQuery(anyString())).thenReturn(allTableResultSet).thenReturn(freeTableResultSet);
    when(allTableResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(allTableResultSet.getString(anyString())).thenReturn(rows[0]).thenReturn(rows[1]);

    // all rows are free
    when(freeTableResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(freeTableResultSet.getString(anyString())).thenReturn(rows[0]).thenReturn(rows[1]);

    final List<String> lockedRows = sqlExecutor.getLockedRows(tableName, connection);
    assertThat(lockedRows, hasSize(0));
  }

  @Test
  public void allRowsAreLocked() throws SQLException {
    final String tableName = "tableA";
    final String[] rows = {"A", "B"};

    when(connection.createStatement()).thenReturn(statementMock).thenReturn(statementMock);
    when(statementMock.executeQuery(anyString())).thenReturn(allTableResultSet).thenReturn(freeTableResultSet);
    when(allTableResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(allTableResultSet.getString(anyString())).thenReturn(rows[0]).thenReturn(rows[1]);

    // no free rows
    when(freeTableResultSet.next()).thenReturn(false);

    final List<String> lockedRows = sqlExecutor.getLockedRows(tableName, connection);

    // all two rows are locked
    assertThat(lockedRows, hasSize(2));
    assertThat(lockedRows, hasItem(rows[0]));
    assertThat(lockedRows, hasItem(rows[1]));
  }

  @Test
  public void someRowsAreLocked() throws SQLException {
    final String tableName = "tableA";
    final String[] rows = {"A", "B", "C"};

    given(connection.createStatement()).willReturn(statementMock);
    given(statementMock.executeQuery(anyString())).willReturn(allTableResultSet).willReturn(freeTableResultSet);
    given(allTableResultSet.next()).willReturn(true).willReturn(true).willReturn(true).willReturn(false);
    given(allTableResultSet.getString(anyString())).willReturn(rows[0]).willReturn(rows[1]).willReturn(rows[2]);

    // B is a free row
    given(freeTableResultSet.next()).willReturn(true).willReturn(false);
    given(freeTableResultSet.getString(anyString())).willReturn(rows[1]);


    final List<String> lockedRows = sqlExecutor.getLockedRows(tableName, connection);
    assertThat(lockedRows, hasSize(2));
    assertThat(lockedRows, hasItem(rows[0]));
    assertThat(lockedRows, hasItem(rows[2]));
  }



}