var propertyOperationStarted = false;

String.prototype.startsWith = function(prefix) {
     return this.indexOf(prefix) == 0;
}

function setProperty() {
    if (propertyOperationStarted) {
        CARBON.showWarningDialog(org_wso2_carbon_registry_properties_ui_jsi18n["property.operation.in.progress"]);
        return;
    }
    propertyOperationStarted = true;
    sessionAwareFunction(function() {
        var reason="";
        reason +=validateEmpty(document.getElementById('propName'), org_wso2_carbon_registry_properties_ui_jsi18n["property.name"]);
//        if (reason == "") {
//            reason +=validateEmpty(document.getElementById('propValue'), org_wso2_carbon_registry_properties_ui_jsi18n["property.value"]);
//        }
        if (reason == "") {
            reason +=validateForInput(document.getElementById('propName'), org_wso2_carbon_registry_properties_ui_jsi18n["property.name"]);
        }
        if (reason == "") {
            reason +=validateForInput(document.getElementById('propValue'), org_wso2_carbon_registry_properties_ui_jsi18n["property.value"]);
        }
        var resourcePath = $('propRPath').value;
        var propertyName = $('propName').value;
        var propertyValue = $('propValue').value;

        if(reason == ""){
            propertyName = propertyName.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
            if (propertyName == "") {
                reason = org_wso2_carbon_registry_properties_ui_jsi18n["property.value.cannot.contain.only.white.spaces"];
            }

            propertyValue = propertyValue.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
//            if (propertyValue == "") {
//                reason = org_wso2_carbon_registry_properties_ui_jsi18n["property.value.cannot.contain.only.white.spaces"];
//            }
        }

        if(propertyName.startsWith("registry.")) {
            reason =  org_wso2_carbon_registry_properties_ui_jsi18n["property.name.cannot.be.hidden"];
        }
        //Check for the previously entered property
        var foundPropName = false;
        var allNodes = document.getElementById("propertiesList").getElementsByTagName("*");

        for (var i = 0; i < allNodes.length; i++) {
            if (YAHOO.util.Dom.hasClass(allNodes[i], "propEditNameSelector")) {
                if(allNodes[i].value == $('propName').value) reason += org_wso2_carbon_registry_properties_ui_jsi18n["duplicate.entry.please.choose.another.name"];
            }
        }

        if(reason!=""){
            CARBON.showWarningDialog(reason);
            return false;
        }else{
            cleanField($('propName'));
            cleanField($('propValue'));

            new Ajax.Request('../properties/properties-ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {path: resourcePath, name: propertyName, value: propertyValue},

                onSuccess: function(transport) {
                    $('resourceProperties').innerHTML = transport.responseText;
                    $('propertiesList').style.display = "";
                    $('propertiesIconExpanded').style.display = "";
                    $('propertiesIconMinimized').style.display = "none";
                    refreshMetadataSection(resourcePath);
                },

                onFailure: function(transport) {
                    CARBON.showErrorDialog(transport.responseText);
                    propertyOperationStarted = false;
                    return;
                }
            });
            showHideCommon('propertiesAddDiv');
        }
        return true;
    }, org_wso2_carbon_registry_properties_ui_jsi18n["session.timed.out"]);
    propertyOperationStarted = false;
}

function editProperty(rowid) {
    if (propertyOperationStarted) {
        CARBON.showWarningDialog(org_wso2_carbon_registry_properties_ui_jsi18n["property.operation.in.progress"]);
        return;
    }
    propertyOperationStarted = true;
    sessionAwareFunction(function() {
        var reason = "";

        var resourcePath = document.getElementById('propRPath_'+rowid).value;
        var oldPropertyName = document.getElementById('oldPropName_'+rowid).value;
        var propertyName = document.getElementById('propName_'+rowid);
        reason += validateEmpty(propertyName,org_wso2_carbon_registry_properties_ui_jsi18n["property.name"]);
        reason += validateForInput(propertyName,org_wso2_carbon_registry_properties_ui_jsi18n["property.name"]);
        var propertyValue = document.getElementById('propValue_'+rowid);
//        reason += validateEmpty(propertyValue,org_wso2_carbon_registry_properties_ui_jsi18n["property.value"]);
        reason += validateForInput(propertyValue,org_wso2_carbon_registry_properties_ui_jsi18n["property.value"]);

        //Check for the previously entered property
        var duplicatePropNameCount = 0;
        var allNodes = document.getElementById("propertiesList").getElementsByTagName("*");

        for (var i = 0; i < allNodes.length; i++) {
            if (YAHOO.util.Dom.hasClass(allNodes[i], "propEditNameSelector")) {
                if(allNodes[i].value == propertyName.value) {
                    duplicatePropNameCount = duplicatePropNameCount + 1;

                }
            }
        }

        if(duplicatePropNameCount > 1) {
            reason += org_wso2_carbon_registry_properties_ui_jsi18n["duplicate.entry.please.choose.another.name"];
        }

        if(reason==""){
            new Ajax.Request('../properties/properties-ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {path: resourcePath, oldName: oldPropertyName, name: propertyName.value, value: propertyValue.value},

                onSuccess: function(transport) {
                    $('resourceProperties').innerHTML = transport.responseText;
                    $('propertiesList').style.display = "";
                    $('propertiesIconExpanded').style.display = "";
                    $('propertiesIconMinimized').style.display = "none";
                    refreshMetadataSection(resourcePath);
                },

                onFailure: function(transport) {
                    CARBON.showErrorDialog(transport.responseText);
                    propertyOperationStarted = false;
                    return;
                }
            });
        }else{
            CARBON.showWarningDialog(reason, function() {
                $('propName_' + rowid).value = $('oldPropName_'+rowid).value;
            });
        }
    }, org_wso2_carbon_registry_properties_ui_jsi18n["session.timed.out"]);
    propertyOperationStarted = false;
}

