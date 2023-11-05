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

    config.setMaximumPoolSize(200);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");


//    // Optional configuration settings (you can adjust these as needed)
//    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
//    config.setMaximumPoolSize(19); // Set the maximum number of connections in the pool
//    config.setAutoCommit(true); // Set auto-commit behavior

    // Create the data source from the configuration
//    try (HikariDataSource dataSource = new HikariDataSource(config)) {
//      // Get a connection from the pool and use it
//      try (Connection connection = dataSource.getConnection()) {
//        // Do something with the connection, for example, a simple query
//        System.out.println("Successfully connected to the database.");
//        // You can add your database operations here
//      }
//    } catch (SQLException e) {
//      e.printStackTrace();
//      System.out.println("Database connection failed");
//    }
    return new HikariDataSource(config);
  }
}