package de.riegraf.lockexplorer.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class SqlExecutor {

  Logger logger = LoggerFactory.getLogger(SqlExecutor.class);

  public Map<String, Object> executeSql(Connection connection, String sql) throws SQLException {
    logger.debug("Execute SQL: {}", sql);
    try (Statement statement = connection.createStatement()) {
      boolean isResultSet = statement.execute(sql);
      if (isResultSet) {
        return convertResultSetToMap(statement.getResultSet());
      } else {
        Map<String, Object> resultMap = new HashMap<>(1);
        resultMap.put("updated", statement.getUpdateCount());
        return resultMap;
      }
    }
  }

  private Map<String, Object> convertResultSetToMap(ResultSet resultSet) throws SQLException {
    final ResultSetMetaData metaData = resultSet.getMetaData();
    final List<Map<String, String>> columnInfo = getColumnInfo(metaData);
    final List<Map<String, String>> data = getDataFromResultSet(resultSet, columnInfo);
    final Map<String, Object> retVal = new HashMap<>(2);
    retVal.put("columnInfo", columnInfo);
    retVal.put("data", data);
    return retVal;
  }

  private List<Map<String, String>> getDataFromResultSet(ResultSet resultSet, List<Map<String, String>> columnInfo) throws SQLException {
    final int columnCount = columnInfo.size();
    final List<Map<String, String>> data = new ArrayList<>();
    while (resultSet.next()) {
      final HashMap<String, String> entry = new HashMap<>(columnCount);
      for (int i = 1; i <= columnCount; i++) {
        String colName = columnInfo.get(i - 1).get("columnName");
        entry.put(colName, resultSet.getString(colName));
      }
      data.add(entry);
    }
    return data;
  }

  private List<Map<String, String>> getColumnInfo(ResultSetMetaData metaData) throws SQLException {
    final int columnCount = metaData.getColumnCount();
    final List<Map<String, String>> columnInfo = new ArrayList<>(columnCount);

    for (int i = 1; i <= columnCount; i++) {
      Map<String, String> map = new HashMap<>(2);
      map.put("columnName", metaData.getColumnName(i));
      map.put("columnType", metaData.getColumnTypeName(i));
      columnInfo.add(map);
    }
    return columnInfo;
  }

  public List<String> getLockedRows(String fullTableName, Connection connection) {

    final List<Map<String, String>> columnInfo = new ArrayList<>(1);
    columnInfo.add(new HashMap<>());
    columnInfo.get(0).put("columnName", "ROWID");
    columnInfo.get(0).put("columnType", "ROWID");

    final List<String> freeRows;
    final List<String> allRows;
    try {
      try (Statement stmt = connection.createStatement()) {
        allRows = getDataFromResultSet(stmt.executeQuery(format("SELECT ROWID FROM %s", fullTableName)), columnInfo)
            .stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
      }

      try (Statement stmt = connection.createStatement()) {
        freeRows = getDataFromResultSet(stmt.executeQuery(format("SELECT ROWID FROM %s FOR UPDATE SKIP LOCKED", fullTableName)), columnInfo)
            .stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
      }
      final List<String> lockedRows = allRows.stream().filter(o -> !freeRows.contains(o)).collect(Collectors.toList());
      logger.debug("Found those locked rows in {}: {}", fullTableName, lockedRows.stream().collect(Collectors.joining(", ", "{", "}")));
      return lockedRows;
    } catch (Exception e) {
      logger.debug("Exception while fetching locked rows: ", e);
      return new ArrayList<>(0);
    }
  }
}
