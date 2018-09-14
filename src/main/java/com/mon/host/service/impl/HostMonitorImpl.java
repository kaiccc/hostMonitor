package com.mon.host.service.impl;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mon.host.common.enums.HostType;
import com.mon.host.common.utils.NullStringToEmptyAdapterFactory;
import com.mon.host.dto.HostMonAgent;
import com.mon.host.dto.HostMonEcharts;
import com.mon.host.service.IHostMonitorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 主机监控 服务实现类
 * </p>
 *
 * @author xt
 * @since 2018年8月31日
 */
@Service
public class HostMonitorImpl implements IHostMonitorService {

    @Value("${mon.spacing}")
    private int mon_spacing;
    private int minute = 60;
    private static final Log log = LogFactory.get();

    private TimedCache<String, List<HostMonAgent>> timedCache ;
    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory()).create();
    
    @Override
    public String findHostPayload(HostType hostType, long start, long end) {
        List<String> cacheKeys = this.getMinuteBetween(start, end);
        List<List<String>> inCountList = CollUtil.newArrayList();
        List<List<String>> outCountList = CollUtil.newArrayList();

        List<HostMonEcharts> hostMonEchartList = CollUtil.newArrayList();
        Map<String, List<HostMonAgent>> agentMap = CollUtil.newHashMap();

        for (String key : cacheKeys) {
            String redisKey = constructCacheKey(hostType, key);
            List<HostMonAgent> networkTimeCacheList = timedCache.get(redisKey);

            if(CollUtil.isEmpty(networkTimeCacheList)){
                continue;
            }

            for(HostMonAgent agent : networkTimeCacheList) {

                if (!agentMap.containsKey(agent.getHostName())){
                    agentMap.put(agent.getHostName(), CollUtil.newArrayList(agent));
                }else {
                    List<HostMonAgent> agentList = agentMap.get(agent.getHostName());
                    if (!CollUtil.isEmpty(agentList)){
                        agentList.add(agent);
                    }else {
                        agentList = CollUtil.newArrayList();
                    }
                    agentMap.put(agent.getHostName(), agentList);
                }
            }
            /*
             * 计算每30秒所有主机流量的平均值
             */
            inCountList.addAll(hostAverageCount(agentMap, true, key));
            outCountList.addAll(hostAverageCount(agentMap, false,key));
        }
        if (!CollUtil.isEmpty(agentMap)){
            hostMonEchartList.add(new HostMonEcharts("所有主机-Input", inCountList));
            hostMonEchartList.add(new HostMonEcharts("所有主机-Output", outCountList));
        }

        int hostCount = 0;
        String maxHostName = "";

        for (String agentName : agentMap.keySet()) {

            List<List<String>> inList = CollUtil.newArrayList();
            List<List<String>> outList = CollUtil.newArrayList();
            List<HostMonAgent> agentList = agentMap.get(agentName);

            Collections.sort(agentList);

            if (agentList.size() > hostCount){
                hostCount = agentList.size();
                maxHostName = agentName;
            }

            for (HostMonAgent agent : agentList){
                inList.add(CollUtil.newArrayList(DateUtil.date(agent.getData().getTime()).toString()
                                                    , String.valueOf(agent.getData().getIn())));

                outList.add(CollUtil.newArrayList(DateUtil.date(agent.getData().getTime()).toString()
                                                    , String.valueOf(agent.getData().getOut())));
            }

            HostMonEcharts agentInDto = new HostMonEcharts(agentName+"-Input", inList);
            hostMonEchartList.add(agentInDto);

            HostMonEcharts agentOutDto = new HostMonEcharts(agentName+"-Output", outList);
            hostMonEchartList.add(agentOutDto);
        }
        Map<String, Object> map = CollUtil.newHashMap();
        map.put("list", hostMonEchartList);
        map.put("xAxis", countXAxis(agentMap.get(maxHostName)));
        return gson.toJson(map);
    }

    @Override
    public void saveHostPayload(HostType hostType, HostMonAgent hostMonAgent) {
        HostMonAgent.DataBean agent = hostMonAgent.getData();
        String redisKey = constructCacheKey(hostType, String.valueOf(agent.getTime() / minute ));

        DateTime agentDate = DateUtil.date(agent.getTime() * 1000);
        agent.setTime(agentDate.setField(DateField.SECOND, correctionTime(agentDate.getField(DateField.SECOND))).getTime());
        hostMonAgent.setData(agent);

        List<HostMonAgent> timeNetList = timedCache.get(redisKey);
        if (CollUtil.isEmpty(timeNetList)){
            timedCache.put(redisKey, CollUtil.newArrayList(hostMonAgent));
        }else {
            timeNetList.add(hostMonAgent);
            timedCache.put(redisKey, timeNetList);
        }
        log.info("save {} | {}", gson.toJson(hostMonAgent), redisKey);
    }

    private String constructCacheKey(HostType hostType, String key){
        return StrUtil.format("{}_{}", hostType, key);
    }
    /**
     * 分钟范围
     * @param start
     * @param end
     * @return
     */
    private List<String> getMinuteBetween(long start, long end) {
        List<String> minuteKeys = Lists.newArrayList();
        start = start / minute;
        end = end / minute;

        while (start != end) {
            minuteKeys.add(String.valueOf(start));
            start ++;
        }

        return minuteKeys;
    }

    /**
     * 秒 校正时间 13秒 = 10秒
     * @param second
     * @return
     */
    private int correctionTime(int second){
        int count = minute / mon_spacing;
        for (int i=0; i < count; i++){
            if (second >= i*mon_spacing && second < (i+1)*mon_spacing){
                return i*mon_spacing;
            }
        }
        return 0;
    }

    /**
     * 计算主机范围时间 30秒 内的平均值
     * @return 平均值
     */
    private List<List<String>> hostAverageCount(Map<String, List<HostMonAgent>> agentMap, boolean isIn, String nowMinute ){
        int between = 30;
        List<List<String>> countList = CollUtil.newArrayList();

        if (! CollUtil.isEmpty(agentMap)){
            for (int i=1; i<= minute / between; i++){
                double hostCount = 0;

                for (String agentName : agentMap.keySet()) {
                    double count = 0;
                    double frequency = 0;

                    List<HostMonAgent> agentList = agentMap.get(agentName);
                    // 单个 主机的信息
                    for (HostMonAgent agent : agentList){
                        DateTime time = DateUtil.date(agent.getData().getTime());

                        if (time.getField(DateField.SECOND) >= (i-1) * between && time.getField(DateField.SECOND) < i * between ){
                            frequency++;
                            if (isIn){
                                count +=agent.getData().getIn();
                            }else {
                                count +=agent.getData().getOut();
                            }
                        }
                    }
                    if (count != 0 && frequency != 0){
                        //log.info("host:{},{}, count:{}, frequency:{}, {}", agentName, dateTime.toString(), count, frequency, count/frequency);
                        hostCount += count / frequency;
                    }
                }
                DateTime dateTime = DateUtil.date((Long.parseLong(nowMinute) * 60 + i * between) *  1000);
                countList.add(CollUtil.newArrayList(dateTime.toString(), NumberUtil.roundStr(hostCount, 2)));
            }
        }

        return countList;
    }

    /**
     * 计算X轴 数据
     * @param agentList
     * @return
     */
    private List<String> countXAxis(List<HostMonAgent> agentList){
        List<String> xAxis = CollUtil.newArrayList();
        if (! CollUtil.isEmpty(agentList)){
            DateTime startTime = DateUtil.date(agentList.get(0).getData().getTime());
            DateTime endTime = DateUtil.date(agentList.get(agentList.size()-1).getData().getTime());

            long time = (endTime.getTime() - startTime.getTime()) / 1000 / mon_spacing;
            for (int i=0; i < time; i++){
                xAxis.add(DateUtil.date(startTime.getTime() + i*mon_spacing*1000).toString());
            }
        }
        return xAxis;
    }

    public HostMonitorImpl() {
        Props props = new Props("mon.properties");
        /*
         * 实例化 时间缓存，定时 清理超时数据
         */
        this.timedCache = new TimedCache<String, List<HostMonAgent>>( props.getInt("mon.cache.timeout") * 60 * 1000);
        this.timedCache.schedulePrune(  props.getInt("mon.cache.delay") * 60 * 1000);
    }


}
