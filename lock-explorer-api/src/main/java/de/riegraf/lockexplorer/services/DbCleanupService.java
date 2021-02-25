package de.riegraf.lockexplorer.services;


import de.riegraf.lockexplorer.services.SessionRegistry.SessionRegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DbCleanupService {
  private static final Logger logger = LoggerFactory.getLogger(DbCleanupService.class);

  final JdbcTemplate systemDatasource;
  final SessionRegisterService sessionRegister;

 public DbCleanupService(JdbcTemplate systemDatasource,
                          @Qualifier("SessionRegistryProxy") SessionRegisterService sessionRegister) {
    this.systemDatasource = systemDatasource;
    this.sessionRegister = sessionRegister;
  }

  // schedule every 10 min
  @Scheduled(fixedRate = 1000 * 60 * 10)
  public void dropInactiveUsers() {
    int amount = sessionRegister.dropExpiredUsers();
    logger.info("Cleaned {} users.", amount);
  }
}