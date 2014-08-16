
$(document).ready(function() {

    $('#menu_charge').addClass('active').find('ul').addClass('in');
   
	//datatable, 动态处理
    $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/chargeAcceptOrder/list",
        "aoColumns": [   
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"BLLING_ORDER_NO", "sWidth": "150px"},
            {"mDataProp":"COMPANY_NAME", "sWidth": "100px"},
            {"mDataProp":"CUSTOMER_TYPE", "bVisible": false},
            {"mDataProp":"STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },
            {"mDataProp":"TRANSFER_ORDER_NO"},
            {"mDataProp":"TRANSFER_ORDER_NO"},
            {"mDataProp":"DELIVERY_ORDER_NO"},            
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"REMARK"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {
                	/*if(obj.aData.CARGO_NATURE=='cargo'){
                		obj.aData.CARGO_NATURE = '普通货品';
                	}else if(obj.aData.CARGO_NATURE=='damageCargo'){
                		return '损坏货品';
                	}*/
                    return	"<a class='btn btn-success' href='"+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "收款"+
                            "</a>";
                }
            }                         
        ]      
    });	
} );