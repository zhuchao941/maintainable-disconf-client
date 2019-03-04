package com.github.zhuchao941.disconf.client.loader;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import com.github.zhuchao941.disconf.client.config.DisconfConfig;
import com.github.zhuchao941.disconf.client.loader.downloader.LocalPropertyDownloader;
import com.github.zhuchao941.disconf.client.loader.downloader.DisconfPropertyDownloader;
import com.github.zhuchao941.disconf.client.loader.downloader.PropertyDownloader;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Created by @author zhuchao on @date 2019/1/23.
 */
@Slf4j
public class DisconfPropertyLoader {

    private final static String DISCONF_PROPERTIES_FILE_NAME = "disconf.properties";
    private final static String DISCONF_PROPERTY_SOURCE_NAME = "wireless-application";

    private static DisconfPropertyLoader INSTANCE = new DisconfPropertyLoader();

    public static DisconfPropertyLoader getInstance() {
        return INSTANCE;
    }

    private DisconfPropertyLoader() {

    }

    private ConfigurableEnvironment environment;

    public void init(ConfigurableEnvironment environment) {
        if (environment != null) {
            this.environment = environment;
        }
    }

    /**
     * load disconf.properties, it must be the first properties to load
     */
    private void loadDisconfProperties() throws IOException {
        Assert.notNull(environment, "environment is null");
        ClassPathResource resource = new ClassPathResource(DISCONF_PROPERTIES_FILE_NAME);
        if (!resource.exists()) {
            log.warn("classpath resource:{} is not exist", DISCONF_PROPERTIES_FILE_NAME);
            return;
        }
        PropertySource<?> propertySource = new PropertiesPropertySourceLoader()
                .load(DISCONF_PROPERTIES_FILE_NAME, resource, null);
        environment.getPropertySources().addLast(propertySource);
    }

    public void load(ConfigurableEnvironment environment, SpringApplication springApplication)
            throws IOException {
        // first init loader
        init(environment);

        // load disconf.properties
        loadDisconfProperties();

        // init disconf config from environment
        DisconfConfig.getInstance().init(environment, springApplication);

        // load disconf property files
        loadDisconfPropertyFiles();
    }

    /**
     * load disconf property files(include download them)
     */
    private void loadDisconfPropertyFiles() {
        // remote or local
        DisconfConfig disconfConfig = DisconfConfig.getInstance();
        List<DisconfAppProp> disconfAppPropList = disconfConfig.getDisconfAppPropList();
        doLoad(disconfAppPropList);
    }

    private void doLoad(List<DisconfAppProp> needDownload) {
        DisconfConfig disconfConfig = DisconfConfig.getInstance();
        List<DisconfAppProp> whole = disconfConfig.getDisconfAppPropList();
        Boolean remote = disconfConfig.getRemote();
        PropertyDownloader propertyDownloader =
                remote ? new DisconfPropertyDownloader() : new LocalPropertyDownloader();
        List<File> fileList = propertyDownloader
                .download(needDownload, disconfConfig.getDownloadDir());
        Map<String, Properties> propertiesMapFromFile = doLoad0(fileList);
        // 赋值到disconfConfig.getDisconfAppPropList()里
        whole.stream().forEach(disconfAppProp -> {
            Properties newProperties = propertiesMapFromFile.get(disconfAppProp.getKey());
            if (newProperties != null) {
                Properties currentProperties = disconfAppProp.getProperties();
                if (currentProperties != null) {
                    MapDifference<Object, Object> difference = Maps
                            .difference(currentProperties, newProperties);
                    log.info("property file:{}, difference:{}", disconfAppProp, difference);
                }
                disconfAppProp.setProperties(newProperties);
            }
        });
        loadToEnv();
    }

    private void loadToEnv() {
        Properties result = new Properties();
        DisconfConfig.getInstance().getDisconfAppPropList().forEach(disconfAppProp -> {
            log.info("loading file:{}, app:{}, version:{}", disconfAppProp.getKey(),
                    disconfAppProp.getName(), disconfAppProp.getVersion());
            CollectionUtils.mergePropertiesIntoMap(disconfAppProp.getProperties(), result);
        });
        PropertiesPropertySource propertySource = new PropertiesPropertySource(
                DISCONF_PROPERTY_SOURCE_NAME, result);
        MutablePropertySources propertySources = environment.getPropertySources();
        if (propertySources.contains(DISCONF_PROPERTY_SOURCE_NAME)) {
            propertySources.replace(DISCONF_PROPERTY_SOURCE_NAME, propertySource);
        } else {
            propertySources.addFirst(propertySource);
        }
    }

    /**
     * load properties from property files
     */
    private Map<String, Properties> doLoad0(List<File> fileList) {
        Map<String, Properties> map = new HashMap<>(fileList.size());
        fileList.stream().forEach(file -> {
            FileSystemResource resource = new FileSystemResource(file);
            Properties properties;
            try {
                properties = PropertiesLoaderUtils.loadProperties(resource);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to loader file", e);
            }
            map.put(file.getName(), properties);
        });
        return map;
    }

    public void reload(DisconfAppProp disconfAppProp) {
        doLoad(Collections.singletonList(disconfAppProp));
    }

    public void reload() {
        loadDisconfPropertyFiles();
    }
}
