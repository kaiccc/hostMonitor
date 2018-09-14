package com.mon.host.service;


import com.mon.host.common.enums.HostType;
import com.mon.host.dto.HostMonAgent;

/**
 * <p>
 * 主机 服务类
 * </p>
 *
 * @author xt
 * @since 2018年8月31日
 */

public interface IHostMonitorService {

    /**
     * 动态查询 主机信息
     * @param start
     * @param end
     * @return
     */
    String findHostPayload(HostType hostType, long start, long end);

    /**
     * 保存 主机信息
     * @param agent
     */
    void saveHostPayload(HostType hostType ,HostMonAgent agent) throws Exception;

}
