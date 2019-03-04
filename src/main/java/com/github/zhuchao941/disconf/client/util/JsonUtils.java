package com.github.zhuchao941.disconf.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by @author zhuchao on @date 2019/1/28.
 */
public class JsonUtils {

    private final static Gson GSON = new GsonBuilder().create();

    public static String serillize(Object obj) {
        return GSON.toJson(obj);
    }
}
