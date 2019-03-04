package com.github.zhuchao941.disconf.client.watch;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import com.github.zhuchao941.disconf.client.loader.DisconfPropertyLoader;
import com.github.zhuchao941.disconf.client.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

/**
 * Created by @author zhuchao on @date 2019/2/13.
 */
@Slf4j
public class PropertyFileZkWatcher implements Watcher {

    private DisconfAppProp disconfAppProp;

    public PropertyFileZkWatcher(DisconfAppProp disconfAppProp) {
        this.disconfAppProp = disconfAppProp;
    }

    @Override
    public void process(WatchedEvent event) {
        log.info("received:{} for watcher:{}", event, disconfAppProp);
        if (event.getType() == EventType.NodeDataChanged) {
            try {
                reload(event.getPath());
            } catch (Exception e) {
                log.error("reload occurs error", e);
            }
        }
    }

    private void reload(String propFilePath) throws Exception {
        try {
            DisconfPropertyLoader.getInstance().reload(disconfAppProp);
        } catch (Throwable e) {
            log.error("reload occurs error, prop:{}", disconfAppProp, e);
        }
        PropertyFilesWatcher.getInstance().doWatch4SinglePropFile(propFilePath, disconfAppProp);
        IDisconfUpdatePipeline pipeline = SpringUtils.getBean(IDisconfUpdatePipeline.class);
        if (pipeline != null) {
            pipeline.reloadDisconfFile(disconfAppProp.getKey(), propFilePath);
        }
    }
}
