package com.github.zhuchao941.disconf.client.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by @author zhuchao on @date 2019/1/30.
 */
@Slf4j
public class DisconfConfigUtils {

    public static final String PRODUCTION_PROFILE_NAME = "production";
    public static final String PRE_PROFILE_NAME = "dwd-pre";
    public static final String DEFAULT_PROFILE_NAME = "default";
    public static final String DISCONF_ENV_DEV = "dwd-dev";

    public static String convertProfilesToDisconfEnv(String profiles[]) {
        if (ArrayUtils.isEmpty(profiles)) {
            return DISCONF_ENV_DEV;
        }
        if (profiles.length == 1 && DEFAULT_PROFILE_NAME.equals(profiles[0])) {
            return DISCONF_ENV_DEV;
        }
        String profile = profiles[0];
        if (profiles.length > 1) {
            List<String> collect = Arrays.stream(profiles)
                    .filter(s -> !DEFAULT_PROFILE_NAME.equals(s)).collect(Collectors.toList());
            if (collect.size() > 1) {
                throw new RuntimeException(
                        "profiles more than one, " + StringUtils.join(profiles, ","));
            }
            profile = collect.get(0);
        }
        if (PRODUCTION_PROFILE_NAME.equalsIgnoreCase(profile)) {
            return "prod";
        }
        if (PRE_PROFILE_NAME.equalsIgnoreCase(profile)) {
            return "pre";
        }
        return profile;
    }

    public static void main(String[] args) {
        String s = convertProfilesToDisconfEnv(new String[]{"default", "dwd-qa1"});
        String s2 = convertProfilesToDisconfEnv(new String[]{"default"});
        String s3 = convertProfilesToDisconfEnv(null);
        String s4 = convertProfilesToDisconfEnv(new String[]{"default", PRODUCTION_PROFILE_NAME});
        String s5 = convertProfilesToDisconfEnv(new String[]{"default", PRE_PROFILE_NAME});
        System.out.println(s);
        System.out.println(s2);
        System.out.println(s3);
        System.out.println(s4);
        System.out.println(s5);
    }
}
