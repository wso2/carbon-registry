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

        var fromDate = document.getElementById('fromDate');
        var toDate = document.getElementById('toDate');
        var userName = document.getElementById('user');
        var path = document.getElementById('path');
        var filterElement = document.getElementById('filter');
        var filter = filterElement.options[filterElement.selectedIndex].value;

        var fromDateValue = fromDate.value;
        var toDateValue = toDate.value;
        var userNameValue = userName.value;
        var pathValue = path.value;

            var fnReason = validateFilterName($('#_saveFilterName'), org_wso2_carbon_registry_activities_ui_jsi18n["filter.name"]);

            if (fnReason != "") {
                CARBON.showWarningDialog(fnReason);
                return false;
            }


            var saveFilterName = $('#_saveFilterName').value;

            new Ajax.Request('../activities/isDuplicateFilterName-ajaxprocessor.jsp',
            {
                method:'get',
                parameters: {filterName: saveFilterName},
                onSuccess: function(transport) {
                    var returnValue = transport.responseText;
                    if (returnValue.search(/----DuplicateFilterName----/) != -1){
                        CARBON.showConfirmationDialog(org_wso2_carbon_registry_activities_ui_jsi18n["are.you.sure.you.want.to.replace.search.filter"] + "&nbsp;<strong>'" +
                                                      saveFilterName + "'</strong> ",
                                function() {
                                    saveSearchFilter(userNameValue, pathValue, fromDateValue, toDateValue, filter, saveFilterName);
                                }, null);

                    } else {
                        saveSearchFilter(userNameValue, pathValue, fromDateValue, toDateValue, filter, saveFilterName);
                    }
                },
                onFailure: function() {
                    CARBON.showErrorDialog(org_wso2_carbon_registry_activities_ui_jsi18n["search.filter.was.not.saved"]);
                    return false;
                }
            });




    }, org_wso2_carbon_registry_activities_ui_jsi18n["session.timed.out"]);
}

function saveSearchFilter(userNameValue, pathValue, fromDateValue, toDateValue, filter, saveFilterName) {
    new Ajax.Request('../activities/saveActivitySearchFilter-ajaxprocessor.jsp',
    {
        method: 'get',
        parameters: {fromDate: fromDateValue, toDate: toDateValue, userName:userNameValue,path:pathValue,filter:filter,saveFilterName:saveFilterName},
        evalScripts: true,

        onSuccess: function() {
            CARBON.showInfoDialog(org_wso2_carbon_registry_activities_ui_jsi18n["successfully.saved.search.filter"]);
            $('#_saveFilterName').value = "";
            new Ajax.Updater('savedSearchFilterListDiv', '../activities/getSavedActivitySearchFilters-ajaxprocessor.jsp',{evalScripts:true});
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
//        document.getElementById('advancedSearchFormDiv').style.display = "";
        document.getElementById('activityForm').style.display = 'block';
        document.getElementById('activityReason').style.display = 'none';
//        document.getElementById('searchResuts').style.display = 'none';
        new Ajax.Updater('activityForm', '../activities/loadActivitySearchFilter-ajaxprocessor.jsp',
        {
            method:'get',
            parameters: {filterName: $('savedSearchFilterList').value},

            onSuccess: function(transport) {
                //document.getElementById('activityForm').innerHTML = transport.responseText;
                initDatePickers();
                //initRangeOperators();
                //initMiscFields();
                //fillMediaTypes();
                $('#_0').fcocus();
            }
        });

    }, org_wso2_carbon_registry_activities_ui_jsi18n["session.timed.out"]);
}

function deleteSearchFilter(filterName) {

     if (filterName == "None") {
         return;
     }

     sessionAwareFunction(function() {

         CARBON.showConfirmationDialog(org_wso2_carbon_registry_activities_ui_jsi18n["are.you.sure.you.want.to.delete.the.filter"] + "&nbsp;<strong>'" +
                                                              filterName + "'</strong> ",
        function() {
            new Ajax.Request('../activities/deleteActivitySearchFilter-ajaxprocessor.jsp',
            {
                method:'get',
                parameters: {filterName:filterName},

                onSuccess: function(transport) {
                new Ajax.Updater('savedSearchFilterListDiv', '../activities/getSavedActivitySearchFilters-ajaxprocessor.jsp',{evalScripts:true});
                initDatePickers();
                $('#_0').fcocus();
            } ,

                onFailure: function(transport) {
                    addSuccess = false;
                    CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.delete"] +
                        " <strong>'" +filterName + "'</strong>. " + transport.responseText);
                }
        }); },null);
     }, org_wso2_carbon_registry_activities_ui_jsi18n["session.timed.out"]);
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