package com.epam.gymcrm.infrastructure.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionPoolHealthIndicatorTest {

    @Mock
    private HikariDataSource dataSource;
    @Mock
    private HikariPoolMXBean pool;
    @InjectMocks
    private ConnectionPoolHealthIndicator indicator;

    private void poolState(int active, int idle, int total, int waiting, int max) {
        when(dataSource.getHikariPoolMXBean()).thenReturn(pool);
        when(dataSource.getMaximumPoolSize()).thenReturn(max);
        when(pool.getActiveConnections()).thenReturn(active);
        when(pool.getIdleConnections()).thenReturn(idle);
        when(pool.getTotalConnections()).thenReturn(total);
        when(pool.getThreadsAwaitingConnection()).thenReturn(waiting);
    }

    @Test
    void isUpAndReportsThePoolState() {
        poolState(2, 1, 3, 0, 5);

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(2, health.getDetails().get("active"));
        assertEquals(1, health.getDetails().get("idle"));
        assertEquals(3, health.getDetails().get("total"));
        assertEquals(5, health.getDetails().get("max"));
        assertEquals(0, health.getDetails().get("waiting"));
    }

    @Test
    void isUpWhenEveryConnectionIsBusyButNobodyWaits() {
        poolState(5, 0, 5, 0, 5);

        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void isDownWhenEveryConnectionIsBusyAndCallersWait() {
        poolState(5, 0, 5, 3, 5);

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(3, health.getDetails().get("waiting"));
    }

    @Test
    void isUnknownBeforeThePoolIsStarted() {
        when(dataSource.getHikariPoolMXBean()).thenReturn(null);

        assertEquals(Status.UNKNOWN, indicator.health().getStatus());
    }
}
