package de.riegraf.lockexplorer.controllers;

import de.riegraf.lockexplorer.models.Message;
import de.riegraf.lockexplorer.models.Response;
import de.riegraf.lockexplorer.services.MessageHandler;
import de.riegraf.lockexplorer.services.SessionRegistry.SessionRegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  final JdbcTemplate jdbcTemplate;
  final SessionRegisterService sessionRegister;
  final MessageHandler messageHandler;

  public UserController(@Qualifier("SessionRegistryProxy") SessionRegisterService sessionRegister,
                        JdbcTemplate jdbcTemplate, MessageHandler messageHandler) {
    this.jdbcTemplate = jdbcTemplate;
    this.sessionRegister = sessionRegister;
    this.messageHandler = messageHandler;
  }

  @PostMapping(value = "/message", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<Response> receiveMessage(@RequestBody Message message) {
    try {
      logger.info("Got message: {}", message);
      return Response.ok(messageHandler.handle(message));
    } catch (Exception e) {
      HttpStatus status = e instanceof NoSuchElementException || e instanceof SQLException ?
          HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
      logger.debug("{}", e);
      logger.info("Respond with status {}. Message was: {}", status, e.getMessage());
      return Response.createError(status, e.getMessage());
    }
  }
}
