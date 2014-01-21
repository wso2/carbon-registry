function processAddAssociation(resourcePath, assoType, associationPaths, fillingDiv, tempSpan) {
    sessionAwareFunction(function() {
        new Ajax.Request('../relations/association-list-ajaxprocessor.jsp',
        {
            method:'post',
            parameters:{path:resourcePath,type:assoType,associationPaths:associationPaths,todo:'add'},
            onSuccess: function(transport) {
                $(fillingDiv).innerHTML = transport.responseText;
                $(fillingDiv).appendChild(tempSpan);
                $(fillingDiv).style.display = "";
                if (fillingDiv == "associationsDiv") {
                    $('associationsIconExpanded').style.display = "";
                    $('associationsIconMinimized').style.display = "none";

                    YAHOO.util.Event.onAvailable('updateFix', function() {
                        $('associationsSum').style.display = "none";
                        $('associationsList').style.display = "";

                    });


                } else {
                    $('dependenciesIconExpanded').style.display = "";
                    $('dependenciesIconMinimized').style.display = "none";
                    YAHOO.util.Event.onAvailable('updateFix', function() {
                        $('dependenciesSum').style.display = "none";
                        $('dependenciesList').style.display = "";
                    });
                }
                dependencyTreeExpansionPath = associationPaths;

            },
            onFailure: function(transport) {
                CARBON.showErrorDialog(transport.responseText);
            }
        });
    }, org_wso2_carbon_registry_relations_ui_jsi18n["session.timed.out"]);
}

function loadAssociationDiv(resourcePath, assoType, page) {
    var fillingDiv = "associationsDiv";
    if (assoType == "depends") {
        fillingDiv = "dependenciesDiv";
    }
    var reason = "";
    if ($('updateFix')) {
        $('updateFix').parentNode.removeChild($('updateFix'));
    }
    var tempSpan = document.createElement('span');
    tempSpan.id = "updateFix";
    sessionAwareFunction(function() {
        new Ajax.Request('../relations/association-list-ajaxprocessor.jsp',
                {
                    method:'post',
                    parameters:{path:resourcePath,type:assoType,page:page},
                    onSuccess: function(transport) {
                        $(fillingDiv).innerHTML = transport.responseText;
                        $(fillingDiv).appendChild(tempSpan);
                        $(fillingDiv).style.display = "";
                        if (fillingDiv == "associationsDiv") {
                            $('associationsIconExpanded').style.display = "";
                            $('associationsIconMinimized').style.display = "none";

                            YAHOO.util.Event.onAvailable('updateFix', function() {
                                $('associationsSum').style.display = "none";
                                $('associationsList').style.display = "";

                            });


                        } else {
                            $('dependenciesIconExpanded').style.display = "";
                            $('dependenciesIconMinimized').style.display = "none";
                            YAHOO.util.Event.onAvailable('updateFix', function() {
                                $('dependenciesSum').style.display = "none";
                                $('dependenciesList').style.display = "";
                            });
                        }
                        dependencyTreeExpansionPath = associationPaths;

                    },
                    onFailure: function(transport) {
                        CARBON.showErrorDialog(transport.responseText);
                    }
                });
    }, org_wso2_carbon_registry_relations_ui_jsi18n["session.timed.out"]);
}

function changeTextVisibility(optionTYpe){
  var option = $('associationOptionList').value;
    if(option == "other") {
      $('type').style.visibility = "";
    } else {
        $('type').style.visibility = "hidden";
    }
}

