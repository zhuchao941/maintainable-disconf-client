package com.github.zhuchao941.disconf.client.util;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import com.github.zhuchao941.disconf.client.config.DisconfConfig;

/**
 * Created by @author zhuchao on @date 2019/1/27.
 */
public class DisconfUrlUtils {

    private static final String PATH = "api/config/file";

    public static String buildPropertyFileDownloadUrl(DisconfAppProp disconfAppProp) {
        String host = DisconfConfig.getInstance().getHost();
        StringBuilder sb = new StringBuilder(host);
        String suffix = "/";
        if (!host.endsWith(suffix) && !PATH.startsWith(suffix)) {
            sb.append(suffix);
        }
        return sb.append(PATH).append("?").append(disconfAppProp.toString()).toString();
    }
}
