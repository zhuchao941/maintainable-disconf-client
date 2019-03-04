package com.github.zhuchao941.disconf.client.watch;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by @author zhuchao on @date 2019/1/26.
 */
@Getter
@Builder
@ToString
public class BasicInfo {

    private String zkAddr;
    private String zkRootNodePath;
}