function removeProperty(propertyName) {
    if (propertyOperationStarted) {
        CARBON.showWarningDialog(org_wso2_carbon_registry_properties_ui_jsi18n["property.operation.in.progress"]);
        return;
    }
    propertyOperationStarted = true;
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog(org_wso2_carbon_registry_properties_ui_jsi18n["are.you.sure.you.want.to.delete"]+ " <strong>'"+ propertyName +"'</strong> " + org_wso2_carbon_registry_properties_ui_jsi18n["permanently"], function(){
            var resourcePath = document.getElementById('propRPath').value;
            new Ajax.Request('../properties/properties-ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {path: resourcePath, name: propertyName, remove:'true'},

                onSuccess: function(transport) {
                    $('resourceProperties').innerHTML = transport.responseText;
                    $('propertiesList').style.display = "";
                    $('propertiesIconExpanded').style.display = "";
                    $('propertiesIconMinimized').style.display = "none";
                    refreshMetadataSection(resourcePath);
                },

                onFailure: function(transport) {
                    CARBON.showErrorDialog(transport.responseText);
                    propertyOperationStarted = false;
                    return;
                }
            });
        }, null);
    }, org_wso2_carbon_registry_properties_ui_jsi18n["session.timed.out"]);
    propertyOperationStarted = false;
}
function showProperties(){
	if($('propertiesIconExpanded').style.display == "none"){
	//We have to expand all and hide sum
	$('propertiesIconExpanded').style.display = "";
	$('propertiesIconMinimized').style.display = "none";
	$('propertiesExpanded').style.display = "";
	$('propertiesMinimized').style.display = "none";
	}
	else{
	$('propertiesIconExpanded').style.display = "none";
	$('propertiesIconMinimized').style.display = "";
	$('propertiesExpanded').style.display = "none";
	$('propertiesMinimized').style.display = "";
	}
}

function showRetention() {
    if ($('retentionIconExpanded').style.display == "none") {
        //We have to expand all and hide sum
        $('retentionIconExpanded').style.display = "";
        $('retentionIconMinimized').style.display = "none";
        $('retentionExpanded').style.display = "";
        $('retentionMinimized').style.display = "none";
    }
    else {
        $('retentionIconExpanded').style.display = "none";
        $('retentionIconMinimized').style.display = "";
        $('retentionExpanded').style.display = "none";
        $('retentionMinimized').style.display = "";
    }
}

function setRetentionProperties() {
    if (propertyOperationStarted) {
        CARBON.showWarningDialog(org_wso2_carbon_registry_properties_ui_jsi18n["property.operation.in.progress"]);
        return;
    }
    propertyOperationStarted = true;
    sessionAwareFunction(function() {
        var reason="";
        reason +=validateDate(document.getElementById('fromDate'), org_wso2_carbon_registry_properties_ui_jsi18n["from.date"]);
        if (reason == "") {
            reason +=validateDate(document.getElementById('toDate'), org_wso2_carbon_registry_properties_ui_jsi18n["to.date"]);
        }
        if (reason == "") {
            reason += validateToFromDate(document.getElementById("fromDate"), document.getElementById("toDate"));
        }

        if(reason!="") {
            CARBON.showWarningDialog(reason);
            return false;
        }

        var vResourcePath = $('resourcePathId').value;
        var vFromDate = $('fromDate').value;
        var vToDate = $('toDate').value;
        var vWriteLock = $('writeBoxId').checked;
        var vReadLock = $('readBoxId').checked;

        var vLockedOperations = "";
        if (vWriteLock == true) {
            vLockedOperations += "write";
        }
        if (vReadLock == true) {
            vLockedOperations += "delete";
        }

        new Ajax.Request('../properties/set_retention_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {path: vResourcePath, fromDate: vFromDate, toDate: vToDate, lockedOperations: vLockedOperations},

            onSuccess: function() {
                CARBON.showInfoDialog(org_wso2_carbon_registry_properties_ui_jsi18n["set.retention.successful"]);
            },

            onFailure: function(transport) {
                CARBON.showErrorDialog(org_wso2_carbon_registry_properties_ui_jsi18n["set.retention.failed"] + transport.responseText);
                return false;
            }
        });

        return true;
    }, org_wso2_carbon_registry_properties_ui_jsi18n["session.timed.out"]);
    propertyOperationStarted = false;
}

function resetRetentionProperties() {
    if (propertyOperationStarted) {
        CARBON.showWarningDialog(org_wso2_carbon_registry_properties_ui_jsi18n["property.operation.in.progress"]);
        return;
    }
    propertyOperationStarted = true;
    sessionAwareFunction(function() {

        var vResourcePath = $('resourcePathId').value;

        $('fromDate').value = "";
        $('toDate').value = "";
        $('writeBoxId').checked = false;
        $('readBoxId').checked = false;

        new Ajax.Request('../properties/set_retention_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {path: vResourcePath},

            onSuccess: function() {
                CARBON.showInfoDialog(org_wso2_carbon_registry_properties_ui_jsi18n["reset.retention.successful"]);
            },

            onFailure: function(transport) {
                CARBON.showErrorDialog(org_wso2_carbon_registry_properties_ui_jsi18n["reset.retention.failed"] + transport.responseText);
                return false;
            }
        });
        return true;
    }, org_wso2_carbon_registry_properties_ui_jsi18n["session.timed.out"]);
    propertyOperationStarted = false;
}
