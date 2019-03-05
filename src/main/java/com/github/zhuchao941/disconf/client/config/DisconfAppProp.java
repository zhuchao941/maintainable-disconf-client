package com.github.zhuchao941.disconf.client.config;

import java.util.Properties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by @author zhuchao on @date 2019/1/22.
 */
@Builder
@Getter
@EqualsAndHashCode(of = {"name", "env", "version", "key"})
public class DisconfAppProp {

    private String name;
    private String env;
    private String version;
    private String key;

    @Setter
    private Properties properties;

    private final static String TEMPLATE = "app=%s&env=%s&version=%s&key=%s&type=0";

    @Override
    public String toString() {
        return String.format(TEMPLATE, name, env, version, key);
    }
}
