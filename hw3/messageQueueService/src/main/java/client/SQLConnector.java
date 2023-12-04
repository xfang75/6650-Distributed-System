package client;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLConnector {
  private static final String JDBC_URL = "jdbc:mysql://fx-hw2-mysql.ctzmuqimajwz.us-east-1.rds.amazonaws.com:3306/AlbumLibrary";
  private static final String USER = "admin";
  private static final String PASSWORD = "fg123456";

  public static HikariDataSource createDataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(JDBC_URL);
    config.setUsername(USER);
    config.setPassword(PASSWORD);

    // MySQL optimization used in part5
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
    config.addDataSourceProperty("useLocalSessionState", "true");
    config.addDataSourceProperty("rewriteBatchedStatements", "true");
    config.addDataSourceProperty("cacheResultSetMetadata", "true");
    config.addDataSourceProperty("cacheServerConfiguration", "true");
    config.addDataSourceProperty("elideSetAutoCommits", "true");
    config.addDataSourceProperty("maintainTimeStats", "false");

// Setting connection pool properties
    config.setMaximumPoolSize(100);
    config.setMinimumIdle(5);
    config.setIdleTimeout(300000);
    config.setConnectionTimeout(30000);
    config.setConnectionTestQuery("SELECT 1");
    config.setMaxLifetime(600000);
    config.setAutoCommit(true);

    return new HikariDataSource(config);
  }
}