package com.github.zhuchao941.disconf.client.loader.downloader;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import java.io.File;
import java.util.List;

/**
 * Created by @author zhuchao on @date 2019/1/23.
 */
public interface PropertyDownloader {

    List<File> download(List<DisconfAppProp> list, String downloadDir);
}
