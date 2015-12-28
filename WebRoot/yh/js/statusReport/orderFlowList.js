
$(document).ready(function() {
	document.title = '单据流转查询 | '+document.title;

    $('#menu_report').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));

	  //datatable, 动态处理
    var dataTable = $('#eeda-table').DataTable({
        "processing": true,
        "searching": false,
        "paging": true, //翻页功能
        "pageLength": 10,
        "aLengthMenu": [ [10 ,25 ,50 ,100 ,9999999], [10 ,25 ,50 ,100, "All"]],
        "scrollX": true,
        "scrollY": "500px",
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "serverSide": true,
        "deferLoading": 0, //初次不查数据
        "columns": [
            { "data": "TRANSFER_ORDER_NO", "width": "10%",
                "render": function ( data, type, full, meta ) {
                	var id = full.TRANSFER_ORDER_NO.substring(0, full.TRANSFER_ORDER_NO.indexOf(':'));
                	var order_no = full.TRANSFER_ORDER_NO.substring((full.TRANSFER_ORDER_NO.indexOf(':')+1),full.TRANSFER_ORDER_NO.indexOf('-'));
                	var status = full.TRANSFER_ORDER_NO.substring(full.TRANSFER_ORDER_NO.indexOf('-'),50);
                	return eeda.getUrlByNo(id,order_no)+status;
                }
            },
            { "data": "CUSTOMER_NAME","width": "10%"},
            { "data": "PICKUP_ORDER_NO", "width": "10%",
                "render": function ( data, type, full, meta ) {
                	var array = [];
                	var re = "";
                	if(full.PICKUP_ORDER_NO != null){
                		array = full.PICKUP_ORDER_NO.split('<br/>');
                    	for (var i = 0; i < array.length; i++) {
                    		var id = array[i].substring(0, array[i].indexOf(':'));
                        	var order_no = array[i].substring((array[i].indexOf(':')+1),array[i].indexOf('-'));
                        	var status = array[i].substring(array[i].indexOf('-'),50);
                        	re += eeda.getUrlByNo(id,order_no)+status+'<br/>';
    					}
                	}
                	return re;

                }
            },
            { "data": "DEPART_ORDER_NO", "width": "10%",
                "render": function ( data, type, full, meta ) {
                	var array = [];
                	var re = "";
                	if(full.DEPART_ORDER_NO != null){
                		array = full.DEPART_ORDER_NO.split('<br/>');
                    	for (var i = 0; i < array.length; i++) {
                    		var id = array[i].substring(0, array[i].indexOf(':'));
                        	var order_no = array[i].substring((array[i].indexOf(':')+1),array[i].indexOf('-'));
                        	var status = array[i].substring(array[i].indexOf('-'),50);
                        	re += eeda.getUrlByNo(id,order_no)+status+'<br/>';
    					}
                	}
                	return re;
                }
            },
            { "data": "DELIVERY_ORDER_NO", "width": "10%",
                "render": function ( data, type, full, meta ) {
                	var array = [];
                	var re = "";
                	if(full.DELIVERY_ORDER_NO != null){
                		array = full.DELIVERY_ORDER_NO.split('<br/>');
                    	for (var i = 0; i < array.length; i++) {
                    		var id = array[i].substring(0, array[i].indexOf(':'));
                        	var order_no = array[i].substring((array[i].indexOf(':')+1),array[i].indexOf('+'));
                        	var status = array[i].substring(array[i].indexOf('+'),50);
                        	re += eeda.getUrlByNo(id,order_no)+status+'<br/>';
    					}
                	}
                	return re;
                }
            },
            { "data": "RETURN_ORDER_NO", "width": "10%",
                "render": function ( data, type, full, meta ) {
                	var array = [];
                	var re = "";
                	if(full.RETURN_ORDER_NO != null){
                		array = full.RETURN_ORDER_NO.split('<br/>');
                    	for (var i = 0; i < array.length; i++) {
                    		var id = array[i].substring(0, array[i].indexOf(':'));
                        	var order_no = array[i].substring((array[i].indexOf(':')+1),array[i].indexOf('-'));
                        	var status = array[i].substring(array[i].indexOf('-'),50);
                        	re += eeda.getUrlByNo(id,order_no)+status+'<br/>';
    					}
                	}
                	return re;
                }
            },
            { "data": "CHARGE_ORDER_NO", "width": "10%",
                "render": function ( data, type, full, meta ) {
                	var array = [];
                	var re = "";
                	if(full.CHARGE_ORDER_NO != null){
                		array = full.CHARGE_ORDER_NO.split('<br/>');
                    	for (var i = 0; i < array.length; i++) {
                    		var id = array[i].substring(0, array[i].indexOf(':'));
                        	var order_no = array[i].substring((array[i].indexOf(':')+1),array[i].indexOf('-'));
                        	var status = array[i].substring(array[i].indexOf('-'),50);
                        	re += eeda.getUrlByNo(id,order_no)+status+'<br/>';
    					}
                	}
                	return re;
                }
            }, 
            { "data": "COST_ORDER_NO1", "width": "10%",
                "render": function ( data, type, full, meta ) {
                	var array = [];
                	var re = "";
                	if(full.COST_ORDER_NO1 != null){
                		array = full.COST_ORDER_NO1.split('<br/>');
                    	for (var i = 0; i < array.length; i++) {
                    		var id = array[i].substring(0, array[i].indexOf(':'));
                        	var order_no = array[i].substring((array[i].indexOf(':')+1),array[i].indexOf('-'));
                        	var status = array[i].substring(array[i].indexOf('-'),50);
                        	re += eeda.getUrlByNo(id,order_no)+status+'<br/>';
    					}
                	}
                	return re;
                }
            }, 
            { "data": "COST_ORDER_NO2", "width": "10%",
                "render": function ( data, type, full, meta ) {
                	var array = [];
                	var re = "";
                	if(full.COST_ORDER_NO2 != null){
                		array = full.COST_ORDER_NO2.split('<br/>');
                    	for (var i = 0; i < array.length; i++) {
                    		var id = array[i].substring(0, array[i].indexOf(':'));
                        	var order_no = array[i].substring((array[i].indexOf(':')+1),array[i].indexOf('-'));
                        	var status = array[i].substring(array[i].indexOf('-'),50);
                        	re += eeda.getUrlByNo(id,order_no)+status+'<br/>';
    					}
                	}
                	return re;
                }
            }, 
            { "data": "COST_ORDER_NO3", "width": "10%",
                "render": function ( data, type, full, meta ) {
                	var array = [];
                	var re = "";
                	if(full.COST_ORDER_NO3 != null){
                		array = full.COST_ORDER_NO3.split('<br/>');
                    	for (var i = 0; i < array.length; i++) {
                    		var id = array[i].substring(0, array[i].indexOf(':'));
                        	var order_no = array[i].substring((array[i].indexOf(':')+1),array[i].indexOf('-'));
                        	var status = array[i].substring(array[i].indexOf('-'),50);
                        	re += eeda.getUrlByNo(id,order_no)+status+'<br/>';
    					}
                	}
                	return re;
                }
            }
        ]
    });

    
    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    });

   var searchData=function(){
        var transfer_order_no = $("#transfer_order_no").val();
        //var customer_id=$("#customer_id").val();
        var pickup_order_no=$("#pickup_order_no").val();
        var depart_order_no = $("#depart_order_no").val();
        var delivery_order_no = $('#delivery_order_no').val();
        var return_order_no = $('#return_order_no').val();
        var charge_order_no = $('#charge_order_no').val();
        var cost_order_no = $("#cost_order_no").val();
        var sign_no = $("#sign_no").val();
        var serial_no = $("#serial_no").val();
        
        /*
            查询规则：参数对应DB字段名
            *_no like
            *_id =
            *_status =
            时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/statusReport/orderFlowList?transfer_order_no="+transfer_order_no
             +"&pickup_order_no="+pickup_order_no
             +"&delivery_order_no="+delivery_order_no
             +"&depart_order_no="+depart_order_no
             +"&return_order_no="+return_order_no
             +"&charge_order_no="+charge_order_no
             +"&cost_order_no="+cost_order_no
             +"&sign_no="+sign_no
             +"&serial_no="+serial_no;

        dataTable.ajax.url(url).load();
    };
    

} );