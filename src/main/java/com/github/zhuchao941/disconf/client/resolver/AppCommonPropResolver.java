package com.github.zhuchao941.disconf.client.resolver;


/**
 * Created by @author zhuchao on @date 2019/2/13.
 */
@Resolver(order = AppCommonPropResolver.ORDER)
public class AppCommonPropResolver extends AbstractDisconfAppPropResolver {

    public static final int ORDER = -2999;

    @Override
    protected String name() {
        return "common";
    }

    @Override
    protected String fileNameStrKey() {
        return "disconf.common_app_conf_files_name";
    }
}
