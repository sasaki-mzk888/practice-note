package com.example.practicenote.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** Reads the PostgreSQL URL written by Neon CLI without logging its credentials. */
@Configuration
@Profile("postgres")
public class NeonDataSourceConfiguration {

    @Bean
    public DataSource dataSource(Environment environment) {
        String databaseUrl = stripQuotes(environment.getRequiredProperty("DATABASE_URL").trim());

        try {
            URI uri = URI.create(databaseUrl);
            if (!"postgresql".equals(uri.getScheme()) && !"postgres".equals(uri.getScheme())) {
                throw new IllegalArgumentException("Unsupported database URL scheme");
            }
            if (uri.getHost() == null || uri.getRawUserInfo() == null || uri.getRawPath() == null) {
                throw new IllegalArgumentException("Incomplete database URL");
            }

            String[] credentials = uri.getRawUserInfo().split(":", 2);
            if (credentials.length != 2) {
                throw new IllegalArgumentException("Database URL must include a username and password");
            }

            String authority = uri.getRawAuthority();
            String jdbcUrl = "jdbc:postgresql://" + authority.substring(authority.lastIndexOf('@') + 1)
                    + uri.getRawPath()
                    + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());

            return DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .driverClassName("org.postgresql.Driver")
                    .url(jdbcUrl)
                    .username(decode(credentials[0]))
                    .password(decode(credentials[1]))
                    .build();
        } catch (RuntimeException exception) {
            // Do not include the URL in the error message because it contains credentials.
            throw new IllegalStateException(
                    "DATABASE_URL is not a valid Neon PostgreSQL connection string.");
        }
    }

    private String decode(String value) {
        // In a URI user-info segment, '+' is literal; URLDecoder would otherwise turn it into a space.
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