function addAssociation(mainType) {

    //JS injection validation
    if(!validateTextForIllegal(document.getElementById('associationPaths'))) {
        CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+ "association path content"+" " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"]);
        return false;
    }

    var typeForm = document.forms[mainType];
    var addDivId = 'associationsAddDiv';
    var fillingDiv = 'associationsDiv';
    var assoType = 'depends';
    var reason = "";
    if ($('updateFix')) {
        $('updateFix').parentNode.removeChild($('updateFix'));
    }
    var tempSpan = document.createElement('span');
    tempSpan.id = "updateFix";
    var assoPathField = document.getElementById('associationPaths');
    if(mainType == "depForm"){
        	assoPathField = document.getElementById('depPaths');
    }
    
    if (mainType != "depForm") {
        //validate a non empty association type is selected
        if($('associationOptionList').value == "0") {
            CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+ "association type"+" " + org_wso2_carbon_registry_common_ui_jsi18n["cannot.empty"]);
            return false;
        }

        var ass_option = $('associationOptionList').value;
        var assTypeVal;

        if(ass_option == "other") {
            assoType = typeForm.type.value;
            assTypeVal = typeForm.type;
        } else {
          assoType = ass_option;
          assTypeVal = $('associationOptionList');
        }

        reason += validateForInput(assTypeVal, org_wso2_carbon_registry_relations_ui_jsi18n["type"]);
        if (reason == "") {
                reason += validateEmpty(assTypeVal, org_wso2_carbon_registry_relations_ui_jsi18n["type"]);
        }
        if (reason == "") {
            reason += validateNotExists(assoPathField.value);
        }

        if (reason == "") {
            assoType = assoType.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
            if (assoType == "") {
                reason = org_wso2_carbon_registry_relations_ui_jsi18n["association.type.cannot.contain.only.white.spaces"];
            }
        }

        if (assoType == "depends") {
            fillingDiv = 'dependenciesDiv';
        }
    } else {

        fillingDiv = 'dependenciesDiv';
        addDivId = "dependenciesAddDiv";
    }

    if (reason == "") {
        reason += validateForInput(assoPathField, org_wso2_carbon_registry_relations_ui_jsi18n["path"]);
    }
    if (reason == "") {
        reason += validateEmpty(assoPathField, org_wso2_carbon_registry_relations_ui_jsi18n["path"]);
    }
    if (reason == "") {
        reason += validateNotExists(assoPathField.value);
    }
    if (reason == "") {
        reason += validateAssocationNotExists($('resourcePath').value,
                            assoPathField.value, assoType);
    }

    if (reason == "") {
        assoPathField.value = assoPathField.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        if (assoPathField.value == "") {
            reason = org_wso2_carbon_registry_relations_ui_jsi18n["path.cannot.contain.only.white.spaces"];
        }
    }

    if (reason == "") {
        var resourcePath = $('resourcePath').value;
        var associationPaths = assoPathField.value;
        typeForm.type.value = "";
        $('associationOptionList').value = "0";
        assoPathField.value = "";
        showHideCommon(addDivId);
        if (assoType != "depends" && associationPaths.toString().indexOf(";") > 0) {
            var temp = associationPaths.split(";");
            for (i = 0; i < temp.length; i++) {
                if (!temp[i] || temp[i] == null || temp[i] == "") {
                    continue;
                }
                processAddAssociation(resourcePath, assoType, temp[i], fillingDiv, tempSpan);
            }
        } else {
            processAddAssociation(resourcePath, assoType, associationPaths, fillingDiv, tempSpan);
        }

    }
    else {
        CARBON.showWarningDialog(reason);
    }
}

var dependencyTreeExpansionPath = null;

function addDependency() {
    addAssociation('depForm');

}

function appendAssociation() {
    if ($('associationPathsHidden').value.length == 0) {
        return;    
    }
    var associationPath = $('associationPaths').value;
    associationPath = associationPath.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    if (associationPath.length == 0) {
        $('associationPaths').value = $('associationPathsHidden').value;
    } else {
        $('associationPaths').value = associationPath + ";" + $('associationPathsHidden').value;
    }
    dependencyTreeExpansionPath = $('associationPathsHidden').value;
}

function validateNotExists(pickedPath) {
    if (pickedPath.charAt(0) != '/') {
        // this is a not a registry path, so avoid validating
        if (pickedPath.indexOf("http:") == 0 || pickedPath.indexOf("https:") == 0) {
            return "";
        }
        return org_wso2_carbon_registry_relations_ui_jsi18n["resource.does.not.exist"]+ " <strong>" + pickedPath + "</strong>.";
    }
    var error = "";
    new Ajax.Request('../resources/resource_exists_ajaxprocessor.jsp',
    {
        method:'post',
        parameters: {pickedPath: pickedPath},
        asynchronous:false,
        onSuccess: function(transport) {
            var returnValue = transport.responseText;
            if (returnValue.search(/----ResourceExists----/) == -1) {
                error = org_wso2_carbon_registry_relations_ui_jsi18n["resource.does.not.exist"]+ " <strong>" + pickedPath + "</strong>.";
            }
        },
        onFailure: function() {

        }
    });
    return error;
}


