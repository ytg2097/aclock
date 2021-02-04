package io.ac.lock;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-02-18
 **/
@ConfigurationProperties("spring")
@Data
public class RedissonProperties {

    private RedissonProperties.Application application;
    private RedissonProperties.Redis redis;

    public RedissonProperties.Application getApplication() {
        return this.application;
    }

    public RedissonProperties.Redis getRedis() {
        return this.redis;
    }


    @Data
    public static class Redis {
        private String host;
        private String port;
        private String password;

    }

    @Data
    public static class Application {
        private String name;

    }
}

