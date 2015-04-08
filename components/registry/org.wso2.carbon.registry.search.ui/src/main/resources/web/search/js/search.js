var mediaTypes = new Array();
var currentMediaType = "";

function fillMediaTypesForSearch() {

    var humanReadableMediaTypeMap = new Object();

    new Ajax.Request('../resources/get_human_readable_mediatype_ajaxprocessor.jsp',
        {
            method:'get',
            parameters:{random:getRandom()},
            onSuccess: function(transport) {
                var humanReadableMediaTypeResponse = transport.responseText || "";

                var mType = jQuery.trim(humanReadableMediaTypeResponse).split(',');
                for (var i = 0; i < mType.length; i++) {
                    var typeData = mType[i].split(':');
                    if (typeData.length == 2) {
                        humanReadableMediaTypeMap[typeData[0]] = typeData[1];
                    }
                }
                new Ajax.Request('../resources/get_media_types_ajaxprocessor.jsp',
                    {
                        method:'get',
                        parameters:{random:getRandom()},
                        onSuccess: function(transport) {
                            var mediaTypeResponse = transport.responseText || "txt:text,wsdl:wsdl/xml";

                            var mType = mediaTypeResponse.split(',');
                            for (var i = 0; i < mType.length; i++) {
                                var typeData = mType[i].split(':');
                                if (typeData.length == 2 && mediaTypes.indexOf(typeData[1]) < 0) {
                                    if (humanReadableMediaTypeMap[typeData[1]] != null
                                        && mediaTypes.indexOf(humanReadableMediaTypeMap[typeData[1]]) < 0) {
                                        mediaTypes.push(humanReadableMediaTypeMap[typeData[1]]);
                                    } else {
                                        mediaTypes.push(typeData[1]);
                                    }
                                }
                            }
                            initAutocomplete(mediaTypes);
                        },
                        onFailure: function() {
                            CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["could.not.get.media.type.information"]);
                        }
                    });
            },
            onFailure: function() {
                CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["could.not.get.custom.ui.media.type.information"]);
            }
        });
}

function initAutocomplete(datasource) {
    // Use a LocalDataSource
    var oDS = new YAHOO.util.LocalDataSource(datasource);
    // Optional to define fields for single-dimensional array
    oDS.responseSchema = {fields : ["state"]};

    // Instantiate the AutoComplete
    var oAC = new YAHOO.widget.AutoComplete("#_mediaType", "mediaTypeContainer", oDS);
    oAC.prehighlightClassName = "yui-ac-prehighlight";
    oAC.useShadow = true;
    oAC.itemSelectEvent.subscribe(itemSelectHandler);

    return {
        oDS: oDS,
        oAC: oAC
    };
}
function itemSelectHandler(sType, aArgs) {
	YAHOO.log(sType); // this is a string representing the event;
				      // e.g., "itemSelectEvent"
	var oMyAcInstance = aArgs[0]; // your AutoComplete instance
	var elListItem = aArgs[1]; // the <li> element selected in the suggestion
	   					       // container
	var oData = aArgs[2]; // object literal of data for the result
    createCustomUI(oData[0]);
}
function loadCustomUI(){
    var mediaType = $("#_mediaType").value;
    if (mediaType == "") {
        return;
    }
    createCustomUI(mediaType);
}
function createCustomUI(mediaType) {
    sessionAwareFunction(function() {
        new Ajax.Request('../search/customSearch-ajaxprocessor.jsp',
    {
        method:'get',
        parameters: {mediaType:mediaType , evalScripts: true},
        onSuccess: function(transport) {
            $('customDiv').innerHTML = transport.responseText;
            if (trim(transport.responseText) != "") {
                $('customUIButtonDiv').innerHTML = //'<input id="#_mediaType" name="mediaType" type="text" style="width:auto !important;"/>' +
                                        '<a href="javascript:collapseCustomUI()" class="loadMediaTypeButton">' + org_wso2_carbon_registry_search_ui_jsi18n["fewer"] +
                                                                                                                          ' <img src="../search/images/arrow-up.png" /></a>';
            } else {
                $('customUIButtonDiv').innerHTML = "";
            }
            initAutocomplete(mediaTypes);
        },
        onFailure: function() {
            CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["could.not.get.media.type.information"]);
        }
    });
//        new Ajax.Updater('customDiv', '../search/customSearch-ajaxprocessor.jsp',
//        { method: 'get', parameters: {mediaType:mediaType} , evalScripts: true });
//
//
//        initAutocomplete(mediaTypes);
//        var customDivVar = $('customDiv');
//        customDivVar.style.display = "block";

    }, org_wso2_carbon_registry_search_ui_jsi18n["session.timed.out"]);
}
function collapseCustomUI() {
    if (trim($('customDiv').innerHTML) != "") {
        $('customUIButtonDiv').innerHTML = //'<input id="#_mediaType" name="mediaType" type="text" style="width:auto !important;"/>' +
                                        '<a href="javascript:loadCustomUI()" class="loadMediaTypeButton">' +
                                           org_wso2_carbon_registry_search_ui_jsi18n["more"] + ' <img src="../search/images/arrow-down.png" /></a>';
    }
    $('customDiv').innerHTML = "";
    initAutocomplete(mediaTypes);
}

