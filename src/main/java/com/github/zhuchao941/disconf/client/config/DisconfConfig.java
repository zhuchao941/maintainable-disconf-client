package com.github.zhuchao941.disconf.client.config;

import com.github.zhuchao941.disconf.client.resolver.DisconfAppPropResolver;
import com.github.zhuchao941.disconf.client.resolver.Resolver;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;

/**
 * Created by @author zhuchao on @date 2019/1/23.
 */
@Getter
@Slf4j
public class DisconfConfig {

    private final static DisconfConfig INSTANCE = new DisconfConfig();

    public static DisconfConfig getInstance() {
        return INSTANCE;
    }

    private DisconfConfig() {

    }

    private Boolean remote;
    private String host;
    private String downloadDir;
    private List<DisconfAppProp> disconfAppPropList;

    public void init(Environment environment, SpringApplication springApplication) {
        // 这一部分都可以不填
        String downloadDir = environment
                .getProperty("disconf.user_define_download_dir", "./src/main/resources/config");
        Boolean remote = environment
                .getProperty("disconf.enable.remote.conf", Boolean.class, Boolean.TRUE);
        String host = environment.getProperty("disconf.conf_server_host");

        ServiceLoader<DisconfAppPropResolver> loader = ServiceLoader
                .load(DisconfAppPropResolver.class);
        List<DisconfAppPropResolver> resolvers = Lists.newArrayList(loader.iterator()).stream()
                .filter(o -> o.getClass().isAnnotationPresent(Resolver.class)).sorted((o1, o2) -> {
                    Resolver r1 = o1.getClass().getAnnotation(Resolver.class);
                    Resolver r2 = o2.getClass().getAnnotation(Resolver.class);
                    return Integer.compare(r1.order(), r2.order());
                }).collect(Collectors.toList());
        log.info("resolvers are:{}", resolvers);

        List<DisconfAppProp> total = resolvers.stream()
                .map(resolver -> resolver.resolve(environment, springApplication))
                .flatMap(List::stream).collect(Collectors.toList());

        checkIfDuplicate(total);

        this.remote = remote;
        this.host = host;
        this.downloadDir = downloadDir;
        this.disconfAppPropList = total;
    }

    private void checkIfDuplicate(List<DisconfAppProp> total) {
        List<String> duplicates = total.stream()
                .collect(Collectors.groupingBy(prop -> prop.getKey())).entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1).map(entry -> entry.getKey())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(duplicates)) {
            throw new IllegalStateException(String.format("config file duplicate:%s", duplicates));
        }
    }
}
