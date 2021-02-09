package de.riegraf.lockexplorer.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

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