function showHideCustomDiv(){

    var table = $('customTable');
    var rows = table.getElementsByTagName('input');

    if(currentMediaType == $("#_mediaType").value){
       $('customUIButtonDiv').innerHTML = //'<input id="#_mediaType" name="mediaType" type="text" style="width:auto !important;"/>' +
                                    '<a href="javascript:loadCustomUI()" class="loadMediaTypeButton">' +
                                       org_wso2_carbon_registry_search_ui_jsi18n["more"] + ' <img src="../search/images/arrow-down.png" /></a>';
    }
    else{
       $('customUIButtonDiv').innerHTML ="";
       currentMediaType = $("#_mediaType").value;
    }

}

function clearAll(){
    var table = $('customTable');
    var rows = table.getElementsByTagName('input');

    for (var i = 0; i < rows.length; i++) {
         if (rows[i].type == "text") {
            rows[i].value = "";
         }

         if(rows[i].type == "checkbox"){
            rows[i].checked = false;
         }
    }

    var opList = table.getElementsByTagName('select');

    for (var i = 0; i < opList.length; i++) {
        opList[i].selectedIndex = 0;
    }

    document.getElementById("lblPropName").innerHTML = "-";
    collapseCustomUI();
}

function isNumberKey(evt) {
   var opRight = document.getElementById('opRight').value;

   if(opRight != 'eq') {
    var charCode = (evt.which) ? evt.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57))
          return false;
   }
   return true;
}	

function adjustMediaTypeNegate(isFilter, oldMediaType) {

       if (isFilter == "1") {

          if(oldMediaType != null && oldMediaType != "") {
            var mediaTypeNegateObj = document.getElementById('mediaTypeNegateDiv');
            var currentMediaType = document.getElementById('#_mediaType').value;

            if(oldMediaType != currentMediaType || (oldMediaType == currentMediaType && !isCustomUIDivEmpty())) {
                mediaTypeNegateObj.style.cssFloat = "left";
                mediaTypeNegateObj.style.marginLeft = "160px";
            }
          }
    }
}

function isCustomUIDivEmpty() {
    var htmlString = document.getElementById('customUIButtonDiv').innerHTML;
    htmlString = (htmlString.trim) ? htmlString.trim() : htmlString.replace(/^\s+/, '');

    if (htmlString == '')
        return true;

    return false;
}

function initRangeOperators() {

    var leftVal = document.getElementById("hiddenOpLeft").value;
    var rightVal = document.getElementById("hiddenOpRight").value;

    setIndexForOp("opLeft", leftVal);
    setIndexForOp("opRight", rightVal);
    adjustPropertyInput();
}