function validateAssocationNotExists(source, destination, type) {
    var error = "";
    new Ajax.Request('../relations/asssociation_exists_ajaxprocessor.jsp',
    {
        method:'post',
        parameters: {path: source, destination: destination, type: type},
        asynchronous:false,
        onSuccess: function(transport) {
            var returnValue = transport.responseText;
            if (returnValue.search(/----AssocationExists----/) != -1) {
                error = org_wso2_carbon_registry_relations_ui_jsi18n["the.given"] +" " + (type == "depends"? org_wso2_carbon_registry_relations_ui_jsi18n["dependency"]: org_wso2_carbon_registry_relations_ui_jsi18n["association"]) + " " + org_wso2_carbon_registry_relations_ui_jsi18n["already.exists"] + " " +
                        org_wso2_carbon_registry_relations_ui_jsi18n["you.cant.have.duplicate"] + " " + (type == "depends"? org_wso2_carbon_registry_relations_ui_jsi18n["dependencies"]: org_wso2_carbon_registry_relations_ui_jsi18n["associations"]);
            }
        },
        onFailure: function() {

        }
    });
    return error;
}

function editDependencyPaths(mainType, resoPath, assocPaths)
{
    var typeForm = document.forms[mainType];
    fillingDiv = 'dependenciesDiv';
    addDivId = "dependenciesAddDiv";
    var assoType = 'depends';
    var reason = "";

    if ($('updateFix')) {
        $('updateFix').parentNode.removeChild($('updateFix'));
    }
    var tempSpan = document.createElement('span');
    tempSpan.id = "updateFix";

    /*resourcePath = resoPath;

    associationPaths = assocPaths;*/

    if (reason == "") {
        var resourcePath = $('resourcePath').value;
        var associationPaths = typeForm.associationPaths.value;
        typeForm.type.value = "";
        typeForm.associationPaths.value = "";

        new Ajax.Request('../relations/association-list-ajaxprocessor.jsp',
        {
            method:'post',
            parameters:{path:resourcePath,type:assoType,associationPaths:associationPaths,todo:'edit'},
            onSuccess: function(transport) {
                $(fillingDiv).innerHTML = transport.responseText;
                $(fillingDiv).appendChild(tempSpan);
                $(fillingDiv).style.display = "";

                if (fillingDiv == "associationsDiv") {
                    $('associationsIconExpanded').style.display = "";
                    $('associationsIconMinimized').style.display = "none";

                    YAHOO.util.Event.onAvailable('updateFix', function() {
                        $('associationsSum').style.display = "none";
                        $('associationsList').style.display = "";

                    });

                } else {

                    $('dependenciesIconExpanded').style.display = "";
                    $('dependenciesIconMinimized').style.display = "none";
                    YAHOO.util.Event.onAvailable('updateFix', function() {
                        $('dependenciesSum').style.display = "none";
                        $('dependenciesList').style.display = "";
                    });

                }
                editAssociationPaths('assoForm', resoPath, assocPaths);
            },
            onFailure: function(transport) {
                CARBON.showErrorDialog(transport.responseText);
            }
        });


    }
    else {
        CARBON.showWarningDialog(reason);
    }
}

