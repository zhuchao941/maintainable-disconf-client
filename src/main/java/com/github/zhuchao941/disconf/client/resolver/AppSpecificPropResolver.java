package com.github.zhuchao941.disconf.client.resolver;

import com.google.common.collect.Lists;
import java.util.List;

/**
 * Created by @author zhuchao on @date 2019/2/13.
 */
@Resolver(order = AppSpecificPropResolver.ORDER)
public class AppSpecificPropResolver extends AbstractDisconfAppPropResolver {

    public static final int ORDER = -1999;

    @Override
    public List<String> defaultFiles() {
        return Lists.newArrayList("specific.properties");
    }

    @Override
    protected String fileNameStrKey() {
        return "disconf.app_conf_files_name";
    }
}