function initMiscFields() {

    // Set the media type negate styles in case there's no values in the filter for it
    var mediaTypeNegateObj = document.getElementById('mediaTypeNegateDiv');
    var mediaTypeIn = document.getElementById('#_mediaType');
    mediaTypeNegateObj.style.cssFloat = "left";
    if(mediaTypeIn.value != "") {
        mediaTypeNegateObj.style.marginLeft = "20px";
    } else {
        mediaTypeNegateObj.style.marginLeft = "160px";
    }
}

function submitAdvSearchForm(pageNumber) {		
    sessionAwareFunction(function() {
        document.getElementById('advancedSearchFormDiv').style.display = "none";
	var resourceName = document.getElementById('#_resourceName');
        var reasonDiv = $('advSearchReason');
        var reason = "";
        var searchResuts = $('searchResuts');
        searchResuts.style.display = "";
        searchResuts.innerHTML = org_wso2_carbon_registry_search_ui_jsi18n["searching"];

        var table = $('customTable');
        var rows = table.getElementsByTagName('input');        

        var cFromDate, cToDate,
                uFromDate, uToDate;
	
       
        for (var i = 0; i < rows.length; i++) {
            if ((rows[i].id == "cfromDate") && rows[i].value != "") {
                cFromDate = rows[i];
                reason += validateDate(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["from"]);
            }
            else if ((rows[i].id == "ctoDate") && rows[i].value != "") {
                cToDate = rows[i];
                reason += validateDate(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["to"]);
            }
            else if ((rows[i].id == "ufromDate") && rows[i].value != "") {
                uFromDate = rows[i];
                reason += validateDate(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["from"]);
            }
            else if ((rows[i].id == "utoDate") && rows[i].value != "") {
                uToDate = rows[i];
                reason += validateDate(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["to"]);
            }

            else if ((rows[i].id == "#_resourceName") && trim(rows[i].value) != "")
	    {
		if(rows[i].value.indexOf("/") >= 0 )
		{
			//reason += "invalid search term";
			reason += org_wso2_carbon_registry_search_ui_jsi18n["invalid.search.term"];
		}

		reason += validateIllegalNoPercent(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["resource.name"]);
	    }
            else if ((rows[i].id == "#_content") && trim(rows[i].value) != "") reason += validateIllegalContentSearchString(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["content.name"]);
            else if ((rows[i].id == "#_author") && rows[i].value != "") reason += validateIllegalSearchString(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["created.by"]);
            else if ((rows[i].id == "#_updater") && rows[i].value != "") reason += validateIllegalSearchString(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["updated.by"]);
            else if ((rows[i].id == "#_tags") && rows[i].value != "") reason += validateTagsInput(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["tags"]);
            else if ((rows[i].id == "#_comments") && rows[i].value != "") reason += validateForInput(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["comments"]);
            else if ((rows[i].id == "#_associationType") && rows[i].value != "") reason += validateIllegalSearchString(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["associationType"]);
            else if ((rows[i].id == "#_associationDest") && rows[i].value != "") reason += validateIllegalSearchString(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["associationDest"]);
            else if ((rows[i].value != "") && rows[i].type == "text") {           	
               reason += validateForInput(rows[i], rows[i].name);
            }           
        }
		
        
        
        if(cFromDate != null && cFromDate.value != "" && cToDate != null && cToDate.value != "") {
            reason += validateToFromDate(cFromDate, cToDate);
        }

        if(uFromDate != null && uFromDate.value != "" && uToDate != null && uToDate.value != "") {
            reason += validateToFromDate(uFromDate, uToDate);
        }
        
        reasonDiv.innerHTML = reason;
        if (reason == "") {

            var emptyFields = 0;
            for (var i = 0; i < rows.length; i++) {
                if (rows[i].type == "text") {
                    emptyFields += emptyIncrementer(rows[i]);
                }
            }
            
            var customParamterList = "";           
            
            for (var i = 0; i < rows.length - 1; i++) {            	
                if (rows[i].type == "text") {
                    customParamterList = customParamterList + rows[i].name + "^";
                    if (rows[i].value == "") {
                        customParamterList = customParamterList + "null";
                    }
                    else {
                        customParamterList = customParamterList + rows[i].value;
                    }
                    if (i != (rows.length - 1)) {
                        customParamterList = customParamterList + "|";
                    }
                }
               
                if (rows[i].type == "checkbox") {                	
                    customParamterList = customParamterList + rows[i].name + "^";
                    if (rows[i].checked) {
                        customParamterList = customParamterList + "on";
                    }
                    else {
                        customParamterList = customParamterList + "null";
                    }
                    if (i != (rows.length - 1)) {
                        customParamterList = customParamterList + "|";
                    }
                }               
            }           
            
            var opList = table.getElementsByTagName('select');            
            
            for (var i = 0; i < opList.length; i++) {
            	customParamterList = customParamterList + opList[i].name + "^";
            	customParamterList = customParamterList + opList[i].value;
            	if (i != (opList.length - 1)) {
                    customParamterList = customParamterList + "|";
                }           	
            }           

            var validateValue = validateEmptyPropertyValues();
                if(validateValue > 0 ) {
                   searchResuts.innerHTML = "";
                   	   if(validateValue == 1) {
                         CARBON.showWarningDialog(org_wso2_carbon_registry_search_ui_jsi18n["property.name.required"]);
                       }
                       document.getElementById('advancedSearchFormDiv').style.display = "";
                         return false;

                }
            
            if(validatePropertyValues() == 0){
            	searchResuts.innerHTML = "";			
            	CARBON.showWarningDialog(org_wso2_carbon_registry_search_ui_jsi18n["left.needs.less.than.right.property.value"]);
            	document.getElementById('advancedSearchFormDiv').style.display = "";            	
            	return false;
            }

            if (emptyFields == 0) {
                searchResuts.innerHTML = "";
                CARBON.showWarningDialog(org_wso2_carbon_registry_search_ui_jsi18n["please.fill.at.least.one"]);
                document.getElementById('advancedSearchFormDiv').style.display = "";
                return false;
            }

            var advancedSearchForm = $('advancedSearchForm');
            advancedSearchForm.style.display = "none";
            reasonDiv.innerHTML = "<a href=\"#\" onclick=\"displayAdvSearchForm(this)\"" +
                                  "class=\"icon-link\" style=\"margin-left:0px;background-image:url(images/search.gif);\">" +
                                  org_wso2_carbon_registry_search_ui_jsi18n["search.again"] + "</a>";
            reasonDiv.style.display = "block";

            if (pageNumber) {
                new Ajax.Updater('searchResuts', '../search/advancedSearch-ajaxprocessor.jsp',
                { method: 'get', parameters: {parameterList:customParamterList,requestedPage:pageNumber} , evalScripts: true });
            } else {
                new Ajax.Updater('searchResuts', '../search/advancedSearch-ajaxprocessor.jsp',
                { method: 'get', parameters: {parameterList:customParamterList} , evalScripts: true });
            }
            $('#_0').focus();

        }
        else {
            searchResuts.innerHTML = "";
            document.getElementById('advancedSearchFormDiv').style.display = "";
            CARBON.showWarningDialog(reason);
            return false;
        }
    }
            ,
            org_wso2_carbon_registry_search_ui_jsi18n["session.timed.out"]
            )
            ;
}

