package com.github.zhuchao941.disconf.client.loader;

import com.github.zhuchao941.disconf.client.config.DisconfConfig;
import com.github.zhuchao941.disconf.client.watch.PropertyFilesWatcher;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Created by @author zhuchao on @date 2019/1/23.
 */
@Slf4j
public class DisconfPropertyProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
            SpringApplication application) {

        try {
            DisconfPropertyLoader.getInstance().load(environment, application);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to call DisconfPropertyLoader.load", e);
        }

        DisconfConfig disconfConfig = DisconfConfig.getInstance();
        if (disconfConfig.getRemote()) {
            try {
                PropertyFilesWatcher.getInstance().watch(disconfConfig.getDisconfAppPropList());
            } catch (Exception e) {
                throw new IllegalStateException("Unable to call PropertyFilesWatcher.watch", e);
            }
        }
    }
}