function editAssociationPaths(mainType, resoPath, assocPaths)
{
    var typeForm = document.forms[mainType];
    fillingDiv = 'associationsDiv';
    addDivId = "associationsAddDiv";
    var assoType = typeForm.type.value;
    var reason = "";

    if ($('updateFix')) {
        $('updateFix').parentNode.removeChild($('updateFix'));
    }
    var tempSpan = document.createElement('span');
    tempSpan.id = "updateFix";

    /*resourcePath = resoPath;

    associationPaths = assocPaths;*/

    assoType = typeForm.type.value;

    if (reason == "") {
        var resourcePath = $('resourcePath').value;
        if (!resourcePath) {
            resourcePath = resoPath;
        }
        var associationPaths = typeForm.associationPaths.value;
        if (!associationPaths) {
            associationPaths = assocPaths;
        }
        typeForm.type.value = "";
        typeForm.associationPaths.value = "";

        new Ajax.Request('../relations/association-list-ajaxprocessor.jsp',
        {
            method:'post',
            parameters:{path:resourcePath,type:assoType,associationPaths:associationPaths,todo:'edit'},
            onSuccess: function(transport) {
                $(fillingDiv).innerHTML = transport.responseText;
                $(fillingDiv).appendChild(tempSpan);
                $(fillingDiv).style.display = "";

                if (fillingDiv == "associationsDiv") {
                    $('associationsIconExpanded').style.display = "";
                    $('associationsIconMinimized').style.display = "none";

                    YAHOO.util.Event.onAvailable('updateFix', function() {
                        $('associationsSum').style.display = "none";
                        $('associationsList').style.display = "";

                    });

                } else {
                    $('dependenciesIconExpanded').style.display = "";
                    $('dependenciesIconMinimized').style.display = "none";
                    YAHOO.util.Event.onAvailable('updateFix', function() {
                        $('dependenciesSum').style.display = "none";
                        $('dependenciesList').style.display = "";
                    });

                }
            },
            onFailure: function(transport) {
                CARBON.showErrorDialog(transport.responseText);
            }
        });

    }
    else {
        CARBON.showWarningDialog(reason);
    }
}

function removeAssociation(resourcePath, associationPaths, assoType, fillingDiv, status) {
    var promptMsg = org_wso2_carbon_registry_relations_ui_jsi18n["are.you.sure.you.want.to.delete.dependency"];
    if(fillingDiv == "associationsDiv" )     {
        promptMsg = org_wso2_carbon_registry_relations_ui_jsi18n["are.you.sure.you.want.to.delete.association"];
    }
    if (status != 1)
    {
        CARBON.showConfirmationDialog(promptMsg + " <strong>'" + associationPaths + "'</strong> " + org_wso2_carbon_registry_relations_ui_jsi18n["permanently"], function() {

            if ($('updateFix')) {
                $('updateFix').parentNode.removeChild($('updateFix'));
            }
            var tempSpan = document.createElement('span');
            tempSpan.id = "updateFix";

            new Ajax.Request('../relations/association-list-ajaxprocessor.jsp',
            {
                method:'post',
                parameters:{path:resourcePath,type:assoType,associationPaths:associationPaths,todo:'remove'},
                onSuccess: function(transport) {
                    $(fillingDiv).innerHTML = transport.responseText;
                    $(fillingDiv).appendChild(tempSpan);
                    $(fillingDiv).style.display = "";
                    if (fillingDiv == "associationsDiv") {
                        $('associationsIconExpanded').style.display = "";
                        $('associationsIconMinimized').style.display = "none";

                        YAHOO.util.Event.onAvailable('updateFix', function() {
                            $('associationsSum').style.display = "none";
                            $('associationsList').style.display = "";

                        });


                    } else {
                        $('dependenciesIconExpanded').style.display = "";
                        $('dependenciesIconMinimized').style.display = "none";
                        YAHOO.util.Event.onAvailable('updateFix', function() {
                            $('dependenciesSum').style.display = "none";
                            $('dependenciesList').style.display = "";
                        });

                    }

                },
                onFailure: function(transport) {
                    CARBON.showErrorDialog(transport.responseText);
                }
            });
        }, null);
    }
    else
    {
        if ($('updateFix')) {
            $('updateFix').parentNode.removeChild($('updateFix'));
        }
        var tempSpan = document.createElement('span');
        tempSpan.id = "updateFix";
        new Ajax.Request('../relations/association-list-ajaxprocessor.jsp',
        {
            method:'post',
            parameters:{path:resourcePath,type:assoType,associationPaths:associationPaths,todo:'remove'},
            onSuccess: function(transport) {
                $(fillingDiv).innerHTML = transport.responseText;
                $(fillingDiv).appendChild(tempSpan);
                $(fillingDiv).style.display = "";
                if (fillingDiv == "associationsDiv") {
                    $('associationsIconExpanded').style.display = "";
                    $('associationsIconMinimized').style.display = "none";

                    YAHOO.util.Event.onAvailable('updateFix', function() {
                        $('associationsSum').style.display = "none";
                        $('associationsList').style.display = "";

                    });

                } else {
                    $('dependenciesIconExpanded').style.display = "";
                    $('dependenciesIconMinimized').style.display = "none";
                    YAHOO.util.Event.onAvailable('updateFix', function() {
                        $('dependenciesSum').style.display = "none";
                        $('dependenciesList').style.display = "";
                    });
                }

                if (assoType != 'depends')
                {
                    removeAssociation(resourcePath, associationPaths, 'depends', 'dependenciesDiv', 1);
                }
            },
            onFailure: function(transport) {
                CARBON.showErrorDialog(transport.responseText);
            }
        });
    }
}

