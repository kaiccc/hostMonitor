var vm = {
    dynamicChartDom : null,
    hostDataList : [],
    xTimeList : [],
    hostType : 'network',
    echartsTitle : '网络流量(MByte/s)',
    isStartRefresh : true,
    dynamicTimer : null
};

function initEcharts() {
    vm.dynamicChartDom = {};
    vm.dynamicChartDom = echarts.init(document.getElementById('dynamicChart'));
    dynamicAjaxNetworkData();

    vm.dynamicTimer = setInterval(function () {
        dynamicAjaxNetworkData();
    }, 6000);
}

$(function(){
    $('#hostTypeSelect').on('change',function(){
        vm.hostType = $('#hostTypeSelect').val();
        var text = '';
        if (vm.hostType === 'network'){
            vm.echartsTitle = '网络流量(MByte/s)';
        }else {
            vm.echartsTitle = '磁盘IO(MByte/s)';
        }
        $("#hostTypeBtn").html(text);
        initEcharts();
    });
    initEcharts();
});

function dynamicChart() {
    // 指定图表的配置项和数据
    var dynamicChartOption = getLineOption(vm.hostDataList, vm.xTimeList, vm.echartsTitle);
    vm.dynamicChartDom.setOption(dynamicChartOption);
}

function dynamicAjaxNetworkData() {
    var nowTime = Date.parse(new Date()) / 1000 + 60;
    var startSeconds = nowTime - 30 * 60;
    $.ajax({
        type: 'GET',
        url: "/mon/api/"+vm.hostType+"/dynamic/"+startSeconds+"/"+nowTime,
        dataType: "JSON",
        success: function (result) {
            if (result.code === 0) {
                var rJson = JSON.parse(result.content);
                vm.hostDataList = rJson.list;
                vm.xTimeList = rJson.xAxis;
                dynamicChart();
            }else {
                layer.alert(result.message, {icon: 5});
            }
        }
    });
}

function refreshDynamicTimer() {
    if (vm.isStartRefresh){
        vm.isStartRefresh = false;

        clearInterval(vm.dynamicTimer);
        $("#refreshBtn").html("开始刷新");

    }else {
        vm.isStartRefresh = true;
        dynamicAjaxNetworkData();
        vm.dynamicTimer = setInterval(function () {
            dynamicAjaxNetworkData();
        }, 6000);

        $("#refreshBtn").html("停止刷新");
    }

}

var getLineOption = function (data,timeList, title ) {
    var listModel = [];
    var name = [];
    var legendColors = [];
    for (var i = 0; i < data.length; i++) {
        var lineColor = getRandomColor(i, data.length);
        name.push(data[i].name);
        legendColors.push(lineColor[0]);
        var model = {
            name: data[i].name,
            type: 'line',
            showSymbol: false,
            smooth: 'true',
            lineStyle: {
                normal: {
                    width: 1,
                    color: lineColor[0]
                }
            },
            areaStyle: {
                normal: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                        offset: 0,
                        color: lineColor[1]
                    }, {
                        offset: 1.0,
                        color: lineColor[2]
                    }], false)
                }
            },
            data: data[i].data
        };
        listModel.push(model);
    }

    var option = {
        title: {
            text:title,
            textStyle:{fontSize:14},
            top:0,
            padding: 0,
            width: 200,
            height:15,
            fontWeight:'normal'
        },
        tooltip: {
            trigger: 'axis',
            confine: true
        },
        grid: {
            left: '2%',
            right: '2%',
            bottom: '5%',
            top: 80,
            containLabel: true
        },
        color:legendColors,
        legend: {
            icon: 'rect',
            data: name,
            top: 15,
            padding: 5,
            itemHeight: 9,
            itemWidth : 9
        },
        xAxis: [{
            type: 'category',
            data: timeList,
            boundaryGap: false
        }],
        yAxis: [{
            // splitNumber : 10,
            type: 'value'
        }],
        series: listModel
    };
    return option;
};

function getRandomColor(i, count) {
    var colorDisk = ['#FF7F50','#9ACD32','#ffb980','#d87a80', '#8d98b3','#97b552','#95706d','#dc69aa', '#07a2a4','#9a7fd1',
        '#588dd5','#f5994e','#c05050','#FF8C00', '#CD5C5C', '#ADFF2F', '#40E0D0',
        '#59678c','#c9ab00','#7eb00a','#6f5553','#c14089','#dd6b66','#759aa0','#e69d87','#8dc1a9','#ea7e53','#eedd78','#73a373','#73b9bc','#7289ab', '#91ca8c','#f49f42'];
    if(i < colorDisk.length){
        var destColor = colorDisk[i];
        var r = parseInt(destColor.substr(1,2),16);
        var g = parseInt(destColor.substr(3,2),16);
        var b = parseInt(destColor.substr(5,2),16);
        //return line_color, top_color, bottom_color
        return ["rgb(" + r + ',' + g + ',' + b + ")", "rgba(" + r + ',' + g + ',' + b + "," + 0.8 / count + ")", "rgba(" + r + ',' + g + ',' + b + ",0.001)"];
    }else{
        var r = Math.floor(Math.random(i) * 256);
        var g = Math.floor(Math.random(i) * 256);
        var b = Math.floor(Math.random(i) * 256);
        //return line_color, top_color, bottom_color
        return ["rgb(" + r + ',' + g + ',' + b + ")", "rgba(" + r + ',' + g + ',' + b + "," + 0.8 / count + ")", "rgba(" + r + ',' + g + ',' + b + ",0.001)"];
    }
}
