package de.riegraf.lockexplorer.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;


@Configuration
@Component
public class DataSourceBean {

  @Value("${DATABASE_PASSWORD}")
  private String password;

  @Value("${DATABASE_USERNAME}")
  private String username;

  @Value("${DATABASE_URL}")
  private String url;


  @Bean
  @Primary
  public DataSource dataSource() {
    return DataSourceBuilder
        .create()
        .username(username)
        .password(password)
        .url("jdbc:oracle:thin:@" + url)
        .build();
  }
}
