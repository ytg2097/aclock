package io.ac.lock;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-02-18
 **/
public class ACLock {
    private static final Logger log = LoggerFactory.getLogger(ACLock.class);
    private String applicationName;
    private RedissonClient redissonClient;

    public ACLock(String applicationName, String host, String port, String password) {
        this.applicationName = applicationName;
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress("redis://" + host + ":" + port);

        try {
            if (password != null) {
                singleServerConfig.setPassword(password);
            }
        } catch (Exception var8) {
            log.error("ac-lock error", var8);
        }

        this.redissonClient = Redisson.create(config);
    }

    public RLock getLock(String lockKey) {
        return this.redissonClient.getLock(lockKey);
    }

    public RReadWriteLock getReadWriteLock(String lockKey) {
        return this.redissonClient.getReadWriteLock(lockKey);
    }

    public void lockDo(String lockKey, ACLock.DoSomething function) {
        RLock lock = this.getLock(this.applicationName + ":lock:" + lockKey);
        lock.lock();

        try {
            function.execute();
        } catch (Exception var8) {
            log.error("ac-lock error", var8);
        } finally {
            lock.unlock();
        }

    }

    public <T> T lockDo(String lockKey, Supplier<T> function) {
        RLock lock = this.getLock(this.applicationName + ":lock:" + lockKey);
        lock.lock();

        try {
            return function.get();
        } catch (Exception var9) {
            log.error("ac-lock error", var9);
            return null;
        } finally {
            lock.unlock();
        }

    }

    public void readLockDo(String lockKey, ACLock.DoSomething function) {
        RReadWriteLock lock = this.getReadWriteLock(this.applicationName + ":rw-lock:" + lockKey);
        lock.readLock().lock();

        try {
            function.execute();
        } catch (Exception var8) {
            log.error("ac-lock error", var8);
        } finally {
            lock.readLock().unlock();
        }

    }

    public <T> T readLockDo(String lockKey, Supplier<T> function) {
        RReadWriteLock lock = this.getReadWriteLock(this.applicationName + ":rw-lock:" + lockKey);
        lock.readLock().lock();

        try {
           return function.get();
        } catch (Exception var9) {
            log.error("ac-lock error", var9);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void writeLockDo(String lockKey, ACLock.DoSomething function) {
        RReadWriteLock lock = this.getReadWriteLock(this.applicationName + ":rw-lock:" + lockKey);
        lock.writeLock().lock();

        try {
            function.execute();
        } catch (Exception var8) {
            log.error("ac-lock error", var8);
        } finally {
            lock.writeLock().unlock();
        }

    }

    public <T> T writeLockDo(String lockKey, Supplier<T> function) {
        RReadWriteLock lock = this.getReadWriteLock(this.applicationName + ":rw-lock:" + lockKey);
        lock.writeLock().lock();

        try {
            return function.get();
        } catch (Exception var9) {
            log.error("ac-lock error", var9);
            return null;
        } finally {
            lock.writeLock().unlock();
        }

    }

    public boolean tryLockDo(String lockKey, ACLock.DoSomething function) {
        RLock lock = this.getLock(this.applicationName + ":try-lock:" + lockKey);
        boolean isLock;
        if (isLock = lock.tryLock()) {
            try {
                function.execute();
                Thread.sleep(1000L);
            } catch (Exception var9) {
                log.error("ac-lock error", var9);
            } finally {
                lock.unlock();
            }
        }

        return isLock;
    }

    public <T> T tryLockDo(String lockKey, Supplier<T> function) {
        RLock lock = this.getLock(this.applicationName + ":try-lock:" + lockKey);
        T t = null;
        boolean isLock = false;

        try {
            if (isLock = lock.tryLock()) {
                log.info("开始执行定时任务：{}", lockKey);
                t = function.get();
                Thread.sleep(1000L);
            } else {
                log.info("定时任务({})已由其他client执行，跳过", lockKey);
            }
        } catch (Exception var10) {
            log.error("ac-lock error", var10);
        } finally {
            if (isLock) {
                lock.unlock();
            }

        }

        return t;
    }

    @FunctionalInterface
    public interface DoSomething {
        void execute();
    }
}

