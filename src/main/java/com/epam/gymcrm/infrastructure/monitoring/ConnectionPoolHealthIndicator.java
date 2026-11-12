package com.epam.gymcrm.infrastructure.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Health check of the HikariCP connection pool. The pool is down when every connection is busy
 * and callers are waiting for one. Shown in /actuator/health as "connectionPool".
 */
@Component
public class ConnectionPoolHealthIndicator extends AbstractHealthIndicator {

    private final HikariDataSource dataSource;

    public ConnectionPoolHealthIndicator(HikariDataSource dataSource) {
        super("Connection pool health check failed");
        this.dataSource = dataSource;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        if (pool == null) {
            builder.unknown().withDetail("reason", "the pool is not started yet");
            return;
        }
        int active = pool.getActiveConnections();
        int idle = pool.getIdleConnections();
        int total = pool.getTotalConnections();
        int waiting = pool.getThreadsAwaitingConnection();
        int max = dataSource.getMaximumPoolSize();

        boolean exhausted = active >= max && waiting > 0;
        (exhausted ? builder.down() : builder.up())
                .withDetail("active", active)
                .withDetail("idle", idle)
                .withDetail("total", total)
                .withDetail("max", max)
                .withDetail("waiting", waiting);
    }
}