function adjustPropertyInput(){
	var opLeft = document.getElementById('opLeft');
	var opRight = document.getElementById('opRight');
	var txtRight = document.getElementById('valueRight');
	var txtLeft = document.getElementById('valueLeft');

	opLeft.disabled=false;
	txtLeft.disabled=false;

	if(opRight.options[opRight.selectedIndex].value == "eq"){
	  opLeft.disabled=true;
	  txtLeft.disabled=true;
	  txtLeft.value = "";
	}
}

function adjustAllOpInput() {
    var txtRight = document.getElementById('valueRight');
    adjustPropertyInput();
    txtRight.value = "";
}



function setPropertyName() {
    var propName = document.getElementById("#_propertyName");
    var propLabel = document.getElementById("lblPropName");

    if(propName.value != "") {
        propLabel.innerHTML = propName.value;
    } else {
        propLabel.innerHTML = "-";
    }
}

function setIndexForOp(opName, param) {
    var opList = document.getElementById(opName);

    for (var i = 0; i < opList.options.length; i++ ){

      if(opList.options[i].value == param) {
        opList.selectedIndex = i;
        break;
      }
    }
}


function submitSaveSearchForm() {
    sessionAwareFunction(function() {
        //var reasonDiv = $('advSearchReason');
        var reason = "";

        var table = $('customTable');
        var rows = table.getElementsByTagName('input');

        
        for (var i = 0; i < rows.length; i++) {
            if ((rows[i].id == "cfromDate") && rows[i].value != "") reason += validateDate(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["from"]);
            else if ((rows[i].id == "ctoDate") && rows[i].value != "") reason += validateDate(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["to"]);
            else if ((rows[i].id == "ufromDate") && rows[i].value != "") reason += validateDate(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["from"]);
            else if ((rows[i].id == "utoDate") && rows[i].value != "") reason += validateDate(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["to"]);
            else if ((rows[i].id == "#_resourceName") && trim(rows[i].value) != "") reason += validateIllegalNoPercent(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["resource.name"]);
            else if ((rows[i].id == "#_author") && rows[i].value != "") reason += validateForInput(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["created.by"]);
            else if ((rows[i].id == "#_updater") && rows[i].value != "") reason += validateForInput(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["updated.by"]);
            else if ((rows[i].id == "#_tags") && rows[i].value != "") reason += validateTagsInput(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["tags"]);
            else if ((rows[i].id == "#_comments") && rows[i].value != "") reason += validateForInput(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["comments"]);
            else if ((rows[i].id == "#_associationType") && rows[i].value != "") reason += validateForInput(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["associationType"]);
            else if ((rows[i].id == "#_associationDest") && rows[i].value != "") reason += validateForInput(rows[i], org_wso2_carbon_registry_search_ui_jsi18n["associationDest"]);
            else if ((rows[i].value != "") && rows[i].type == "text") {
                reason += validateForInput(rows[i], rows[i].name);
            }
        } 

        //reasonDiv.innerHTML = reason;
        if (reason == "") {
            var fnReason = validateFilterName($('#_saveFilterName'), org_wso2_carbon_registry_search_ui_jsi18n["filter.name"]);

            if (fnReason != "") {
                CARBON.showWarningDialog(fnReason);
                return false;
            }


            var emptyFields = 0;
            for (var i = 0; i < rows.length; i++) {
                if (rows[i].type == "text") {
                    emptyFields += emptyIncrementer(rows[i]);
                }
            }

            var customParamterList = "";

            for (var i = 0; i < rows.length - 1; i++) {
                if (rows[i].type == "text") {
                    customParamterList = customParamterList + rows[i].name + "^";
                    if (rows[i].value == "") {
                        customParamterList = customParamterList + "null";
                    }
                    else {
                        customParamterList = customParamterList + rows[i].value;
                    }

                    if (i != (rows.length - 1)) {
                        customParamterList = customParamterList + "|";
                    }
                }

                if (rows[i].type == "checkbox") {
                    customParamterList = customParamterList + rows[i].name + "^";
                    if (rows[i].checked) {
                        customParamterList = customParamterList + rows[i].value;
                    } else {
                        customParamterList = customParamterList + "null";
                    }

                    if (i != (rows.length - 1)) {
                        customParamterList = customParamterList + "|";
                    }
                }
            }

            var opList = table.getElementsByTagName('select');

                for (var i = 0; i < opList.length; i++) {
                      customParamterList = customParamterList + opList[i].name + "^";
                      customParamterList = customParamterList + opList[i].value;
                      if (i != (opList.length - 1)) {
                        customParamterList = customParamterList + "|";
                      }
                }


            if (emptyFields == 0) {
                $('searchResuts').innerHTML = "";
                CARBON.showWarningDialog(org_wso2_carbon_registry_search_ui_jsi18n["please.fill.at.least.one"]);
                return false;
            }

            var saveFilterName = $('#_saveFilterName').value;

            new Ajax.Request('../search/isDuplicateFilterName-ajaxprocessor.jsp',
            {
                method:'get',
                parameters: {filterName: saveFilterName},
                onSuccess: function(transport) {
                    var returnValue = transport.responseText;
                    if (returnValue.search(/----DuplicateFilterName----/) != -1){
                        CARBON.showConfirmationDialog(org_wso2_carbon_registry_search_ui_jsi18n["are.you.sure.you.want.to.replace.search.filter"] + "&nbsp;<strong>'" +
                                                      saveFilterName + "'</strong> ",
                                function() {
                                    saveSearchFilter(customParamterList, saveFilterName);
                                }, null);

                    } else {
                        saveSearchFilter(customParamterList, saveFilterName);
                    }
                },
                onFailure: function() {
                    CARBON.showErrorDialog(org_wso2_carbon_registry_search_ui_jsi18n["search.filter.was.not.saved"]);
                    return false;
                }
            });

/*

            new Ajax.Request('../search/saveSearchFilter-ajaxprocessor.jsp',
            {
                method: 'get',
                parameters: {parameterList:customParamterList,saveFilterName:$('#_saveFilterName').value},
                evalScripts: true,

                onSuccess: function() {
                    CARBON.showInfoDialog(org_wso2_carbon_registry_search_ui_jsi18n["successfully.saved.search.filter"]);
                    $('#_saveFilterName').value = "";
                    showSaveSearch();
                },

                onFailure: function(transport) {
                    CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.save.search.filter"] + transport.responseText);
                }
            });
            $('#_0').focus();

*/

        } else {
            CARBON.showWarningDialog(reason);
            return false;
        }

    }, org_wso2_carbon_registry_search_ui_jsi18n["session.timed.out"]);
}

