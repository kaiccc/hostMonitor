package com.mon.host.common.enums;

import cn.hutool.core.util.StrUtil;

public enum HostType {
    /**
     * 网络流量
     */
    NETWORK,
    /**
     * 硬盘IO
     */
    DISK;

    public static HostType getHostType(String key) {
        HostType hostType = null;

        for (HostType type : HostType.values()) {
            if (StrUtil.equalsIgnoreCase(type.name(), key)){
                hostType = type;
                break;
            }
        }

        return hostType;
    }
}
