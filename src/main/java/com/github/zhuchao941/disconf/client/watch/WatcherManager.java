package com.github.zhuchao941.disconf.client.watch;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.zookeeper.Watcher;

/**
 * Created by @author zhuchao on @date 2019/2/13.
 */
public class WatcherManager {

    private static Map<DisconfAppProp, Watcher> MAP = new ConcurrentHashMap<>();

    public static Watcher getWatcher(DisconfAppProp disconfAppProp) {
        Watcher watcher = MAP.get(disconfAppProp);
        if (watcher == null) {
            synchronized (disconfAppProp) {
                watcher = MAP.get(disconfAppProp);
                if (watcher == null) {
                    watcher = new PropertyFileZkWatcher(disconfAppProp);
                    MAP.put(disconfAppProp, watcher);
                }
            }
        }
        return watcher;
    }
}
