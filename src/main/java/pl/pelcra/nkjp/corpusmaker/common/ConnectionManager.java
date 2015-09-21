package pl.pelcra.nkjp.corpusmaker.common;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.configuration.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public enum ConnectionManager {
    INSTANCE;
    private static DataSource dataSource;

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        if (dataSource == null) {
            dataSource = setupDataSource(ConfigProvider.CONNECTION_PROPS);
        }
        return dataSource.getConnection();
    }

    private static DataSource setupDataSource(Configuration connectionProps) {
        String connectURI = connectionProps.getString("jdbcURL")
              + "?autoReconnect=true" + "&rewriteBatchedStatements=true";
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(connectionProps.getString("jdbcDriver"));
        ds.setUsername(connectionProps.getString("jdbcUser"));
        ds.setPassword(connectionProps.getString("jdbcPassword"));
        ds.setJdbcUrl(connectURI);
        return ds;
    }
}
