package com.github.zhuchao941.disconf.client.resolver;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import com.github.zhuchao941.disconf.client.util.DisconfConfigUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;

/**
 * Created by @author zhuchao on @date 2019/2/13.
 */
@NoArgsConstructor
public abstract class AbstractDisconfAppPropResolver implements DisconfAppPropResolver {

    @Setter
    protected Environment environment;
    @Setter
    protected SpringApplication springApplication;

    @Override
    public List<DisconfAppProp> resolve(Environment environment,
            SpringApplication springApplication) {
        setEnvironment(environment);
        setSpringApplication(springApplication);
        String fileNameStr = null;
        String key = fileNameStrKey();
        if (StringUtils.isNotBlank(key)) {
            fileNameStr = environment.getProperty(key);
        }
        List<String> fileNames = new ArrayList<>();
        if (StringUtils.isNotBlank(fileNameStr)) {
            fileNames.addAll(Arrays.asList(fileNameStr.split(",")));
        }
        List<String> defaultFiles = defaultFiles();
        if (CollectionUtils.isNotEmpty(defaultFiles)) {
            fileNames.addAll(defaultFiles);
        }
        return fileNames.stream()
                .map(fileName -> DisconfAppProp.builder().name(name()).env(env()).version(version())
                        .key(fileName).build()).collect(Collectors.toList());
    }

    protected List<String> defaultFiles() {
        return null;
    }

    protected String name() {
        return getProperty("project.name");
    }

    protected String env() {
        return DisconfConfigUtils.convertProfilesToDisconfEnv(environment.getActiveProfiles());
    }

    protected String version() {
        String version = defaultVersion();
        // 异地多活相关
        String region = getProperty("region");
        String zone = getProperty("zone");
        if (StringUtils.isNoneBlank(region, zone)) {
            version = region + "_" + zone;
        }
        return version;
    }

    protected String defaultVersion() {
        return getProperty("disconf.version");
    }

    protected String getProperty(String key) {
        return environment.getProperty(key);
    }

    protected abstract String fileNameStrKey();

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
