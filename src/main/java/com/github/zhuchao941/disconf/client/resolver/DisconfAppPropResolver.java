package com.github.zhuchao941.disconf.client.resolver;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;

/**
 * Created by @author zhuchao on @date 2019/2/13.
 */
public interface DisconfAppPropResolver {

    List<DisconfAppProp> resolve(Environment environment, SpringApplication springApplication);
}
