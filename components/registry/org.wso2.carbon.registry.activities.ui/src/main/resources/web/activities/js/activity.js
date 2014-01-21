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