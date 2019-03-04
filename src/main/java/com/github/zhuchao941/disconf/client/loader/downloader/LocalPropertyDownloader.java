package com.github.zhuchao941.disconf.client.loader.downloader;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by @author zhuchao on @date 2019/1/23.
 */
public class LocalPropertyDownloader implements PropertyDownloader {

    @Override
    public List<File> download(List<DisconfAppProp> list, String downloadDir) {
        return list.stream().map(disconfAppProp -> {
            String propertiesFileName = disconfAppProp.getKey();
            return new File(downloadDir, propertiesFileName);
        }).filter(file -> file.exists()).collect(Collectors.toList());
    }
}
