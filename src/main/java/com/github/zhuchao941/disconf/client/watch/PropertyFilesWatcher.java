package com.github.zhuchao941.disconf.client.watch;

import com.github.zhuchao941.disconf.client.config.DisconfConfig;
import com.github.zhuchao941.disconf.client.util.JsonUtils;
import com.github.zhuchao941.disconf.client.util.ZkPathUtils;
import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import com.github.zhuchao941.disconf.client.loader.DisconfPropertyLoader;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ThreadUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by @author zhuchao on @date 2019/1/26.
 */
@Slf4j
public class PropertyFilesWatcher {

    private final static PropertyFilesWatcher INSTANCE = new PropertyFilesWatcher();

    private PropertyFilesWatcher() {

    }

    private CuratorFramework client;
    private final AtomicBoolean isConnected = new AtomicBoolean(true);


    public static PropertyFilesWatcher getInstance() {
        return INSTANCE;
    }

    /**
     * 监听一组配置文件的变更
     */
    public void watch(List<DisconfAppProp> disconfAppPropList) throws Exception {
        String host = DisconfConfig.getInstance().getHost();
        // first to fetch zkAddr from the disconf web server
        BasicInfo basicInfo = BasicInfoFetcher.getInstance().fetchBasicInfo(host);
        // init zk client
        initZkClient(basicInfo.getZkAddr());
        // 重连时实际上只需要这一步
        disconfAppPropList.stream().forEach(disconfAppProp -> {
            try {
                watch4SinglePropFile(basicInfo.getZkRootNodePath(), disconfAppProp);
            } catch (Exception e) {
                throw new IllegalStateException("watch file occurs error", e);
            }
        });
    }

    private void watch4SinglePropFile(String zkRootNodePath, DisconfAppProp disconfAppProp)
            throws Exception {
        String propFilePath = ZkPathUtils.buildPropFilePath(zkRootNodePath, disconfAppProp);
        // create property file node
        createNodeIfNeeded(propFilePath, "");
        doWatch4SinglePropFile(propFilePath, disconfAppProp);
    }

    /**
     * 监听配置文件并上传实例信息
     */
    void doWatch4SinglePropFile(String propFilePath, DisconfAppProp disconfAppProp)
            throws Exception {
        // watch node data changed 需要重复注册
        client.getData().usingWatcher(WatcherManager.getWatcher(disconfAppProp))
                .inBackground((client, event) -> {
                    log.info("get data and register watcher success, path:{}, resultCode:{}",
                            event.getPath(), event.getResultCode());
                }).forPath(propFilePath);
        // upload instance info
        uploadInstanceInfo(propFilePath, disconfAppProp.getProperties());
    }

    /**
     * 初始化动作 1. 查询zk地址 2. 创建curator客户端 3. 查询node路径 4. 创建node
     */
    private void initZkClient(String zkAddr) {
        if (client != null) {
            return;
        }
        client = CuratorFrameworkFactory.newClient(zkAddr, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.getConnectionStateListenable().addListener((client, newState) -> {
            if ((newState == ConnectionState.CONNECTED) || (newState
                    == ConnectionState.RECONNECTED)) {
                if (isConnected.compareAndSet(false, true)) {
                    try {
                        DisconfPropertyLoader.getInstance().reload();
                        watch(DisconfConfig.getInstance().getDisconfAppPropList());
                    } catch (Exception e) {
                        ThreadUtils.checkInterrupted(e);
                        log.error("Trying to reset after reconnection", e);
                    }
                }
            } else {
                isConnected.set(false);
                log.error("disconf zk connection state is not correct:{}", newState.name());
            }
        });
    }

    /**
     * 上传实例信息
     */
    private void uploadInstanceInfo(String propFilePath, Properties properties) throws Exception {
        if (properties == null) {
            // file not found
            return;
        }
        String instancePath = ZkPathUtils.buildInstancePath(propFilePath);
        // create instance node
        Stat stat = client.checkExists().forPath(instancePath);
        // 为了和老数据保持一致顺序
        String propertiesAsString = JsonUtils.serillize(new HashMap<>(properties));
        if (stat != null) {
            // because of uuid, there is no need to check session owner
            Stat update = client.setData().withVersion(stat.getVersion())
                    .forPath(instancePath, propertiesAsString.getBytes());
            log.info("path:{}, update to version:{} with version:{}", instancePath,
                    update.getVersion(), stat.getVersion());
        } else {
            String s = client.create().withMode(CreateMode.EPHEMERAL)
                    .forPath(instancePath, propertiesAsString.getBytes());
            log.info("create new path:{}", s);
        }
    }

    /**
     * 创建PropFile节点信息
     */
    private boolean createNodeIfNeeded(String path, String data) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        if (stat == null) {
            // 并发场景会直接抛错 节点已存在之类的信息
            String result = client.create().creatingParentsIfNeeded()
                    .forPath(path, data.getBytes());
            log.info("create zk node:{}", result);
            return path.equals(result);
        }
        return false;
    }


    // TODO 写到zk上的properties的顺序不一样
    public static void main(String[] args) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("config/specific.properties");
        Properties properties = new Properties();
        properties.load(classPathResource.getInputStream());
        Map map = new HashMap<>(properties);
        Gson gson = new Gson();
        String json = gson.toJson(map);
        System.out.println(map);
        System.out.println(json);
    }
}
