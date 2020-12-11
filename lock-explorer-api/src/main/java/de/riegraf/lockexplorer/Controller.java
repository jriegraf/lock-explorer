package de.riegraf.lockexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Controller {

  Logger logger = LoggerFactory.getLogger(Controller.class);

  @Autowired
  JdbcTemplate jdbcTemplate;

  public List<String> runSql(String sql) {
    try {
      return jdbcTemplate.query(sql, (resultSet, i) ->
      {
        final int columnCount = resultSet.getMetaData().getColumnCount();
        List<String> list = new ArrayList(columnCount);
        for (int j = 1; j <= columnCount; j++) {
          list.add(resultSet.getString(j));
        }
        return String.join(" | ", list);
      });
    } catch (DataAccessException ex){
      return Arrays.asList(ex.getMessage());

    }
  }


  @GetMapping(value = "/sql/{sql}", produces = MediaType.TEXT_HTML_VALUE)
  String hello(@PathVariable String sql) {
    logger.info("sql is {}", sql);
    return String.format("<pre>%s</pre>", String.join("<br>", runSql(sql)));
  }
}