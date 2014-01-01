<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="s" uri="/struts-tags"%> <%@
page import="com.gcrm.util.DateTimeUtil"%> <%@ page language="java"
import="com.gcrm.domain.User"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf8" />
<link rel="stylesheet" type="text/css" href="../../css/global.css" />
<link rel="stylesheet" type="text/css" media="screen"
 href="../../css/redmond/jquery-ui-1.9.2.custom.css" />
<link rel="stylesheet" type="text/css" media="screen"
 href="../../css/ui.multiselect.css" />
<link rel="stylesheet" type="text/css" media="screen"
 href="../../css/ui.jqgrid.css" />
<link rel="stylesheet" type="text/css"
 href="../../themes/default/easyui.css" />
<link rel="stylesheet" type="text/css" href="../../themes/icon.css" />

<script type="text/javascript" src="../../js/jquery-1.8.3.min.js"></script>
<script type="text/javascript"
 src="../../js/datagrid-<%=(String)session.getAttribute("locale")%>.js"></script>
<script type="text/javascript" src="../../js/global.js"></script>
<script type="text/javascript"
 src="../../js/jquery-ui-1.9.2.custom.min.js"></script>
<script type="text/javascript" src="../../js/ui.multiselect.js"></script>
<script type="text/javascript" src="../../js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="../../js/jquery.easyui.min.js"></script>
<script type="text/javascript"
 src="../../js/i18n/grid.locale-<%=(String)session.getAttribute("locale")%>.js"></script>
<script type="text/javascript"
 src="../../js/locale/easyui-lang-<%=(String)session.getAttribute("locale")%>.js"></script>

<script type="text/javascript">
    $(document).ready(function(){
	  $("#delete").click(function() {	
		  many_deleterow("/crm/deleteAccount.action?seleteIDs=");
	  });	

	  $("#massUpdate").click(function() {	
		  many_massUpdaterow("/crm/editAccount.action?seleteIDs=");
	  });
	  
	  $("#export").click(function() {	
		  many_exportrow("/crm/exportAccount.action?seleteIDs=");
	  });		  
	  
	  $("#copy").click(function() {	
		  many_copyrow("/crm/copyAccount.action?seleteIDs=");
	  });

	  var mygrid = jQuery("#grid").jqGrid({
			datatype: "json", 
			url:'listAccountFull.action', 
			mtype: 'POST',
			height: "auto",
		   	colNames:['<s:text name="entity.id.label" />','<s:text name="entity.name.label" />',
			  		'<s:text name="entity.office_phone.label" />','<s:text name="entity.email.label" />',
		  		   	'<s:text name="entity.billing_street.label" />',
		  		   	'<s:text name="entity.billing_city.label" />',
		  		   	'<s:text name="entity.billing_state.label" />',
		  		   	'<s:text name="entity.billing_country.label" />',
		  		   	'<s:text name="entity.billing_postal_code.label" />',
		  		    '<s:text name="entity.assigned_to.label" />','<s:text name="entity.website.label" />',
		  		    '<s:text name="entity.fax.label" />',
		  		   	'<s:text name="entity.shipping_street.label" />',
		  		    '<s:text name="entity.shipping_city.label" />',
		  		   	'<s:text name="entity.shipping_state.label" />',
		  		    '<s:text name="entity.shipping_country.label" />',
		  		   	'<s:text name="entity.shipping_postal_code.label" />',
		  		    '<s:text name="account.sic_code.label" />','<s:text name="account.ticket_symbol.label" />',
		  		    '<s:text name="account.manager.label" />', '<s:text name="entity.createdBy.label" />',
		  		    '<s:text name="entity.updatedBy.label" />','<s:text name="entity.createdOn.label" />',
		  		    '<s:text name="entity.updatedOn.label" />'],
		   	colModel:[
		   		{name:'id',index:'id', width:120, key: true,sorttype:"int",resizable:true, hidden:true},
		   		{name:'name',index:'name', width:150, resizable:true, formatter:urlFmatter},
		   		{name:'office_phone',index:'office_phone', width:150, resizable:true, formatter:urlFmatter},
		   		{name:'email',index:'email', width:150, resizable:true, formatter:urlFmatter},
		   		{name:'bill_street',index:'bill_street', width:150, resizable:true, formatter:urlFmatter},
		   		{name:'bill_city',index:'bill_city', width:150, resizable:true, formatter:urlFmatter},
		   		{name:'bill_state',index:'bill_state', width:150, resizable:true, formatter:urlFmatter},		   		
		   		{name:'bill_country',index:'bill_country', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'bill_postal_code',index:'bill_postal_code', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'assigned_to.name',index:'assigned_to.name', width:150, resizable:true, formatter:urlFmatter},
		   		{name:'website',index:'website', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'fax',index:'fax', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'ship_street',index:'ship_street', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'ship_city',index:'ship_city', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'ship_state',index:'ship_state', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'ship_country',index:'ship_country', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'ship_postal_code',index:'ship_postal_code', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'sic_code',index:'sic_code', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'ticket_symbol',index:'ticket_symbol', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'manager.name',index:'manager.name', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'created_by.name',index:'created_by.name', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'updated_by.name',index:'updated_by.name', width:150, resizable:true, hidden:true, formatter:urlFmatter},
		   		{name:'created_on',index:'created_on', width:150, resizable:true, hidden:true, formatter:urlFmatter, stype:'select', 
			   		editoptions:{value:"<%=DateTimeUtil.getSelectOptions()%>"}},
		   		{name:'updated_on',index:'updated_on', width:150, resizable:true, hidden:true, formatter:urlFmatter, stype:'select', 
				   		editoptions:{value:"<%=DateTimeUtil.getSelectOptions()%>"}}   		
		   	],
		   	pager: 'pager', 
		   	imgpath: 'image/images', 
		   	rowNum:15, 
		   	viewrecords: true, 
		   	rowList:[15,50,100], 
		   	multiselect: true, 
		   	caption: "<s:text name='title.grid.accounts'/>"
		});
		function urlFmatter (cellvalue, options, rowObject)
		{  
		   var par='<%=((User)session.getAttribute("loginUser")).getUpdate_account()%>';
		   if (par == 1){
		     new_format_value = "<a href='editAccount.action?id=" + rowObject[0] + "'>" + cellvalue + "</a>";
		   }else {
			 new_format_value = cellvalue;
		   }
		   return new_format_value
		};	
		
		jQuery("#grid").jqGrid('navGrid','#pager',{del:false,add:false,edit:false,refresh:false,search:false});
		jQuery("#grid").jqGrid('navButtonAdd',"#pager",{caption:"",title:"<s:text name='grid.button.toggle.title'/>", buttonicon :'ui-icon-pin-s',
			onClickButton:function(){
				mygrid[0].toggleToolbar();
			} 
		});		
		jQuery("#grid").jqGrid('navButtonAdd',"#pager",{caption:"",title:"<s:text name='grid.button.advancedSearch.title'/>",buttonicon :'ui-icon-search',
			onClickButton:function(){
				jQuery("#grid").jqGrid('searchGrid', {multipleSearch:true} );
			} 
		});	
		jQuery("#grid").jqGrid('navButtonAdd',"#pager",{caption:"",title:"<s:text name='grid.button.clear.title'/>",buttonicon :'ui-icon-refresh',
			onClickButton:function(){
				var postdata = jQuery("#grid").jqGrid('getGridParam','postData');
				postdata.filters = "";
				mygrid[0].clearToolbar()
			} 
		});
		jQuery("#grid").jqGrid('navButtonAdd','#pager',{caption: "",title: "<s:text name='grid.button.reorderColumn.title'/>",
		    onClickButton : function (){
		    	jQuery("#grid").jqGrid('columnChooser');
		    }
		});		
		jQuery("#grid").jqGrid('filterToolbar');		
	});	    
  </script>