function saveSearchFilter(customParameterList, saveFilterName) {
    new Ajax.Request('../search/saveSearchFilter-ajaxprocessor.jsp',
    {
        method: 'get',
        parameters: {parameterList:customParameterList,saveFilterName:saveFilterName},
        evalScripts: true,

        onSuccess: function() {
            CARBON.showInfoDialog(org_wso2_carbon_registry_search_ui_jsi18n["successfully.saved.search.filter"]);
            $('#_saveFilterName').value = "";
            new Ajax.Updater('savedSearchFilterListDiv', '../search/getSavedSearchFilters-ajaxprocessor.jsp',{evalScripts:true});
            //showSaveSearch();
        },

        onFailure: function(transport) {
            CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.save.search.filter"] + transport.responseText);
        }
    });
    $('#_0').focus();
}


function loadSearchFilter() {
    sessionAwareFunction(function() {
        document.getElementById('advancedSearchFormDiv').style.display = "";        
        document.getElementById('advancedSearchForm').style.display = 'block';
        document.getElementById('advSearchReason').style.display = 'none';
        document.getElementById('searchResuts').style.display = 'none';
        new Ajax.Request('../search/advancedSearchForm-ajaxprocessor.jsp',
        {
            method:'get',
            parameters: {filterName: $('savedSearchFilterList').value},

            onSuccess: function(transport) {
                document.getElementById('advancedSearchFormDiv').innerHTML = transport.responseText;
                initDatePickers();
                initRangeOperators();
                initMiscFields();
                //fillMediaTypes();
                $('#_0').fcocus();
            }
        });

    }, org_wso2_carbon_registry_search_ui_jsi18n["session.timed.out"]);
}

