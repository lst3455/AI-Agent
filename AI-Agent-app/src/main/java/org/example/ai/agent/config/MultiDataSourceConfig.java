package org.example.ai.agent.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MultiDataSourceConfig {

    // ────────────────────────────────────────────────────────────
    // 2.1 Bind MySQL properties under spring.datasource.mysql.*
    // ────────────────────────────────────────────────────────────
    @Bean
    @Primary   // Mark this one as the "default" DataSource
    @ConfigurationProperties("spring.datasource.mysql")  // Binds to spring.datasource.mysql.url, username, etc. :contentReference[oaicite:33]{index=33}
    public DataSourceProperties mysqlDataSourceProperties() {
        // DataSourceProperties will pick up: url, username, password, driver-class-name, hikari.* under spring.datasource.mysql. :contentReference[oaicite:34]{index=34}
        return new DataSourceProperties();
    }

    @Bean
    @Primary   // Mark this DataSource as primary (for @Autowired DataSource)
    @ConfigurationProperties("spring.datasource.mysql.hikari")  // Binds Hikari-specific properties under spring.datasource.mysql.hikari.* :contentReference[oaicite:35]{index=35}
    public DataSource mysqlDataSource(
            @Qualifier("mysqlDataSourceProperties") DataSourceProperties mysqlProps) {
        // Initialize a HikariDataSource using the bound MySQL properties
        return mysqlProps.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ────────────────────────────────────────────────────────────
    // 2.2 Bind Postgres properties under spring.datasource.postgres.*
    // ────────────────────────────────────────────────────────────
    @Bean
    @ConfigurationProperties("spring.datasource.postgres")  // Binds to spring.datasource.postgres.url, username, etc. :contentReference[oaicite:36]{index=36}
    public DataSourceProperties postgresDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.postgres.hikari")  // Binds Hikari-specific properties under spring.datasource.postgres.hikari.* :contentReference[oaicite:37]{index=37}
    public DataSource postgresDataSource(
            @Qualifier("postgresDataSourceProperties") DataSourceProperties postgresProps) {
        // Initialize a HikariDataSource using the bound Postgres properties
        return postgresProps.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // ────────────────────────────────────────────────────────────
    // 2.3 Optionally, expose two JdbcTemplate beans (one per DataSource)
    // ────────────────────────────────────────────────────────────
    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(
            @Qualifier("mysqlDataSource") DataSource mysqlDs) {
        return new JdbcTemplate(mysqlDs);
    }

    @Bean(name = "postgresJdbcTemplate")
    public JdbcTemplate postgresJdbcTemplate(
            @Qualifier("postgresDataSource") DataSource postgresDs) {
        return new JdbcTemplate(postgresDs);
    }
}
