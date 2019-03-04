package com.github.zhuchao941.disconf.client.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by @author zhuchao on @date 2018/12/28.
 */
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.ctx = applicationContext;
    }

    public static <T> T getBean(Class<T> requiredType) {
        if (ctx == null) {
            return null;
        }
        return ctx.getBean(requiredType);
    }

    public static <T> T getBean(String name, Class<T> requiredType) {
        if (ctx == null) {
            return null;
        }
        return ctx.getBean(name, requiredType);
    }
}
