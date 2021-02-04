package io.ac.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-02-18
 **/
@Configuration
@EnableConfigurationProperties({RedissonProperties.class})
@ConditionalOnClass({ACLock.class})
public class LockAutoConfig {
    private static final Logger log = LoggerFactory.getLogger(LockAutoConfig.class);
    @Resource
    private RedissonProperties redissonProperties;

    public LockAutoConfig() {
    }

    @Bean
    public ACLock acLock() {
        log.info("初始化ACLock");
        RedissonProperties.Redis redis = this.redissonProperties.getRedis();
        if (redis == null) {
            throw new NullPointerException("缺少redis配置");
        } else if (redis.getHost() == null) {
            throw new NullPointerException("缺少redis host配置");
        } else if (redis.getPort() == null) {
            throw new NullPointerException("缺少redis port配置");
        } else {
            RedissonProperties.Application application = this.redissonProperties.getApplication();
            String applicationName = "";
            if (application != null && application.getName() != null) {
                applicationName = application.getName();
            }

            return new ACLock(applicationName, this.redissonProperties.getRedis().getHost(), this.redissonProperties.getRedis().getPort(), this.redissonProperties.getRedis().getPassword());
        }
    }

    @Bean
    public ScheduleAop scheduleAop() {
        return new ScheduleAop();
    }
}

