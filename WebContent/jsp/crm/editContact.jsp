<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf8" />
<link rel="stylesheet" type="text/css"
  href="../../themes/default/easyui.css" />
<link rel="stylesheet" type="text/css" href="../../themes/icon.css" />
<link rel="stylesheet" type="text/css" href="../../css/global.css" />

<script type="text/javascript" src="../../js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="../../js/jquery.easyui.min.js"></script>
<script type="text/javascript" src="../../js/global.js"></script>
<script type="text/javascript"
  src="../../js/locale/easyui-lang-<%=(String)session.getAttribute("locale")%>.js"></script>

<script type="text/javascript">
    function save() {
        $("input[name='contact.other_street']").removeAttr("disabled"); 
        $("input[name='contact.other_city']").removeAttr("disabled"); 
        $("input[name='contact.other_state']").removeAttr("disabled"); 
        $("input[name='contact.other_postal_code']").removeAttr("disabled"); 
        $("input[name='contact.other_country']").removeAttr("disabled");        	
        baseSave("Contact");
	}

	function saveClose() {
        $("input[name='contact.other_street']").removeAttr("disabled"); 
        $("input[name='contact.other_city']").removeAttr("disabled"); 
        $("input[name='contact.other_state']").removeAttr("disabled"); 
        $("input[name='contact.other_postal_code']").removeAttr("disabled"); 
        $("input[name='contact.other_country']").removeAttr("disabled");        		
		baseSaveClose("Contact");
	}
	
	function cancel() {
		baseCancel("Contact");
	}

	function primaryMap() {
	    address = $("input[name='contact.primary_country']").val() + " "
				+ $("input[name='contact.primary_state']").val() + " "
				+ $("input[name='contact.primary_city']").val() + " "
				+ $("input[name='contact.primary_street']").val();
	    locale = '<%=(String)session.getAttribute("locale")%>'
	    if (locale == "zh_CN"){
		    window.open("http://ditu.google.com/?ie=UTF8&hl=zh-CN&q=" + address,
				"_blank");
	    } else {
			window.open("https://maps.google.com/maps?q=" + address,
				"_blank");	    	
	    }
	}

	function otherMap() {
	    address = $("input[name='contact.other_country']").val() + " "
				+ $("input[name='contact.other_state']").val() + " "
				+ $("input[name='contact.other_city']").val() + " "
				+ $("input[name='contact.other_street']").val();
	    locale = '<%=(String)session.getAttribute("locale")%>'
	    if (locale == "zh_CN"){
		    window.open("http://ditu.google.com/?ie=UTF8&hl=zh-CN&q=" + address,
				"_blank");
	    } else {
			window.open("https://maps.google.com/maps?q=" + address,
				"_blank");	    	
	    }
	}	
	
	function copyAddress(){
		if ($('#copy_checkbox').attr('checked')) { 
			$("input[name='contact.other_street']").attr("value",$("input[name='contact.primary_street']").val());	
			$("input[name='contact.other_street']").attr("disabled","disabled"); 
			$("input[name='contact.other_city']").attr("value",$("input[name='contact.primary_city']").val());
			$("input[name='contact.other_city']").attr("disabled","disabled");
			$("input[name='contact.other_state']").attr("value",$("input[name='contact.primary_state']").val());
			$("input[name='contact.other_state']").attr("disabled","disabled");
			$("input[name='contact.other_postal_code']").attr("value",$("input[name='contact.primary_postal_code']").val());
			$("input[name='contact.other_postal_code']").attr("disabled","disabled");
			$("input[name='contact.other_country']").attr("value",$("input[name='contact.primary_country']").val());
			$("input[name='contact.other_country']").attr("disabled","disabled");
		} else {
			$("input[name='contact.other_street']").removeAttr("disabled"); 
			$("input[name='contact.other_city']").removeAttr("disabled"); 
			$("input[name='contact.other_state']").removeAttr("disabled"); 
			$("input[name='contact.other_postal_code']").removeAttr("disabled"); 
			$("input[name='contact.other_country']").removeAttr("disabled"); 	
		}	
    }
	  
	$(document).ready(function(){
		$('#reportToID').combogrid('setValue', '<s:property value="reportToID"/>');	
		$('#reportToID').combogrid('setText', '<s:property value="reportToText"/>');
		$('#campaignID').combogrid('setValue', '<s:property value="campaignID"/>');
		$('#campaignID').combogrid('setText', '<s:property value="campaignText"/>');
		$('#ownerID').combogrid('setValue', '<s:property value="ownerID"/>');
		$('#ownerID').combogrid('setText', '<s:property value="ownerText"/>');			
		$('#assignedToID').combogrid('setValue', '<s:property value="assignedToID"/>');
		$('#assignedToID').combogrid('setText', '<s:property value="assignedToText"/>');
		$('#accountID').combogrid({    
		    onChange : function(n,o){
		      var officePhone = $('#office_phone').val();
              var primaryAddress = $('#primary_street').val();
              var primaryCity = $('#primary_city').val();
              var primaryState = $('#primary_state').val();
              var primaryPostalCode = $('#primary_postal_code').val();
              var primaryCountry = $('#primary_country').val(); 	      		      
		      var g = $('#accountID').combogrid('grid');	// get datagrid object
		      var r = g.datagrid('getSelected');	// get the selected row
              if (r==null){
            	  return;
              }
		      var billStreet = r.bill_street;
		      if (typeof(billStreet) == "undefined"){billStreet = '';}		      
		      var billCity = r.bill_city;
		      if (typeof(billCity) == "undefined"){billCity = '';}	
		      var billCountry = r.bill_country;
		      if (typeof(billCountry) == "undefined"){billCountry = '';}
		      var billState = r.bill_state;
		      if (typeof(billState) == "undefined"){billState = '';}
		      var billPostalCode = r.bill_postal_code;
		      if (typeof(billPostalCode) == "undefined"){billPostalCode = '';}
		      var accountOfficePhone = r.office_phone;
		      if (typeof(accountOfficePhone) == "undefined"){accountOfficePhone = '';}
		      if (officePhone != '' || primaryAddress != '' || primaryCity != '' || primaryState != '' || primaryPostalCode != '' || primaryCountry != ''){
		    	$.messager.confirm('Confirm', '<s:text name="message.confirm.contact"/><br/>' +
		    			'<br/><s:text name="entity.primary_street.label"/>:' + billStreet +
		    			'<br/><s:text name="entity.primary_city.label"/>:' + billCity +
		    			'<br/><s:text name="entity.primary_state.label"/>:' + billState +
		    			'<br/><s:text name="entity.primary_postal_code.label"/>:' + billPostalCode +
		    			'<br/><s:text name="entity.primary_country.label"/>:' + billCountry +
		    			'<br/><s:text name="entity.office_phone.label"/>:' + accountOfficePhone +
		    			"", function(r){
		    		          if (r){
		    			        $('#primary_street').val(billStreet);
		    			        $('#primary_city').val(billCity);
		    			        $('#primary_state').val(billState);
		    			        $('#primary_postal_code').val(billPostalCode);
		    			        $('#primary_country').val(billCountry);
		    			        $('#office_phone').val(accountOfficePhone);		    			        
		    		        }
		    	});	
		     } else {
			        $('#primary_street').val(billStreet);
			        $('#primary_city').val(billCity);
			        $('#primary_state').val(billState);
			        $('#primary_postal_code').val(billPostalCode);
			        $('#primary_country').val(billCountry);
			        $('#office_phone').val(accountOfficePhone);	
		     }	
		   }  
		});		
		$('#accountID').combogrid('setValue', '<s:property value="accountID"/>');
		$('#accountID').combogrid('setText', '<s:property value="accountText"/>');
		if ($("#seleteIDs").val()!= ""){
			  $("input:checkbox[name=massUpdate]").css("display",'block');
			  $('#tt').tabs('close', '<s:text name='tab.relations'/>');
		}
		if ($("#id").val() == ""){
			  $('#tt').tabs('close', '<s:text name='tab.relations'/>');
			  if ($("#seleteIDs").val() == ""){
				     $("#addObjectForm").form('validate');
			  }
		}
		if ($("#saveFlag").val() == "true"){
			$.messager.show({  
	          title:'<s:text name="message.title" />',  
	          msg:'<s:text name="message.save" />',  
	          timeout:5000,  
	          showType:'slide'  
	      });  
		  $("#saveFlag").val("");
	    }		
		
		$('#tt').tabs({
			onSelect:function(title){
		        if (title == "<s:text name='tab.relations'/>"){
					 var params = {
					   id : $("#id").val()
					 };
					  $.ajax({
					    type: "POST",
					    url: "getContactRelationCounts.action",
					    data: params,
					    dataType:"text", 
					    success: function(json){  
					      var obj = $.parseJSON(json);  
					      $("#opportunityNumber").html(obj.opportunityNumber); 
					      $("#leadNumber").html(obj.leadNumber); 
					      $("#documentNumber").html(obj.documentNumber); 
					      $("#taskNumber").html(obj.taskNumber); 
					      $("#caseNumber").html(obj.caseNumber); 
					    },
					    error: function(json){
					      alert("json=" + json);
					      return false;
					    }
					  });				        	
		        }
		    }
		});
		
	})
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
            name="title.action" />:&nbsp;&nbsp;</b> <span> <span
          style="white-space: nowrap;"> <a id="save_accept_btn"
            href="#" class="easyui-linkbutton"
            iconCls="icon-save-accept" onclick="save()" plain="true"><s:text
                name="button.save" /></a>
        </span> <span> <span style="white-space: nowrap;"> <a
              id="save_go_btn" href="#" class="easyui-linkbutton"
              iconCls="icon-save-go" onclick="saveClose()" plain="true"><s:text
                  name="button.saveClose" /></a>
          </span> <span style="white-space: nowrap;"> <a id="cancel_btn"
              href="#" class="easyui-linkbutton" iconCls="icon-cancel"
              onclick="cancel()" plain="true"><s:text
                  name="button.cancel" /></a>
          </span> <s:if test="contact!=null && contact.id!=null">
              <span style="white-space: nowrap;"><a
                href="javascript:void(0)" id="mtmt"
                class="easyui-menubutton"
                data-options="menu:'#mtm1',iconCls:'icon-more'"><s:text
                    name='menu.toolbar.more.title' /></a>
                <div id="mtm1" style="width: 150px;">
                  <div data-options="iconCls:'icon-import'"
                    onClick="openwindow2('/crm/showChangeLogPage.action?entity=Contact&recordID=' + '<s:property value="contact.id" />',750,500)">
                    <s:text name='menu.item.changeLog.title' />
                  </div>
                </div> </span>
            </s:if>
        </span>
      </div>

      <div id="feature-title">
        <s:if test="contact!=null && contact.id!=null">
          <h2>
            <s:text name="title.updateContact" />
          </h2>
        </s:if>
        <s:else>
          <s:if test="seleteIDs!=null && seleteIDs!= ''">
            <h2>
              <s:text name="title.massUpdateContact" />
            </h2>
          </s:if>
          <s:else>
            <h2>
              <s:text name="title.createContact" />
            </h2>
          </s:else>
        </s:else>
      </div>

      <div id="feature-content">
        <s:form id="addObjectForm" validate="true" namespace="/jsp/crm"
          method="post" enctype="multipart/form-data">
          <s:hidden id="id" name="contact.id" value="%{contact.id}" />
          <s:hidden id="saveFlag" name="saveFlag" />
          <s:hidden name="relationKey" id="relationKey"
            value="%{relationKey}" />
          <s:hidden name="relationValue" id="relationValue"
            value="%{relationValue}" />
          <s:hidden id="seleteIDs" name="seleteIDs" value="%{seleteIDs}" />
          <s:hidden id="createdBy" name="createdBy" />
          <s:hidden id="createdOn" name="createdOn" />
          <s:hidden id="updatedBy" name="updatedBy" />
          <s:hidden id="updatedOn" name="updatedOn" />          

          <table style="" cellspacing="10" cellpadding="0" width="100%">
            <s:actionerror />
            <s:if test="hasFieldErrors()">
              <tr>
                <td align="left" colspan="4"><s:actionerror /> <s:iterator
                    value="fieldErrors" status="st">
                    <s:if test="#st.index  == 0">
                      <s:iterator value="value">
                        <font color="red"> <s:property
                          escape="false" /></font>
                      </s:iterator>
                    </s:if>
                  </s:iterator></td>
              </tr>
            </s:if>
          </table>

          <table style="padding: 10px;" cellspacing="10" cellpadding="0"
            width="100%">
            <tr>
              <td class="td-mass-update"><input id="massUpdate"
                name="massUpdate" type="checkbox" class="massUpdate"
                value="salutation" /></td>
              <td class="td-label"><label class="record-label"><s:text
                    name="menu.salutation.title"></s:text>：</label></td>
              <td class="td-value"><s:select name="salutationID"
                  list="salutations" listKey="id" listValue="label"
                  cssClass="record-value" /></td>
              <td class="td-mass-update"><input id="massUpdate"
                name="massUpdate" type="checkbox" class="massUpdate"
                value="first_name" /></td>
              <td class="td-label"><label class="record-label"><s:text
                    name="entity.first_name.label"></s:text>：</label></td>
              <td class="td-value"><s:textfield
                  name="contact.first_name" cssClass="record-value" /></td>
            </tr>

            <tr>
              <td class="td-mass-update"><input id="massUpdate"
                name="massUpdate" type="checkbox" class="massUpdate"
                value="last_name" /></td>
              <td class="td-label"><label class="record-label"><s:text
                    name="entity.last_name.label"></s:text>：</label></td>
              <td class="td-value"><input name="contact.last_name"
                class="easyui-validatebox record-value"
                data-options="required:true"
                value="<s:property value="contact.last_name" />" /></td>
              <td class="td-mass-update"><input id="massUpdate"
                name="massUpdate" type="checkbox" class="massUpdate"
                value="title" /></td>
              <td class="td-label"><label class="record-label"><s:text
                    name="entity.title.label"></s:text>：</label></td>
              <td class="td-value"><s:textfield
                  name="contact.title" cssClass="record-value" /></td>
            </tr>

            <tr>
              <td class="td-mass-update"><input id="massUpdate"
                name="massUpdate" type="checkbox" class="massUpdate"
                value="account" /></td>
              <td class="td-label"><label class="record-label"><s:text
                    name="entity.account.label"></s:text>：</label></td>
              <td class="td-value"><select id="accountID"
                class="easyui-combogrid record-value" name="accountID"
                style="width: 180px;"
                data-options="  
				            panelWidth:520, 
				            idField:'id',  
				            textField:'name',  
				            url:'listAccount.action',
				            mode:'remote',
				            loadMsg: '<s:text name="datagrid.loading" />',
				            pagination : true,
				            pageSize: 10,
				            pageList: [10,30,50],
						    fit: true,
				            columns:[[  
					                {field:'id',title:'<s:text name="entity.id.label" />',width:60},  
					                {field:'name',title:'<s:text name="entity.name.label" />',width:100},  
					                {field:'office_phone',title:'<s:text name="entity.office_phone.label" />',width:120},  
					                {field:'email',title:'<s:text name="entity.email.label" />',width:100},
					                {field:'bill_street',title:'<s:text name="entity.billing_street.label" />',width:100},
                                    {field:'bill_city',title:'<s:text name="entity.billing_city.label" />',width:100},
                                    {field:'bill_state',title:'<s:text name="entity.billing_state.label" />',width:100},
                                    {field:'bill_country',title:'<s:text name="entity.billing_country.label" />',width:100},
                                    {field:'bill_postal_code',title:'<s:text name="entity.billing_postal_code.label" />',width:100},
                                    {field:'assigned_to.name',title:'<s:text name="entity.assigned_to.label" />',width:100}
				            ]]  
				        ">
              </select></td>
              <td class="td-mass-update"><input id="massUpdate"
                name="massUpdate" type="checkbox" class="massUpdate"
                value="owner" /></td>
              <td class="td-label"><label class="record-label"><s:text
                    name="entity.owner.label"></s:text>：</label></td>
              <td class="td-value"><select id="ownerID"
                class="easyui-combogrid record-value" name="ownerID"
                style="width: 180px;"
                data-options="  
					            panelWidth:520,  
					            idField:'id',  
					            textField:'name',  
					            url:'<s:url action="listUser" namespace="/jsp/system"/>',
		                        loadMsg: '<s:text name="datagrid.loading" />',
		                        pagination : true,
		                        pageSize: 10,
		                        pageList: [10,30,50],
				                fit: true,
					            mode:'remote',
					            columns:[[  
					                {field:'id',title:'<s:text name="entity.id.label" />',width:60},  
					                {field:'name',title:'<s:text name="entity.name.label" />',width:100},  
					                {field:'title',title:'<s:text name="entity.title.label" />',width:120},  
					                {field:'department',title:'<s:text name="entity.department.label" />',width:100},
					                {field:'status.name',title:'<s:text name="entity.status.label" />',width:100}   
					            ]]  
					        ">
              </select></td>
            </tr>
          </table>

          <div id="tt" class="easyui-tabs">
            <div title="<s:text name='tab.overview'/>"
              style="padding: 10px;">
              <div class="section-header">
                <span><s:text name="span.contact" /></span>
              </div>
              <table style="" cellspacing="10" cellpadding="0"
                width="100%">
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="email" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.email.label"></s:text>：</label></td>
                  <td class="td-value"><input name="contact.email"
                    class="easyui-validatebox record-value"
                    data-options="validType:'email'"
                    value="<s:property value="contact.email" />" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="office_phone" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.office_phone.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      id="office_phone" name="contact.office_phone"
                      cssClass="record-value" /></td>
                </tr>
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="website" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.website.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.website" cssClass="record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="fax" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.fax.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.fax" cssClass="record-value" /></td>
                </tr>
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="mobile" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.mobile.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.mobile" cssClass="record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="skype_id" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="contact.skype_id.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.skype_id" cssClass="record-value" /></td>
                </tr>
              </table>

              <div class="section-header">
                <span><s:text name="span.primary_address" /> (<a
                  href="#" onclick="primaryMap()"><s:text
                      name="link.map" /></a>)</span>
              </div>
              <table style="" cellspacing="10" cellpadding="0"
                width="100%">
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="primary_street" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.street.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      id="primary_street" name="contact.primary_street"
                      cssClass="record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="primary_city" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.city.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      id="primary_city" name="contact.primary_city"
                      cssClass="record-value" /></td>
                </tr>

                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="primary_state" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.state.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      id="primary_state" name="contact.primary_state"
                      cssClass="record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="primary_postal_code" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.postal_code.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      id="primary_postal_code"
                      name="contact.primary_postal_code"
                      cssClass="record-value" /></td>
                </tr>

                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="primary_country" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.country.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      id="primary_country"
                      name="contact.primary_country"
                      cssClass="record-value" /></td>
                  <td class="td-mass-update"></td>
                  <td class="td-label"></td>
                  <td class="td-value"></td>
                </tr>
              </table>

              <div class="section-header">
                <span> <s:text name="span.other_address" /> (<a
                  href="#" onclick="otherMap()"><s:text
                      name="link.map" /></a>)
                </span>
              </div>
              <table style="" cellspacing="10" cellpadding="0"
                width="100%">
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="other_street" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.street.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.other_street"
                      cssClass="record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="other_city" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.city.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.other_city" cssClass="record-value" /></td>
                </tr>

                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="other_state" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.state.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.other_state" cssClass="record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="other_postal_code" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.postal_code.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.other_postal_code"
                      cssClass="record-value" /></td>
                </tr>

                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="other_country" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.country.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.other_country"
                      cssClass="record-value" /></td>
                  <td class="td-mass-update"></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="contact.copyAddress.label" />：</label></td>
                  <td class="td-value"><input id="copy_checkbox"
                    name="copy_checkbox" type="checkbox"
                    class="record-value" onclick="copyAddress();" /></td>
                </tr>
              </table>
            </div>
            
            <div title="<s:text name='tab.details'/>"
              style="padding: 10px;">
              <div class="section-header">
                <span><s:text name="span.person_info" /></span>
              </div>              
              <table style="" cellspacing="10" cellpadding="0"
                width="100%">
                <tr>
                  <td class="td-mass-update"></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.picture.label"></s:text>：</label></td>
                  <td class="td-value">
                    <s:if test="contact!=null && contact.id!=null && contact.fileName !=null">
                      <img width="70px" height="70px" src="<s:url value="downloadContact.action?id=%{contact.id}"/>"/>
                    </s:if>
                    <s:else>
                     <img width="70px" height="70px" src="<s:url value="/images/people.png"/>"/>
                    </s:else>
                    <s:file name="upload"/>
                  </td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="origo" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.origo.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.origo" cssClass="record-value" /></td>
                </tr>

                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="height" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.height.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.height" cssClass="easyui-numberbox record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="weight" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.weight.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.weight"
                      cssClass="easyui-numberbox record-value" /></td>
                </tr>

                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="relationship" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.relationship.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.relationship"
                      cssClass="record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="character" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.character.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.character"
                      cssClass="record-value" /></td>
                </tr>
                
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="interest" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.interest.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.interest"
                      cssClass="record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="taboo" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.taboo.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.taboo"
                      cssClass="record-value" /></td>
                </tr>  
                
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="religious" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.religious.label"></s:text>：</label></td>
                  <td class="td-value"><s:select name="religiousID"
                      list="religiouses" listKey="id" listValue="label"
                      cssClass="record-value" cssStyle="width:140px;"/></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="habit" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.habit.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.habit"
                      cssClass="record-value" /></td>
                </tr>
                
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="diet" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.diet.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.diet"
                      cssClass="record-value" /></td>
                  <td class="td-mass-update"></td>
                  <td class="td-label"></td>
                  <td class="td-value"></td>
                </tr>                                                  
              </table>              
                          
              <div class="section-header">
                <span><s:text name="span.other_info" /></span>
              </div>
              <table style="" cellspacing="10" cellpadding="0"
                width="100%">
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="department" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.department.label"></s:text>：</label></td>
                  <td class="td-value"><s:textfield
                      name="contact.department" cssClass="record-value" /></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="leadSource" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="menu.leadSource.title"></s:text>：</label></td>
                  <td class="td-value"><s:select
                      name="leadSourceID" list="leadSources"
                      listKey="id" listValue="label"
                      cssClass="record-value" /></td>
                </tr>

                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="campaign" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.campaign.label"></s:text>：</label></td>
                  <td class="td-value"><select id="campaignID"
                    class="easyui-combogrid record-value"
                    name="campaignID" style="width: 180px;"
                    data-options="  
						            panelWidth:520,  
						            idField:'id',  
						            textField:'name',  
						            url:'listCampaign.action',
						            loadMsg: '<s:text name="datagrid.loading" />',
						            pagination : true,
						            pageSize: 10,
						            pageList: [10,30,50],
						            fit: true,
						            mode:'remote',
						            columns:[[  
						                {field:'id',title:'<s:text name="entity.id.label" />',width:60},  
						                {field:'name',title:'<s:text name="entity.name.label" />',width:100},  
						                {field:'status.name',title:'<s:text name="entity.status.label" />',width:120},  
						                {field:'type.name',title:'<s:text name="entity.type.label" />',width:100},
						                {field:'start_date',title:'<s:text name="entity.start_date.label" />',width:100},  
						                {field:'end_date',title:'<s:text name="entity.end_date.label" />',width:100},
						                {field:'assigned_to.name',title:'<s:text name="entity.assigned_to.label" />',width:100}
						            ]]  
						        ">
                  </select></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="not_call" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.not_call.label"></s:text>：</label></td>
                  <td class="td-value"><s:checkbox
                      id="contact.not_call" name="contact.not_call"
                      cssClass="record-value" /></td>
                </tr>
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="report_to" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="contact.report_to.label"></s:text>：</label></td>
                  <td class="td-value"><select id="reportToID"
                    class="easyui-combogrid record-value"
                    name="reportToID" style="width: 180px;"
                    data-options="  
						            panelWidth:520,  
						            idField:'id',  
						            textField:'name',  
						            url:'listContact.action',
						            loadMsg: '<s:text name="datagrid.loading" />',
						            pagination : true,
						            pageSize: 10,
						            pageList: [10,30,50],
						            fit: true,
						            mode:'remote',
						            columns:[[  
										{field:'id',title:'<s:text name="entity.id.label" />',width:80,align:'center',sortable:'true'},
										{field:'name',title:'<s:text name="entity.name.label" />',width:80,align:'center',sortable:'true'},
										{field:'title',title:'<s:text name="entity.title.label" />',width:80,align:'center',sortable:'true'},
										{field:'account.name',title:'<s:text name="entity.account.label" />',width:80,align:'right',sortable:'true'},
										{field:'email',title:'<s:text name="entity.email.label" />',width:80,align:'center',sortable:'true'},
										{field:'office_phone',title:'<s:text name="entity.office_phone.label" />',width:80,align:'center',sortable:'true'},
										{field:'assigned_to.name',title:'<s:text name="entity.assigned_to.label" />',width:80,align:'center',sortable:'true'}
						            ]]  
						        ">
                  </select></td>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="assigned_to" /></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.assigned_to.label"></s:text>：</label></td>
                  <td class="td-value"><select id="assignedToID"
                    class="easyui-combogrid record-value"
                    name="assignedToID" style="width: 180px;"
                    data-options="  
						            panelWidth:520,  
						            idField:'id',  
						            textField:'name',  
						            url:'<s:url action="listUser" namespace="/jsp/system"/>',
						            loadMsg: '<s:text name="datagrid.loading" />',
						            pagination : true,
						            pageSize: 10,
						            pageList: [10,30,50],
						            fit: true,
						            mode:'remote',
						            columns:[[  
							                {field:'id',title:'<s:text name="entity.id.label" />',width:60},  
							                {field:'name',title:'<s:text name="entity.name.label" />',width:100},  
							                {field:'title',title:'<s:text name="entity.title.label" />',width:120},  
							                {field:'department',title:'<s:text name="entity.department.label" />',width:100},
							                {field:'status.name',title:'<s:text name="entity.status.label" />',width:100}  
						            ]]  
						        ">
                  </select></td>
                </tr>
                <tr>
                  <td class="td-mass-update"><input id="massUpdate"
                    name="massUpdate" type="checkbox" class="massUpdate"
                    value="notes" /></td>
                  <td class="td-label" valign="top"><label
                    class="record-label"><s:text
                        name="entity.notes.label"></s:text>：</label></td>
                  <td class="td-value" valign="top"><s:textarea
                      name="contact.notes" rows="5" cssStyle="width:350px;" cssClass="record-value" /></td>
                  <td class="td-mass-update"></td>
                  <td class="td-label"></td>
                  <td class="td-value"></td>                         
                </tr>                 
              </table>

              <div class="section-header">
                <span><s:text name="span.system_info" /></span>
              </div>
              <table style="" cellspacing="10" cellpadding="0"
                width="100%">
                <tr>
                  <td class="td-mass-update"></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.createdBy.label"></s:text>：</label></td>
                  <td class="td-value"><label class="record-value"><s:property
                        value="createdBy" /></label></td>
                  <td class="td-mass-update"></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.createdOn.label"></s:text>：</label></td>
                  <td class="td-value"><label class="record-value"><s:property
                        value="createdOn" /></label></td>
                </tr>
                <tr>
                  <td class="td-mass-update"></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.updatedBy.label"></s:text>：</label></td>
                  <td class="td-value"><label class="record-value"><s:property
                        value="updatedBy" /></label></td>
                  <td class="td-mass-update"></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.updatedOn.label"></s:text>：</label></td>
                  <td class="td-value"><label class="record-value"><s:property
                        value="updatedOn" /></label></td>
                </tr>
                <tr>
                  <td class="td-mass-update"></td>
                  <td class="td-label"><label class="record-label"><s:text
                        name="entity.id.label"></s:text>：</label></td>
                  <td class="td-value"><label class="record-value"><s:property
                        value="id" /></label></td>
                  <td class="td-mass-update"></td>
                  <td class="td-label"></td>
                  <td class="td-value"></td>
                </tr>
              </table>
            </div>

            <div title="<s:text name='tab.relations'/>" id="relations"
              style="padding: 10px;">
              <table style="" cellspacing="10" cellpadding="0"
                width="100%">
                <tr>
                  <td width="20%" valign="top">
                    <div class="easyui-accordion" style="width: 200px;">
                      <div title="<s:text name="menu.sales.title"/>"
                        style="overflow: auto; padding: 10px;"
                        selected="true">
                        <a
                          href="relateContactOpportunityPage.action?id=<s:property value="contact.id" />"
                          target="contentFrame"><label
                          class="record-value menuLink"><s:text
                              name="menu.opportunities.title" />(<span
                            id="opportunityNumber"></span>)</label></a><br /> <a
                          href="relateContactLeadPage.action?id=<s:property value="contact.id" />"
                          target="contentFrame"><label
                          class="record-value menuLink"><s:text
                              name="menu.leads.title" />(<span
                            id="leadNumber"></span>)</label></a><br /> <a
                          href="relateContactDocumentPage.action?id=<s:property value="contact.id" />"
                          target="contentFrame"><label
                          class="record-value menuLink"><s:text
                              name="menu.documents.title" />(<span
                            id="documentNumber"></span>)</label></a>
                      </div>
                      <div
                        title="<s:text name="menu.activities.title"/>"
                        style="overflow: auto; padding: 10px;"
                        selected="true">
                        <a
                          href="filterTaskPage.action?filter_key=related_record&id=<s:property value="contact.id" />&moreFilterKey=relationKey&moreFilterValue=Contact&createKey=relationValue&removeKey=Contact"
                          target="contentFrame"><label
                          class="record-value menuLink"><s:text
                              name="menu.tasks.title" />(<span
                            id="taskNumber"></span>)</label></a>
                      </div>
                      <div title="<s:text name="menu.support.title"/>"
                        style="overflow: auto; padding: 10px;">
                        <a
                          href="relateContactCasePage.action?id=<s:property value="contact.id" />"
                          target="contentFrame"><label
                          class="record-value menuLink"><s:text
                              name="menu.cases.title" />(<span
                            id="caseNumber"></span>)</label></a>
                      </div>
                    </div>
                  </td>
                  <td width="80%" valign="top"><Iframe
                      name="contentFrame" id="contentFrame"
                      scrolling="no" frameborder="0" width="100%"
                      height="390"></iframe></td>
                </tr>
              </table>
            </div>

          </div>

        </s:form>
      </div>
    </div>

    <s:include value="../footer.jsp" />
  </div>
</body>
</html>