function deleteSearchFilter(filterName) {

     if (filterName == "None") {
         return;
     }

     sessionAwareFunction(function() {

         CARBON.showConfirmationDialog(org_wso2_carbon_registry_search_ui_jsi18n["are.you.sure.you.want.to.delete.the.filter"] + "&nbsp;<strong>'" +
                                                              filterName + "'</strong> ",
        function() {
            new Ajax.Request('../search/deleteSearchFilter-ajaxprocessor.jsp',
            {
                method:'get',
                parameters: {filterName:filterName},

                onSuccess: function(transport) {
                document.getElementById('advancedSearchFormDiv').innerHTML = transport.responseText;
                new Ajax.Updater('savedSearchFilterListDiv', '../search/getSavedSearchFilters-ajaxprocessor.jsp',{evalScripts:true});
                new Ajax.Updater('advancedSearchFormDiv', '../search/advancedSearchForm-ajaxprocessor.jsp');
                initDatePickers();
                $('#_0').fcocus();
            } ,

                onFailure: function(transport) {
                    addSuccess = false;
                    CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.delete"] +
                        " <strong>'" +filterName + "'</strong>. " + transport.responseText);
                }
        }); },null);
     }, org_wso2_carbon_registry_search_ui_jsi18n["session.timed.out"]);
}


