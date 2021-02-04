package io.ac.lock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-02-18
 **/
@SpringBootApplication
public class LockApplication {
    public LockApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(LockApplication.class, args);
    }
}

