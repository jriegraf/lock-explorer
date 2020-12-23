package de.riegraf.lockexplorer.controllers;

import de.riegraf.lockexplorer.models.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
class SystemController {

  Logger logger = LoggerFactory.getLogger(SystemController.class);

  @Autowired
  JdbcTemplate jdbcTemplate;


//  @GetMapping(value = "/getView/{view}", produces = MediaType.APPLICATION_JSON_VALUE)
//  ResponseEntity<Response> getTables(@PathVariable String viewName) {
//    try {
//      final String sql = "SELECT table_name FROM all_tables WHERE owner = user";
//      Map<String, Object> result = jdbcTemplate.query(sql, this::convertResultSetToMap);
//      return Response.ok(result);
//    } catch (DataAccessException e) {
//      return Response.createError(HttpStatus.BAD_REQUEST, e.getMessage());
//    }
//  }
//
//  @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
//  ResponseEntity<String> getHealth() {
//    return ResponseEntity.ok("{ \"Status\": \"Alive\" }");
//  }
}