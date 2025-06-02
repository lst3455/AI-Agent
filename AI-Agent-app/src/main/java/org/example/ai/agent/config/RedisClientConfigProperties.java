package org.example.ai.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis connection configuration <a href="https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter">redisson-spring-boot-starter</a>
 */
@Data
@ConfigurationProperties(prefix = "redis.sdk.config", ignoreInvalidFields = true)
public class RedisClientConfigProperties {

    /** host:ip */
    private String host;
    /** port */
    private int port;
    /** account password */
    private String password;
    /** Set the size of the connection pool, default is 64 */
    private int poolSize = 64;
    /** Set the minimum number of idle connections in the pool, default is 10 */
    private int minIdleSize = 10;
    /** Set the maximum idle time for a connection (unit: ms), idle connections exceeding this time will be closed, default is 10000 */
    private int idleTimeout = 10000;
    /** Set the connection timeout (unit: ms), default is 10000 */
    private int connectTimeout = 10000;
    /** Set the number of connection retry attempts, default is 3 */
    private int retryAttempts = 3;
    /** Set the interval between connection retries (unit: ms), default is 1000 */
    private int retryInterval = 1000;
    /** Set the interval for periodically checking if the connection is available (unit: ms), default is 0, which means no periodic check */
    private int pingInterval = 0;
    /** Set whether to keep the connection alive, default is true */
    private boolean keepAlive = true;

}