function displayAdvSearchForm(obj) {
    sessionAwareFunction(function() {
        document.getElementById('advancedSearchFormDiv').style.display = "";
        document.getElementById('advancedSearchForm').style.display = '';
        obj.parentNode.style.display = 'none';
    }, org_wso2_carbon_registry_search_ui_jsi18n["session.timed.out"]);
}

function directToResource(url) {

    sessionAwareFunction(function() {
        location.href = url;
    }, org_wso2_carbon_registry_search_ui_jsi18n["session.timed.out"]);
}
function emptyIncrementer(fld) {
    if (trim(fld.value) == "") {
        fld.value = trim(fld.value);
        return 0;
    }
    else {
        return 1;
    }
}

function handletextBoxKeyPress(event) {
    if (event.keyCode == 13) {
        submitAdvSearchForm();
    }
}

function showSimpleSearch() {
    if ($('searchIconExpanded').style.display == "none") {
        //We have to expand all and hide sum
        $('searchIconExpanded').style.display = "";
        $('searchIconMinimized').style.display = "none";
        $('searchExpanded').style.display = "";
        $('searchMinimized').style.display = "none";
    }
    else {
        $('searchIconExpanded').style.display = "none";
        $('searchIconMinimized').style.display = "";
        $('searchExpanded').style.display = "none";
        $('searchMinimized').style.display = "";
    }
}

/*function showSaveSearch() {
    sessionAwareFunction(function() {
        if ($('saveSearchIconExpanded').style.display == "none") {
            $('saveSearchIconExpanded').style.display = "";
            $('saveSearchIconMinimized').style.display = "none";
            $('saveSearchExpanded').style.display = "";
            $('saveSearchMinimized').style.display = "none";
        }
        else {
            $('saveSearchIconExpanded').style.display = "none";
            $('saveSearchIconMinimized').style.display = "";
            $('saveSearchExpanded').style.display = "none";
            $('saveSearchMinimized').style.display = "";
        }
    }, org_wso2_carbon_registry_search_ui_jsi18n["session.timed.out"]);
}

function showLoadSearch() {
    sessionAwareFunction(function() {
        if ($('loadSearchIconExpanded').style.display == "none") {
            $('loadSearchIconExpanded').style.display = "";
            $('loadSearchIconMinimized').style.display = "none";
            $('loadSearchExpanded').style.display = "";
            $('loadSearchMinimized').style.display = "none";
            new Ajax.Updater('savedSearchFilterListDiv', '../search/getSavedSearchFilters-ajaxprocessor.jsp');
        }
        else {
            $('loadSearchIconExpanded').style.display = "none";
            $('loadSearchIconMinimized').style.display = "";
            $('loadSearchExpanded').style.display = "none";
            $('loadSearchMinimized').style.display = "";
        }
    }, org_wso2_carbon_registry_search_ui_jsi18n["session.timed.out"]);
}*/

