package com.example.avisos.Conexiones;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPOOL {

    private static final String servidor = "localhost";
    private static final String usuario = "root";
    private static final String password = "taliTakumi514";
    private static final String puerto = "3306";
    private static final String db = "admin";

    private static final String url = "jdbc:mariadb://" + servidor + ":" + puerto + "/" + db;

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {

        config.setJdbcUrl(url);
        config.setUsername(usuario);
        config.setPassword(password);
        config.setMinimumIdle(10);
        config.setMaximumPoolSize(50);
        config.setMaxLifetime(60000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);

    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

}
