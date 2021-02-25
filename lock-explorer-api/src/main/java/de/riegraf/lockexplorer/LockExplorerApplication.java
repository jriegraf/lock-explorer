package de.riegraf.lockexplorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LockExplorerApplication {

  public static void main(String[] args) {
    SpringApplication.run(LockExplorerApplication.class, args);
  }

}
