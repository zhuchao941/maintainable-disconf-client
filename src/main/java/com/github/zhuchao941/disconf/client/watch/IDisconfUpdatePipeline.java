package com.github.zhuchao941.disconf.client.watch;

/**
 * Created by @author zhuchao on @date 2019/1/28.
 */
public interface IDisconfUpdatePipeline {

    void reloadDisconfFile(String fileName, String filePath) throws Exception;
}
