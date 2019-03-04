package com.github.zhuchao941.disconf.client.util;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import java.util.UUID;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;

/**
 * Created by @author zhuchao on @date 2019/1/28.
 */
public class ZkPathUtils {

    public static final String PROPERTY_FILE_NODE_TEMPLATE = "%s_%s_%s";
    public static final String INSTANCE_NODE_TEMPLATE = "%s_%s_%s_%s";
    public static final String FILE_NODE = "file";
    private static String UNIQKEY = UUID.randomUUID().toString();

    public static String buildPropFilePath(String zkRootNodePath, DisconfAppProp disconfAppProp) {
        String name = disconfAppProp.getName();
        String version = disconfAppProp.getVersion();
        String env = disconfAppProp.getEnv();
        String key = disconfAppProp.getKey();
        return ZKPaths.makePath(zkRootNodePath,
                String.format(PROPERTY_FILE_NODE_TEMPLATE, name, version, env), FILE_NODE, key);
    }

    public static String buildInstancePath(String propFilePath) {
        return ZKPaths.makePath(propFilePath,
                String.format(INSTANCE_NODE_TEMPLATE, AppUtils.HOST_NAME, AppUtils.IP,
                        AppUtils.PROJECT_NAME, UNIQKEY));
    }

    public static void main(String[] args) throws Exception {
        String zkAddr = "192.168.11.29:2182";
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .sessionTimeoutMs(Integer.MAX_VALUE).connectString(zkAddr)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        client.start();
        String path = "/disconf/wireless-monitor-service_1_0_0_1_dwd-dev/file";
        NodeCache nodeCache = new NodeCache(client, path);
        nodeCache.start(true);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                ChildData currentData = nodeCache.getCurrentData();
                System.out.println("nodeChanged:" + new String(currentData.getData()));
            }
        });
//        client.create().withMode(CreateMode.EPHEMERAL).forPath(ZKPaths.makePath(path, "test4.properties"), "test".getBytes());
        Thread.sleep(Integer.MAX_VALUE);
    }
}
