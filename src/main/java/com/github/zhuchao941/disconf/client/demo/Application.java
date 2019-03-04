package com.github.zhuchao941.disconf.client.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by @author zhuchao on @date 2019/1/23.
 */
@SpringBootApplication
public class Application {

    static {
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext run = new SpringApplicationBuilder(Application.class)
                .run(args);
    }
}
