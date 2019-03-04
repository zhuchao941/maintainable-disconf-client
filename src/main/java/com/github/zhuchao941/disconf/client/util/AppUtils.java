package com.github.zhuchao941.disconf.client.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * Created by @author zhuchao on @date 2018/12/14.
 */
@Slf4j
public class AppUtils {

    public final static String PROJECT_NAME;
    public final static String IP;
    public final static String HOST_NAME;

    static {
        PROJECT_NAME = System.getProperty("project.name");
        Assert.notNull(PROJECT_NAME, "project.name cannot be null");

        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("getLocalHost occurs error", e);
        }
        IP = localHost.getHostAddress();
        HOST_NAME = localHost.getHostName();
    }

    public static void main(String[] args) {
        System.out.println(PROJECT_NAME);
        System.out.println(IP);
        System.out.println(HOST_NAME);

        long l = System.currentTimeMillis();
        System.out.println(l);
        System.out.println(l % 4);
        System.out.println(l & 3);
    }
}
