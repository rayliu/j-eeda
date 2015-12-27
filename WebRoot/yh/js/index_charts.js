$(document).ready(function() {
    // 路径配置
    require.config({
        paths: {
            echarts: 'http://echarts.baidu.com/build/dist'
        }
    });

    // 使用
    require(
        [
            'echarts',
            'echarts/chart/line',
            'echarts/chart/bar', // 使用柱状图就加载bar模块，按需加载
            'echarts/chart/funnel',
            'echarts/chart/pie'
        ],
        function(ec) {
            // 基于准备好的dom，初始化echarts图表

            // ---------------profit
            var profit_chart = ec.init(document.getElementById('profit_chart'));
            profit_option = {
                tooltip: {
                    trigger: 'axis'
                },
                toolbox: {
                    show: false,
                    y: 'bottom',
                    feature: {
                        mark: {
                            show: true
                        },
                        dataView: {
                            show: true,
                            readOnly: false
                        },
                        magicType: {
                            show: true,
                            type: ['line', 'bar', 'stack', 'tiled']
                        },
                        restore: {
                            show: true
                        },
                        saveAsImage: {
                            show: true
                        }
                    }
                },
                calculable: true,
                legend: {
                    data: ['收入', '成本', '毛利', '毛利率']
                },
                xAxis: [{
                    type: 'category',
                    splitLine: {
                        show: false
                    },
                    data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
                }],
                yAxis: [{
                    type: 'value',
                    name: '金额（元）',
                    position: 'left'
                }, {
                    type: 'value',
                    name: '其他',
                    splitNumber: 10,
                    position: 'right'
                }],
                series: [{
                        name: '收入',
                        type: 'bar',
                        data: [320, 332, 301, 334, 390, 330, 320]
                    }, {
                        name: '成本',
                        type: 'bar',
                        data: [120, 132, 101, 134, 90, 230, 210]
                    }, {
                        name: '毛利',
                        type: 'bar',
                        data: [220, 182, 191, 234, 290, 330, 310]
                    }, {
                        name: '毛利率',
                        type: 'line',
                        data: [8, 10, 4, 10, 16, 16, 70]
                    },

                    {
                        name: '总收入饼图',
                        type: 'pie',
                        tooltip: {
                            trigger: 'item',
                            formatter: '{a} <br/>{b} : {c} ({d}%)'
                        },
                        center: [160, 130],
                        radius: [0, 50],
                        itemStyle: 　{
                            normal: {
                                labelLine: {
                                    length: 20
                                }
                            }
                        },
                        data: [{
                            value: 1048,
                            name: '成本'
                        }, {
                            value: 2510,
                            name: '毛利'
                        }]
                    }
                ]
            };
            profit_chart.setOption(profit_option);



        } // end of function(ec) {
    );
});
