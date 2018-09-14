package com.mon.host.controller;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.mon.host.common.enums.HostType;
import com.mon.host.common.rest.RestResponse;
import com.mon.host.dto.HostMonAgent;
import com.mon.host.service.IHostMonitorService;
import io.swagger.annotations.Api;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 主机监控 前端控制器
 * </p>
 *
 * @author xt
 * @since 2018-8-31
 */
@RestController
@RequestMapping(value = "/api/", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = "HostMonitorController", description = "主机监控", produces = MediaType.APPLICATION_JSON_VALUE)
public class HostMonitorController {
    @Resource
    private IHostMonitorService networkMonitorService;
    private static final Log log = LogFactory.get();

    /**
     * 得到主机动态使用率监控数据
     * @param startMillSeconds 开始时间
     * @param endMillSeconds   结束时间
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{type}/dynamic/{startMillSeconds}/{endMillSeconds}", method = RequestMethod.GET)
    @ResponseBody
    public RestResponse findNetWorkPayloadDynamic(@PathVariable String type, @PathVariable long startMillSeconds, @PathVariable long endMillSeconds) throws Exception {
        return RestResponse.success(networkMonitorService.findHostPayload(HostType.getHostType(type), startMillSeconds,endMillSeconds));
    }

    @RequestMapping(value = "/{type}/{hostName}/info", method = RequestMethod.POST)
    @ResponseBody
    public RestResponse saveNetWorkPayload(@PathVariable String type ,@PathVariable String hostName, @RequestBody HostMonAgent agent) throws Exception {
        networkMonitorService.saveHostPayload(HostType.getHostType(type), agent);
        return RestResponse.success();
    }

}
