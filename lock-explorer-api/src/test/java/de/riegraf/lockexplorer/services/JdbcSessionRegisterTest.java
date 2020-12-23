package de.riegraf.lockexplorer.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JdbcSessionRegisterTest {

  @Autowired
  JdbcSessionRegister jdbcSessionRegister;

  @Test
  public void dropAllTest(){
    jdbcSessionRegister.dropAllUsers();
  }

}