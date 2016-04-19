$(document).ready(function() {
    var revenue = [];//收入
    var cost=[];//成本
    var profit=[];//毛利
    var profit_rate=[];//毛利率
    var sum_profit=0;//总毛利
    var sum_cost=0;//总成本
    // 路径配置
    require.config({
        paths: {
            echarts: '/yh/js/plugins/echarts'
        }
    });

  

    var get_charts_option= function(revenue, cost, profit, profit_rate, sum_profit, sum_cost){
        var profit_option = {
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
                data: ['收入', '成本', '毛利', '毛利率(%)']
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
                    name: '百分比',
                    position: 'right',
                    axisLabel : {
                        formatter: '{value} %'
                    }
                }
            ],
            series: [{
                    name: '收入',
                    type: 'line',
                    itemStyle: {
                        normal: {
                            label : {
                                show: true, position: 'top'
                            }
                        }
                    },
                    data: revenue
                }, {
                    name: '毛利率(%)',
                    type: 'line',
                    itemStyle: {
                        normal: {
                            label : {
                                show: true, 
                                position: 'top',
                                formatter: function (params){
                                    return params.value+'%';
                                }
                            }
                        }
                    },
                    yAxisIndex: 1,
                    data: profit_rate
                },{
                    name: '成本',
                    type: 'bar',
                    stack: '总量',
                    data: cost
                }, {
                    name: '毛利',
                    type: 'bar',
                    stack: '总量',
                    data: profit
                },{
                    name: '总收入饼图',
                    type: 'pie',
                    tooltip: {
                        trigger: 'item',
                        formatter: '{a} <br/>{b} : {c} ({d}%)'
                    },
                    center: [560, 80],
                    radius: [0, 35],
                    itemStyle: {
                        normal: {
                            labelLine: {
                                length: 10
                            }
                        }
                    },
                    data: [{
                        value: sum_cost,
                        name: '成本'
                    }, {
                        value: sum_profit,
                        name: '毛利'
                    }]
                }
            ]
        };
        return profit_option;
    }

    var profit_chart;
    // 使用
    require([
                'echarts',
                'echarts/chart/line',
                'echarts/chart/bar', // 使用柱状图就加载bar模块，按需加载
                'echarts/chart/funnel',
                'echarts/chart/pie'
            ],
        function(ec) {
            // 基于准备好的dom，初始化echarts图表
            // ---------------profit
            profit_chart = ec.init(document.getElementById('profit_chart'));
            var profit_option = get_charts_option(revenue, cost, profit, profit_rate, sum_profit, sum_cost);
            profit_chart.setOption(profit_option);
        } // end of function(ec) {
    );//end of require

    $("#searchBtn").click(function(){
        refreshData();
    });
    refreshData=function(){
        var customer_id=$("#customer_id").val();
        var years_date = $("#years_date").val();
        $.post('/statusReport/revenueIndex',{years_date:years_date,customer_id:customer_id},function(data){
            if(jQuery.isEmptyObject(data.toString)){
                revenue=data.revenue;
                cost=data.cost;
                profit=data.profit;
                profit_rate=data.profit_rate;
                sum_profit=data.sum_profit;
                sum_cost=data.sum_cost

                var profit_option = get_charts_option(revenue, cost, profit, profit_rate, sum_profit, sum_cost);
                profit_chart.setOption(profit_option);
            }
        })
    }
});
