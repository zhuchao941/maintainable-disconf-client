package com.github.zhuchao941.disconf.client.loader.downloader;

import com.github.zhuchao941.disconf.client.config.DisconfAppProp;
import com.github.zhuchao941.disconf.client.util.DisconfUrlUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

/**
 * Created by @author zhuchao on @date 2019/1/22.
 */
@Slf4j
public class DisconfPropertyDownloader implements PropertyDownloader {

    public static ExecutorService THREADPOOL = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10,
            TimeUnit.SECONDS, new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("disconf-downloader-%s")
                    .build());

    @Override
    public List<File> download(List<DisconfAppProp> list, String downloadDir) {

        List<Future<File>> futures = new ArrayList<>(list.size());
        list.stream().forEach(disconfAppProp -> futures
                .add(THREADPOOL.submit(() -> download(disconfAppProp, downloadDir))));
        List<File> files = futures.stream().map(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to download file", e);
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return files;
    }

    private File download(DisconfAppProp disconfAppProp, String downloadDir) throws Exception {
        String urlString = DisconfUrlUtils.buildPropertyFileDownloadUrl(disconfAppProp);
        URL url = URI.create(urlString).toURL();
        String propertiesFileName = disconfAppProp.getKey();
        File file = new File(downloadDir, propertiesFileName);
        try {
            FileUtils.copyURLToFile(url, file);
        } catch (FileNotFoundException e) {
            // ignore
            log.warn("file not found:{}", urlString);
            return null;
        }
        log.info("download success:{}", urlString);
        return file;
    }
}
