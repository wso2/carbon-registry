/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
function submitContentSearch() {
    var content = document.getElementById('#_content');
    var reason = validateEmpty(content, org_wso2_carbon_registry_indexing_ui_jsi18n["search.for"]);
    if (reason != "") {
        CARBON.showWarningDialog(reason);
        return false;
    }
    var regEx = new RegExp("^[a-zA-Z 0-9()&|\"]*$");
	if (content.getValue() == "") {
		return false;
    } else if (!content.value.match(regEx)) {
        CARBON.showWarningDialog("Please only enter letters, digits or parentheses as input for content search. Use quotation marks to search for occurrences of a phrase.");
        return false;
	} else {
		return true;
	}
}

function submitAdvSearchForm() {
    var reasonDiv = $('advSearchReason');
    var reason = "";
    var searchResuts = $('searchResuts');
    searchResuts.innerHTML = jsi18n["searching"] + " ....";

    if ($('cfromDate').value != "") reason += validateDate($('cfromDate'), "From");
    if ($('ctoDate').value != "") reason += validateDate($('ctoDate'), "To");
    if ($('ufromDate').value != "") reason += validateDate($('ufromDate'), "From");
    if ($('utoDate').value != "") reason += validateDate($('utoDate'), "To");
    if ($('#_resourceName').value != "") reason += validateForInput($('#_resourceName'), "Resource Name");
    if ($('#_author').value != "") reason += validateForInput($('#_author'), "Created by");
    if ($('#_updater').value != "") reason += validateForInput($('#_updater'), "Updated by");
    if ($('#_tags').value != "") reason += validateForInput($('#_tags'), "Tags");
    if ($('#_comments').value != "") reason += validateForInput($('#_comments'), "Comments");
    if ($('#_content').value != "") reason += validateForInput($('#_content'), "Including Content");
    reasonDiv.innerHTML = reason;
    if (reason != "") {
        CARBON.showWarningDialog(reason);
        return false;
    }
    else {
    	//Do the validation for atleast one field is filled
    	var emptyFields = 	emptyIncrementer($('#_resourceName')) + 
    				emptyIncrementer($('cfromDate')) +
    				emptyIncrementer($('ctoDate')) +
    				emptyIncrementer($('ufromDate')) +
    				emptyIncrementer($('utoDate')) +
    				emptyIncrementer($('#_author')) +
    				emptyIncrementer($('#_updater')) +
    				emptyIncrementer($('#_tags')) +
    				emptyIncrementer($('#_comments')) +
    				emptyIncrementer($('#_propertyName')) +
    				emptyIncrementer($('#_content')) +
    				emptyIncrementer($('#_propertyValue'));
    	if(emptyFields == 0){
    		searchResuts.innerHTML = "";
    		CARBON.showWarningDialog("Please fill at least one field.");
        	return false;
    	}
    	
        var advancedSearchForm = $('advancedSearchForm');
        advancedSearchForm.style.display = "none";
        reasonDiv.innerHTML = "<a href=\"#\" onclick=\"javascript:document.getElementById('advancedSearchForm').style.display='block';" +
                              "this.parentNode.style.display='none'\"" +
                              "class=\"icon-link\" style=\"background-image:url(images/search.gif);\">" +
                              jsi18n["search.again"] + "</a>";
        reasonDiv.style.display = "block";

        new Ajax.Updater('searchResuts', './advancedSearch-ajaxprocessor.jsp',
        { method: 'post', parameters: {createdAfter: $('cfromDate').value,createdBefore:$('ctoDate').value, updatedAfter: $('ufromDate').value,updatedBefore:$('utoDate').value,resourcePath:$('#_resourceName').value,author:$('#_author').value,updater:$('#_updater').value,tags:$('#_tags').value,commentWords:$('#_comments').value,propertyName:$('#_propertyName').value,propertyValue:$('#_propertyValue').value, content:$('#_content').value} });

    }
}
function emptyIncrementer(fld){
	if(fld.value == ""){
		return 0;
	}
	else{
		return 1;
	}
}
function handletextBoxKeyPress(event) {
    if (event.keyCode == 13) {
    	submitContentSearch();
    }
}
function showSimpleSearch(){
	if($('searchIconExpanded').style.display == "none"){
	//We have to expand all and hide sum
	$('searchIconExpanded').style.display = "";
	$('searchIconMinimized').style.display = "none";
	$('searchExpanded').style.display = "";
	$('searchMinimized').style.display = "none";
	}
	else{
	$('searchIconExpanded').style.display = "none";
	$('searchIconMinimized').style.display = "";
	$('searchExpanded').style.display = "none";
	$('searchMinimized').style.display = "";
	}
}

function clearAll() {
    document.getElementById('#_content').value = '';
}