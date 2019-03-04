package com.github.zhuchao941.disconf.client.watch;

import com.github.zhuchao941.disconf.client.util.HttpUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by @author zhuchao on @date 2019/1/27.
 */
@Slf4j
public class BasicInfoFetcher {

    public static final String PREFIX_QUERY_PATH = "/api/zoo/prefix";
    public static final String ZK_ADDR_QUERY_PATH = "/api/zoo/hosts";

    private final static BasicInfoFetcher INSTANCE = new BasicInfoFetcher();
    private BasicInfo basicInfo;

    private BasicInfoFetcher() {

    }

    public static BasicInfoFetcher getInstance() {
        return INSTANCE;
    }

    private String queryDisconfZkAddr(String host) throws Exception {
        return queryValue(String.format("%s%s", host, ZK_ADDR_QUERY_PATH));
    }

    private String queryZkRootPath(String host) throws Exception {
        return queryValue(String.format("%s%s", host, PREFIX_QUERY_PATH));
    }

    private String queryValue(String url) throws Exception {
        String result = HttpUtils.get(url);
        JsonObject jsonElement = (JsonObject) new JsonParser().parse(result);
        if (jsonElement.get("status").getAsBoolean()) {
            throw new IllegalArgumentException(
                    String.format("query failed. url:%s, response:%s", url, result));
        }
        return jsonElement.get("value").getAsString();
    }

    public BasicInfo fetchBasicInfo(String host) throws Exception {
        if (basicInfo != null) {
            return basicInfo;
        }
        synchronized (this) {
            if (basicInfo != null) {
                return basicInfo;
            }
            basicInfo = BasicInfo.builder().zkAddr(queryDisconfZkAddr(host))
                    .zkRootNodePath(queryZkRootPath(host)).build();
            log.info("disconf zk basic info:{}", basicInfo);
            return basicInfo;
        }
    }
}