function showAssociationTree(toShow, resourcePath) {
    //This variable is use to address the cashe issue in IE
    var random = Math.floor(Math.random() * 2000);
    sessionAwareFunction(function() {
        var loadingContent = null;
        if (toShow == "depends") {
            loadingContent = "<div class=\"ajax-loading-message\">" +
                             "<img src=\"images/ajax-loader.gif\" align=\"top\"/>" +
                             "<span>" + org_wso2_carbon_registry_relations_ui_jsi18n["dependency.tree.loading"] + "</span>" +
                             "</div>";
            new Ajax.Request('../relations/assoTree-ajaxprocessor.jsp', {
                method: 'get',
                parameters: {type:"depends",random:random, path:resourcePath},
                onSuccess: function(transport) {
                    CARBON.showPopupDialog(loadingContent, org_wso2_carbon_registry_relations_ui_jsi18n["dependency.tree"], 500, true);
                    var dialog = $('dialog');
                    dialog.innerHTML = '<div style="padding-bottom:40px;">'+transport.responseText+'</div>';
                },
                onFailure: function() {
                    CARBON.showWarningDialog(org_wso2_carbon_registry_relations_ui_jsi18n["no.dependencies"]);
                }
            });
            //new Ajax.Updater("dialog", "../relations/assoTree-ajaxprocessor.jsp", {method:"get",parameters:{type:"depends",random:random, path:resourcePath} });
        } else if (toShow == "asso") {
            loadingContent = "<div class=\"ajax-loading-message\">" +
                             "<img src=\"images/ajax-loader.gif\" align=\"top\"/>" +
                             "<span>" + org_wso2_carbon_registry_relations_ui_jsi18n["association.tree.loading"] + "</span>" +
                             "</div>";
            new Ajax.Request('../relations/assoTree-ajaxprocessor.jsp', {
                method: 'get',
                parameters: {type:"asso",random:random, path:resourcePath},
                onSuccess: function(transport) {
                    CARBON.showPopupDialog(loadingContent, org_wso2_carbon_registry_relations_ui_jsi18n["association.tree"], 500, true);
                    var dialog = $('dialog');
                    dialog.innerHTML = '<div style="padding-bottom:40px;">'+transport.responseText+'</div>';
                },
                onFailure: function() {
                    CARBON.showWarningDialog(org_wso2_carbon_registry_relations_ui_jsi18n["no.associations"]);
                }
            });
            //new Ajax.Updater("dialog", "../relations/assoTree-ajaxprocessor.jsp", {method:"get",parameters:{type:"asso",random:random, path:resourcePath}});
        }

    }, org_wso2_carbon_registry_relations_ui_jsi18n["session.timed.out"]);

}
function showAssociations() {
    if ($('associationsIconExpanded').style.display == "none") {
        //We have to expand all and hide sum
        $('associationsIconExpanded').style.display = "";
        $('associationsIconMinimized').style.display = "none";
        $('associationsExpanded').style.display = "";
        $('associationsMinimized').style.display = "none";
    }
    else {
        $('associationsIconExpanded').style.display = "none";
        $('associationsIconMinimized').style.display = "";
        $('associationsExpanded').style.display = "none";
        $('associationsMinimized').style.display = "";
    }
}
function showDependencies() {
    if ($('dependenciesIconExpanded').style.display == "none") {
        //We have to expand all and hide sum
        $('dependenciesIconExpanded').style.display = "";
        $('dependenciesIconMinimized').style.display = "none";
        $('dependenciesExpanded').style.display = "";
        $('dependenciesMinimized').style.display = "none";
    }
    else {
        $('dependenciesIconExpanded').style.display = "none";
        $('dependenciesIconMinimized').style.display = "";
        $('dependenciesExpanded').style.display = "none";
        $('dependenciesMinimized').style.display = "";
    }
}
