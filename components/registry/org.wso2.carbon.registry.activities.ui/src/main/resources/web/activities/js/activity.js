function submitActivityForm(page, pageNumber) {
    sessionAwareFunction(function() {
        //Do the normal logic when the seesion is not timed out
        var reasonDiv = document.getElementById('activityReason');
        var reason = "";
        var searchResuts = $('activityList');
        searchResuts.innerHTML = org_wso2_carbon_registry_activities_ui_jsi18n["searching"];

        var fromDate = document.getElementById('fromDate');
        var toDate = document.getElementById('toDate');
        var userName = document.getElementById('user');
        var path = document.getElementById('path');
        var filterElement = document.getElementById('filter');
        var filter = filterElement.options[filterElement.selectedIndex].value;

        if (fromDate.value != "") reason += validateDate(fromDate, org_wso2_carbon_registry_activities_ui_jsi18n["from"]);
        if (toDate.value != "") reason += validateDate(toDate, org_wso2_carbon_registry_activities_ui_jsi18n["to"]);
        //validate the from and to date functions.
        reason += validateToFromDate(fromDate, toDate);
        if (userName.value != "") reason += validateForInput(userName, org_wso2_carbon_registry_activities_ui_jsi18n["username"]);
        if (path.value != "") reason += validateForInput(path, org_wso2_carbon_registry_activities_ui_jsi18n["path"]);


        var fromDateValue = fromDate.value;
        var toDateValue = toDate.value;
        var userNameValue = userName.value;
        var pathValue = path.value;

        if(fromDateValue!="" && (fromDateValue == toDateValue)){
            $('activityList').innerHTML="";
            CARBON.showWarningDialog(org_wso2_carbon_registry_activities_ui_jsi18n["from.date.equal.to.date"]);
            return true;
        }

        if(fromDateValue=="" && toDateValue=="" && userNameValue =="" && pathValue =="" && filter == "all"){
            $('activityList').innerHTML="";
            CARBON.showWarningDialog(org_wso2_carbon_registry_activities_ui_jsi18n["please.fill.at.least.one"]);
            return true;
        }
        reasonDiv.innerHTML = reason;
        if (reason != "") {
            CARBON.showWarningDialog(reason);
            $('activityList').innerHTML = "";
            return false;
        }
        else {

            var advancedSearchForm = $('activityForm');
            advancedSearchForm.style.display = "none";
            reasonDiv.innerHTML = "<a href=\"#\" " +
                                  "onclick=\"javascript:document.getElementById('activityForm').style.display='block';" +
                                  "this.parentNode.style.display='none'\"" +
                                  "class=\"icon-link\" style=\"background-image:url(images/search.gif);\">" +
                                  org_wso2_carbon_registry_activities_ui_jsi18n["search.again"] + "</a>";
            reasonDiv.style.display = "block";
            if (pageNumber) {
                new Ajax.Request('../activities/activity-ajaxprocessor.jsp',
                {
                    method:'post',
                    parameters: {fromDate: fromDateValue, toDate: toDateValue, userName:userNameValue,path:pathValue,filter:filter,page:page,requestedPage:pageNumber},

                    onSuccess: function(transport) {
                        $('activityList').innerHTML = transport.responseText;
                    },

                    onFailure: function(transport) {
                        CARBON.showErrorDialog(org_wso2_carbon_registry_activities_ui_jsi18n["an.error.occured"] +
                                               " " + transport.responseText);
                    }
                });
            } else {
                new Ajax.Request('../activities/activity-ajaxprocessor.jsp',
                {
                    method:'post',
                    parameters: {fromDate: fromDateValue, toDate: toDateValue, userName:userNameValue,path:pathValue,filter:filter,page:page},

                    onSuccess: function(transport) {
                        $('activityList').innerHTML = transport.responseText;
                    },

                    onFailure: function(transport) {
                        CARBON.showErrorDialog(org_wso2_carbon_registry_activities_ui_jsi18n["an.error.occured"] +
                                               " " + transport.responseText);
                    }
                });
            }
        }

    }, org_wso2_carbon_registry_activities_ui_jsi18n["session.timed.out"]);
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

function handleUserNameKeyPress(event) {
    if (event.keyCode == 13) {
        submitActivityForm(1);
    }
}

function clearAll(){
    var table = $('#_innerTable');
    var Inputrows = table.getElementsByTagName('input');

    for (var i = 0; i < Inputrows.length; i++) {
         if (Inputrows[i].type == "text") {
            Inputrows[i].value = "";
         }
    }
    var SelectAreas = table.getElementsByTagName('select');
    for (var i = 0; i < SelectAreas.length; i++) {
        SelectAreas[i].selectedIndex = 0;
    }
}