</head>
<body>
 <div id="page-wrap">
  <s:include value="../header.jsp" />
  <s:include value="../menu.jsp" />
  <div id="feature">
   <s:include value="../navigation.jsp" />
   <div id="shortcuts" class="headerList">
    <b style="white-space: nowrap; color: #444;"><s:text
      name="title.action" />:&nbsp;&nbsp;</b> <span> <s:if
      test="#session.loginUser.create_account == 1">
      <span style="white-space: nowrap;"> <a
       href="editAccount.action" class="easyui-linkbutton"
       iconCls="icon-add" plain="true"><s:text
         name="action.createAccount" /></a>
      </span>
     </s:if> <s:if test="#session.loginUser.delete_account == 1">
      <span style="white-space: nowrap;"> <a id="delete" href="#"
       class="easyui-linkbutton" iconCls="icon-remove" plain="true"><s:text
         name="action.deleteAccount" /></a>
      </span>
     </s:if> <span style="white-space: nowrap;"><a
      href="javascript:void(0)" id="mtmt" class="easyui-menubutton"
      data-options="menu:'#mtm1',iconCls:'icon-more'"><s:text
        name='menu.toolbar.more.title' /></a>
      <div id="mtm1" style="width: 150px;">
       <s:if
        test="#session.loginUser.create_account == 1 || #session.loginUser.update_account == 1">
        <div data-options="iconCls:'icon-import'"
         onClick="openwindow('/crm/upload.jsp?entityName=Account&namespace=crm&title=' + '<s:text name="title.import.account" />')">
                  <s:text name='menu.item.import.title' />
      </div> </s:if> <s:if test="#session.loginUser.view_account == 1">
       <div data-options="iconCls:'icon-export'" id="export">
        <s:text name='menu.item.export.title' />
       </div>
      </s:if> <s:if test="#session.loginUser.update_account == 1">
       <div data-options="iconCls:'icon-update'" id="massUpdate">
        <s:text name='menu.item.massupdate.title' />
       </div>
      </s:if> <s:if test="#session.loginUser.create_account == 1">
       <div data-options="iconCls:'icon-copy'" id="copy">
        <s:text name='menu.item.copy.title' />
       </div>
      </s:if>
   </div>
   </span> </span>
  </div>
  <div id="feature-title">
   <h2>
    <s:text name="title.listAccount" />
   </h2>
  </div>
  <div id="feature-content">
   <table style="" cellspacing="10" cellpadding="0" width="100%">
    <s:if test="hasActionErrors()">
     <tr>
      <td align="left" colspan="4"><font color="red"><s:actionerror /></font></td>
     </tr>
    </s:if>
   </table>
   <table id="grid" class="scroll" cellpadding="0" cellspacing="0"></table>
   <div id="pager" class="scroll"></div>
   <div id="filter" style="margin-left: 30%; display: none">
    <s:text name="title.listAccount" />
   </div>
  </div>
 </div>
 <s:include value="../footer.jsp" />
 </div>
</body>
</html>