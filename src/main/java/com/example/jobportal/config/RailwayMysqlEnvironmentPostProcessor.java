package com.example.jobportal.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Railway provides {@code MYSQL_URL} as {@code mysql://user:pass@host:port/db}.
 * Spring DataSource expects {@code jdbc:mysql://host:port/db} plus separate credentials.
 * <p>
 * If you set {@code SPRING_DATASOURCE_URL} yourself (full JDBC URL), this processor does nothing.
 * Otherwise, when {@code MYSQL_URL} is present, it maps to {@code spring.datasource.*}.
 */
public class RailwayMysqlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE = "railwayMysqlUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String explicitJdbc = environment.getProperty("SPRING_DATASOURCE_URL");
        if (explicitJdbc != null && !explicitJdbc.isBlank()) {
            return;
        }
        String mysqlUrl = environment.getProperty("MYSQL_URL");
        if (mysqlUrl == null || mysqlUrl.isBlank()) {
            return;
        }
        mysqlUrl = mysqlUrl.trim();
        if (!mysqlUrl.startsWith("mysql://")) {
            return;
        }
        try {
            URI uri = URI.create(mysqlUrl.replaceFirst("mysql://", "http://"));
            String userInfo = uri.getUserInfo();
            String user = null;
            String password = "";
            if (userInfo != null) {
                int idx = userInfo.indexOf(':');
                if (idx >= 0) {
                    user = urlDecode(userInfo.substring(0, idx));
                    password = urlDecode(userInfo.substring(idx + 1));
                } else {
                    user = urlDecode(userInfo);
                }
            }
            String host = uri.getHost();
            if (host == null) {
                throw new IllegalArgumentException("MYSQL_URL has no host");
            }
            int port = uri.getPort() > 0 ? uri.getPort() : 3306;
            String path = uri.getPath();
            if (path == null || path.length() <= 1) {
                throw new IllegalArgumentException("MYSQL_URL must include /databaseName in the path");
            }
            String database = path.startsWith("/") ? path.substring(1) : path;
            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

            Map<String, Object> map = new HashMap<>();
            map.put("spring.datasource.url", jdbcUrl);
            if (user != null) {
                map.put("spring.datasource.username", user);
            }
            map.put("spring.datasource.password", password);
            environment.getPropertySources().addFirst(new MapPropertySource(SOURCE, map));
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Invalid MYSQL_URL. Expected mysql://user:password@host:port/database — see docs/RAILWAY.md", e);
        }
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
