var browserName = navigator.appName;
var resourceType = "file";
var ASSOCIATION_TYPE01 = "depends";
var myEditor = null;
var textContentEditor = null;
var textContentUpdator = null;
var filterShown = false;
var isMediationLocalEntrySelected = false;

function showHide(toShowHide) {
    var resource_div = document.getElementById('add-resource-div');
    var folder_div = document.getElementById('add-folder-div');
    var link_div = document.getElementById('add-link-div');
    if (toShowHide == 'add-resource-div') {
        if (resource_div!= null && resource_div.style.display == 'block') {
            resource_div.style.display = 'none';
        }
        else {
            resource_div.style.display = 'block';
            if (folder_div!= null && folder_div.style.display == 'block') folder_div.style.display = 'none';
            if (link_div!=null && link_div.style.display == 'block') link_div.style.display = 'none';
        }
    }

    if (toShowHide == 'add-folder-div') {
        if (folder_div!=null && folder_div.style.display == 'block') {
            folder_div.style.display = 'none';
        }
        else {
            if(folder_div!=null) { 
            	folder_div.style.display = 'block'; 
            }
            if (resource_div!=null && resource_div.style.display == 'block') resource_div.style.display = 'none';
            if (link_div != null && link_div.style.display == 'block') link_div.style.display = 'none';
        }
    }

    if (toShowHide == 'add-link-div') {
        if (link_div!=null && link_div.style.display == 'block') {
            link_div.style.display = 'none';
        }
        else {
            if(link_div!=null){link_div.style.display = 'block';}
            if (resource_div!=null && resource_div.style.display == 'block') resource_div.style.display = 'none';
            if (folder_div !=null && folder_div.style.display == 'block') folder_div.style.display = 'none';
        }
    }
}

function showHideTreeView(path,obj) {
    sessionAwareFunction(function() {
        var stdView = document.getElementById('stdView');
        var treeView = document.getElementById('treeView');
        var random = getRandom();
        var clickedon = obj.id;
        if (stdView && YAHOO.util.Dom.hasClass(stdView,"stdView-notSelected") && clickedon=="stdView") {
            YAHOO.util.Dom.removeClass(stdView,"stdView-notSelected");
            YAHOO.util.Dom.addClass(stdView,"stdView-Selected");

            YAHOO.util.Dom.removeClass(treeView,"treeView-Selected");
            YAHOO.util.Dom.addClass(treeView,"treeView-notSelected");
            new Ajax.Updater('viewPanel', '../resources/standard_view_ajaxprocessor.jsp', { method: 'get', parameters: {path: path,random:random}, evalScripts:true });

        } else if(treeView && YAHOO.util.Dom.hasClass(treeView,"treeView-notSelected") && clickedon=="treeView") {
            YAHOO.util.Dom.removeClass(treeView,"treeView-notSelected");
            YAHOO.util.Dom.addClass(treeView,"treeView-Selected");

            YAHOO.util.Dom.removeClass(stdView,"stdView-Selected");
            YAHOO.util.Dom.addClass(stdView,"stdView-notSelected");
            new Ajax.Updater('viewPanel', '../resources/tree_view_ajaxprocessor.jsp', { method: 'get', parameters: {path: path, treeNavigationPath: path, reference: "compute",random:random}, evalScripts:true });
        }
    },org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function setTreeNavigationPath(treeNavigationPath, reference) {
    new Ajax.Request('../resources/tree_view_ajaxprocessor.jsp',
    {
        method:'get',
        parameters: {treeNavigationPath: treeNavigationPath, reference: reference, random:getRandom()},

        onSuccess: function() {
        },

        onFailure: function() {
        }
    });
}

function showHideResources(cont) {
    var random = getRandom();
    var pointA = document.getElementById('pointA');
    var resourceInfo = document.getElementById('resourceInfo');
    var resourceMain = document.getElementById('resourceMain');
    var showHide = document.getElementById('showHideId');
    var contraction=cont;

    if (pointA && YAHOO.util.Dom.hasClass(pointA,"hiddenToShow")) {
    	contraction = "exp";
        YAHOO.util.Dom.removeClass(pointA,"hiddenToShow");
        YAHOO.util.Dom.addClass(pointA,"showToHidden");
        truncateResourceNames();
        new Ajax.Request('../resources/set_contraction_ajaxprocessor.jsp',
        {
            method:'get',
            parameters: {contraction: contraction,random:random},

            onSuccess: function() {
            	
            },

            onFailure: function() {
            }
        });
        resourceMain.style.width = "100%";
	    resourceInfo.style.display = "none";
        showHide.innerHTML = "Show";
    } else {
        contraction = "min";
        YAHOO.util.Dom.removeClass(pointA,"showToHidden");
        YAHOO.util.Dom.addClass(pointA,"hiddenToShow");
        new Ajax.Request('../resources/set_contraction_ajaxprocessor.jsp',
        {
            method:'get',
            parameters: {contraction: contraction,random:random},

            onSuccess: function() {
            },

            onFailure: function() {
            }
        });
        resourceMain.style.width = "70%";
        resourceInfo.style.display = "";
        showHide.innerHTML = "Hide";
        truncateResourceNames();
    }
}
function showDescription(desc) {
    var des_div = document.getElementById(desc);
    if (des_div.style.display == 'none') {
        if (browserName == "Netscape")
        {
            des_div.style.display = 'table-row';
        }
        else
        {
            if (browserName == "Microsoft Internet Explorer")
            {
                des_div.style.display = 'block';
            }
            else
            {
                des_div.style.display = 'table-row';
            }
        }
    }
    else des_div.style.display = 'none';
}

function showHideEdit() {

    var normal_div = document.getElementById('descView');
    var edit_div = document.getElementById('descEdit');
    var edit_button = document.getElementById('editButton');

    if (normal_div.style.display == 'block') {
        normal_div.style.display = 'none';
        edit_div.style.display = 'block';
        edit_button.value = org_wso2_carbon_registry_resource_ui_jsi18n["save.description"];
    }
    else {
        normal_div.style.display = 'block';
        edit_div.style.display = 'none';
        edit_button.value = org_wso2_carbon_registry_resource_ui_jsi18n["edit.description"];
    }

}

/*function addUserPermission(pathToAuthorize) {
    var reason = "";

    var selectedObject = document.getElementById('actionToAuthorize');
    var selected = selectedObject.options[selectedObject.selectedIndex].value;
    
    var selectedObjectUser = document.getElementById('userToAuthorize');
    var selectedUser = selectedObjectUser.options[selectedObjectUser.selectedIndex].value;

    if (selected == 1) reason += org_wso2_carbon_registry_resource_ui_jsi18n["select.action.for.permission"] + "<br />";
    if (selectedUser == "-select-") reason += org_wso2_carbon_registry_resource_ui_jsi18n["select.user.for.permission"]+ "<br />";
    if (reason == "") {
        var userToAuthorize = document.getElementById('userToAuthorize').value;
        var actionToAuthorize = document.getElementById('actionToAuthorize').value;

        var permissionType = "1";
        if (document.getElementById('permissionDeny').checked) {
            permissionType = "2";
        }

        new Ajax.Request('../resources/add_user_permission_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {pathToAuthorize: pathToAuthorize, userToAuthorize: userToAuthorize, actionToAuthorize: actionToAuthorize, permissionType: permissionType},

            onSuccess: function() {
                refreshPermissionsSection(pathToAuthorize);
                CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["permission.applied.successfully"]);
            },

            onFailure: function(transport) {
                CARBON.showErrorDialog(transport.responseText);
            }
        });

    } else {
        CARBON.showWarningDialog(reason);
    }
}*/

function toggleFilterRow() {
    if (filterShown) {
        hideFilterRow();
        filterShown = false;
    } else {
        showFilterRow();
        filterShown = true;
    }
}

function showFilterRow() {
    // get the element inside the hidden box
    var filterRow = document.getElementById('filterRow');
    var filterRowClone = filterRow.cloneNode(true);

    // add the hidden box content to a new row before the permissionBtnRow
    var addPermissionBtnRowNode = document.getElementById('addPermissionBtnRow');
    addPermissionBtnRowNode.parentNode.insertBefore(filterRowClone, addPermissionBtnRowNode);

    // remove the hiddenBox content, as it causes duplicate 'id' values,
    // so could fail in calling 'getElementById'
    filterRow.parentNode.removeChild(filterRow);
}

function hideFilterRow() {
    // get the element inside the hidden box
    var filterRow = document.getElementById('filterRow');
    var filterRowClone = filterRow.cloneNode(true);

    // add the hidden box content to a new row before the permissionBtnRow
    var hiddenFilterBox = document.getElementById('hiddenFilterBox');
    hiddenFilterBox.appendChild(filterRowClone);

    

    // remove the hiddenBox content, as it causes duplicate 'id' values,
    // so could fail in calling 'getElementById'
    filterRow.parentNode.removeChild(filterRow);
}

/*function processPermissions(resourcePath)
{

    var pForm = document.forms["permissions"];
    var len = pForm.elements.length;
    var msgBody = "";
    var currentUser = "";
    for (i = 0; i < len; i++) {
        if (pForm.elements[i].type == "checkbox") {
            var perm = pForm.elements[i];
            if (pForm.elements[i].name.substring(0, 7) != "nonuser") {
                if (currentUser != perm.name) {
                    msgBody = msgBody + "|" + perm.name + ":" + perm.value + "^" + perm.checked;
                    currentUser = perm.name;
                } else {
                    msgBody = msgBody + ":" + perm.value + "^" + perm.checked;
                }
            }
        }
    }

    new Ajax.Request('../resources/change_user_permissions_ajaxprocessor.jsp',
    {
        method:'post',
        parameters: {permissionInput: msgBody, resourcePath: resourcePath},

        onSuccess: function() {
            refreshPermissionsSection(resourcePath);
            CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["permission.applied.successfully"]);
        },

        onFailure: function(transport) {
           CARBON.showErrorDialog(transport.responseText);
        }
    });
}*/

function addRolePermission(pathToAuthorize) {
    var reason = "";

    var selectedObject = document.getElementById('roleActionToAuthorize');
    var selected = selectedObject.options[selectedObject.selectedIndex].value;

    if (selected == 1) reason += org_wso2_carbon_registry_resource_ui_jsi18n["select.action.for.permission"];
    if (reason == "") {
        var roleToAuthorize = document.getElementById('roleToAuthorize').value;
        var actionToAuthorize = document.getElementById('roleActionToAuthorize').value;

        var permissionType = "1";
        if (document.getElementById('rolePermissionDeny').checked) {
            permissionType = "2";
        }

        sessionAwareFunction(function() {
            new Ajax.Request('../resources/add_role_permission_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {pathToAuthorize: pathToAuthorize, roleToAuthorize: roleToAuthorize, actionToAuthorize: actionToAuthorize, permissionType: permissionType, random:getRandom()},

                onSuccess: function() {
                    refreshPermissionsSection(pathToAuthorize);
                    CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["permission.applied.successfully"]);
                },

                onFailure: function(transport) {
                    CARBON.showErrorDialog(transport.responseText,
                            function() {window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=/";},
                            function() {window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=/";});
                }
            });
        }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    } else {
        CARBON.showWarningDialog(reason);
    }
}

function processRolePermissions(resourcePath)
{
    var pForm = document.forms["rolePermissions"];
    var len = pForm.elements.length;
    var msgBody = "";
    var currentUser = "";
    for (i = 0; i < len; i++) {
        if (pForm.elements[i].type == "checkbox") {
            var perm = pForm.elements[i];
            if (pForm.elements[i].name.substring(0, 7) != "nonuser") {
                if (currentUser != perm.name) {
                    msgBody = msgBody + "|" + perm.name + ":" + perm.value + "^" + perm.checked;
                    currentUser = perm.name;
                } else {
                    msgBody = msgBody + ":" + perm.value + "^" + perm.checked;
                }
            }
        }
    }
    sessionAwareFunction(function() {
        new Ajax.Request('../resources/change_role_permissions_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {permissionInput: msgBody, resourcePath: resourcePath, random:getRandom()},

            onSuccess: function() {
                refreshPermissionsSection(resourcePath);
                CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["permission.applied.successfully"]);
            },

            onFailure: function(transport) {
                CARBON.showErrorDialog(transport.responseText,
                        function() {window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=/";},
                        function() {window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=/";});
            }
        });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function displayContentAsText(resourcePath) {
    sessionAwareFunction(function() {
        new Ajax.Request('../resources/display_text_content_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {path: resourcePath,random:getRandom()},

            onSuccess: function(transport) {
                document.getElementById('generalContentDiv').innerHTML = transport.responseText;
            },

            onFailure: function(transport){
                if (trim(transport.responseText)) {
                    CARBON.showWarningDialog(org_wso2_carbon_registry_resource_ui_jsi18n["unsupported.media.type.to.display"] + ": " + transport.responseText);
                } else {
                    CARBON.showWarningDialog(org_wso2_carbon_registry_resource_ui_jsi18n["unsupported.media.type.to.display"]);
                }
            }
        });

        var textDiv = document.getElementById('generalContentDiv');
        textDiv.style.display = "block";
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function displayUploadContent(resourcePath) {
    sessionAwareFunction(function() {
        new Ajax.Request('../resources/upload_content_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {path: resourcePath, random:getRandom()},

            onSuccess: function(transport) {
                document.getElementById('generalContentDiv').innerHTML = transport.responseText;
            },

            onFailure: function(transport){
                if (trim(transport.responseText)) {
                    CARBON.showWarningDialog(org_wso2_carbon_registry_resource_ui_jsi18n["unsupported.media.type.to.upload"] + ": " + transport.responseText);
                } else {
                    CARBON.showWarningDialog(org_wso2_carbon_registry_resource_ui_jsi18n["unsupported.media.type.to.upload"]);
                }
            }
        });

        var textDiv = document.getElementById('generalContentDiv');
        textDiv.style.display = "block";
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function cancelTextContentDisplay() {
    document.getElementById('generalContentDiv').style.display = "none";
}

function displayEditContentAsText(resourcePath) {
    var myConfig = {
        height: '300px',
        width: '100%',
        dompath: false,
        focusAtStart: true,
        toolbar: {
            collapse: true,
            titlebar: false,
            draggable: false,
            buttons: [
                { group: 'fontstyle', label: org_wso2_carbon_registry_resource_ui_jsi18n["font.name.and.size"],
                    buttons: [
                        { type: 'select', label: 'Arial', value: 'fontname', disabled: true,
                            menu: [
                                { text: 'Arial', checked: true },
                                { text: 'Arial Black' },
                                { text: 'Comic Sans MS' },
                                { text: 'Courier New' },
                                { text: 'Lucida Console' },
                                { text: 'Tahoma' },
                                { text: 'Times New Roman' },
                                { text: 'Trebuchet MS' },
                                { text: 'Verdana' }
                            ]
                        },
                        { type: 'spin', label: '13', value: 'fontsize', range: [ 9, 75 ], disabled: true }
                    ]
                },
                { type: 'separator' },
                { group: 'textstyle', label: org_wso2_carbon_registry_resource_ui_jsi18n["font.style"],
                    buttons: [
                        { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["bold"], value: 'bold' },
                        { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["italic"], value: 'italic' },
                        { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["underline"], value: 'underline' },
                        { type: 'separator' },
                        { type: 'color', label: org_wso2_carbon_registry_resource_ui_jsi18n["font.color"], value: 'forecolor', disabled: true },
                        { type: 'color', label: org_wso2_carbon_registry_resource_ui_jsi18n["background.color"], value: 'backcolor', disabled: true }
                    ]
                },
                { type: 'separator' },
                { group: 'indentlist', label: org_wso2_carbon_registry_resource_ui_jsi18n["lists"],
                    buttons: [
                        { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["create.unordered.list"], value: 'insertunorderedlist' },
                        { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["create.ordered.list"], value: 'insertorderedlist' }
                    ]
                },
                { type: 'separator' },
                { group: 'insertitem', label: org_wso2_carbon_registry_resource_ui_jsi18n["insert.item"],
                    buttons: [
                        { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["html.link"], value: 'createlink', disabled: true }
                        /*{ type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["insert.image"], value: 'insertimage' }*/
                    ]
                }
            ]

        }
    };

    sessionAwareFunction(function() {
        new Ajax.Request('../resources/edit_text_content_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {path: resourcePath, random:getRandom()},

            onSuccess: function(transport) {
                document.getElementById('generalContentDiv').innerHTML = transport.responseText;
                textContentUpdator = new YAHOO.widget.SimpleEditor('editTextContentID', myConfig);
                textContentUpdator.render();
            },

            onFailure: function(transport){
                if (trim(transport.responseText)) {
                    CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["unsupported.media.type.to.edit"] + ": " + transport.responseText);
                } else {
                    CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["unsupported.media.type.to.edit"]);
                }
            }
        });

        var textDiv = document.getElementById('generalContentDiv');
        textDiv.style.display = "block";
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function updateTextContent(resourcePath,mediaType,override) {
    sessionAwareFunction(function() {
        var contentText = "";
        var radioObj = new Array();
        radioObj[0] = $('editTextContentIDRichText0');
        radioObj[1] = $('editTextContentIDRichText1');
        var selected = "";
        for(var i=0;i<radioObj.length;i++){
            if(radioObj[i].checked)selected = radioObj[i].value;
        }
        if(selected == 'rich'){
            if (textContentUpdator) {
                textContentUpdator.saveHTML();
                contentText = textContentUpdator.get('textarea').value;
                textContentUpdator.destroy();
                textContentUpdator = null;
            }
        } else{
            if (textContentUpdator) {
                textContentUpdator.destroy();
                textContentUpdator = null;
            }
            contentText = $('editTextContentIDPlain').value;
        }

        if (!contentText) {
            contentText = content;
        }
        document.getElementById('saveContentButtonID').disabled = true;
        new Ajax.Request('../resources/update_text_content_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {resourcePath: resourcePath, contentText: contentText, random:getRandom(),updateOverride:override},
                /* Here we redirect to appropriate list page for following mediatypes otherwise if user
                 edit the content which cause a change of the current resource location, the registry
                 browser will break due to unavailability of resource in current location.
                 */
                onSuccess: function() {
                    if ((mediaType == 'application/wsdl+xml') && isGovernanceListFeatureInstalled()) {
                        location.href = '../list/wsdl.jsp?region=region3&item=governance_list_wsdl_menu'
                    } else if ((mediaType == 'application/policy+xml') && isGovernanceListFeatureInstalled()) {
                        location.href = '../list/policy.jsp?region=region3&item=governance_list_policy_menu'
                    } else if ((mediaType == 'application/x-xsd+xml') && isGovernanceListFeatureInstalled()) {
                        location.href = '../list/schema.jsp?region=region3&item=governance_list_schema_menu'
                    } else if ((mediaType == 'application/vnd.wso2-service+xml') && isGovernanceListFeatureInstalled()) {
                        location.href = '../list/service.jsp?region=region3&item=governance_list_services_menu'
                    } else {
                        refreshContentSection(resourcePath);
                    }
                },

                onFailure: function(transport) {
                    if (transport.responseText.lastIndexOf("Another user has already modified this resource") != -1) {
                        CARBON.showConfirmationDialog("Another user has already modified this resource. Do you want to continue",
                            function(){
                                updateTextContent(resourcePath,mediaType,"true");
                            }
                            , function() {
                                document.getElementById('saveContentButtonID').disabled = false;
                            }
                            , true)
                    } else if (transport.responseText.lastIndexOf("Resource Retention") != -1) {
                        showRegistryError(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.update.retention"]);
                        document.getElementById('saveContentButtonID').disabled = false;
                    } else if (transport.responseText.lastIndexOf("Unable to access information from Session") != -1) {
                        showRegistryError("Unable to access information from Session, Please reload the page and try again");
                        document.getElementById('saveContentButtonID').disabled = false;
                    } else {
                        showRegistryError(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.update"] + " " + transport.responseText);
                        document.getElementById('saveContentButtonID').disabled = false;
                    }
                }
            });

        var textDiv = document.getElementById('generalContentDiv');
        textDiv.style.display = "block";
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function isGovernanceListFeatureInstalled() {
         new Ajax.Request('../resources/feature_installed_verification_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {feature_id: "/list/"},
                onSuccess: function(transport) {
                var returnValue = transport.responseText;
                 if (returnValue.search(/---FEATURE INSTALLED----/) != -1) {
                    return true;
                 } else {
                    return false;
                 }
                },
                onFailure: function(transport) {
                    CARBON.showWarningDialog(transport.responseText);
                    return false;
                }
            });
}



function cancelTextContentEdit() {
    document.getElementById('generalContentDiv').style.display = "none";
}

function displayDownload() {
    var textDiv = document.getElementById('generalContentDiv');
    textDiv.style.display = "none";
}

function showHidePromotion() {
    var proBox = document.getElementById('promotionBox');
    var state = "visible";
    var panel = "left";
    if (proBox.style.display == 'none') {
        proBox.style.display = "";
        proBox.parentNode.className = "promotionDiv";
    }
    else {
        proBox.style.display = 'none';
        proBox.parentNode.className = "promotionDivHidden";
        state = "hidden";
    }
    delete_cookie(panel);
    set_cookie(panel, state);
}

function showHideRight() {
    var rightBox = document.getElementById('rightColumn');
    var leftColoum = document.getElementById('leftColoum');
    var state = "visible";
    var panel = "right";
    if (rightBox.style.display == 'none') {
        rightBox.style.display = "";
        rightBox.className = "rightColumnSizer";
        leftColoum.className = "leftColoumSizer";
    }
    else {
        rightBox.style.display = 'none';
        rightBox.className = "rightColumnSizerHidden";
        leftColoum.className = "leftColoumSizerFull";
        state = "hidden";
    }
    delete_cookie(panel);
    set_cookie(panel, state);
}

function processDescription(resourcePath, todo) {

    var view_div = document.getElementById('descView');
    var edit_textarea = document.getElementById('descEdit');
    var edit_button = document.getElementById('editButton');
    var save_button = document.getElementById('saveButton');

    if (todo == 'editing') {
        save_button.style.display = "";
        edit_button.style.display = "none";
        view_div.style.display = "none";
        edit_textarea.style.display = "";
        
        edit_textarea.value = view_div.innerHTML;
       /*
        var myConfig = {
            height: '300px',
            width: '100%',
            dompath: false,
            focusAtStart: true,
            toolbar: {
                collapse: true,
                titlebar: false,
                draggable: false,
                buttons: [
                    { group: 'fontstyle', label: org_wso2_carbon_registry_resource_ui_jsi18n["font.name.and.size"],
                        buttons: [
                            { type: 'select', label: 'Arial', value: 'fontname', disabled: true,
                                menu: [
                                    { text: 'Arial', checked: true },
                                    { text: 'Arial Black' },
                                    { text: 'Comic Sans MS' },
                                    { text: 'Courier New' },
                                    { text: 'Lucida Console' },
                                    { text: 'Tahoma' },
                                    { text: 'Times New Roman' },
                                    { text: 'Trebuchet MS' },
                                    { text: 'Verdana' }
                                ]
                            },
                            { type: 'spin', label: '13', value: 'fontsize', range: [ 9, 75 ], disabled: true }
                        ]
                    },
                    { type: 'separator' },
                    { group: 'textstyle', label: org_wso2_carbon_registry_resource_ui_jsi18n["font.style"],
                        buttons: [
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["bold"], value: 'bold' },
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["italic"], value: 'italic' },
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["underline"], value: 'underline' },
                            { type: 'separator' },
                            { type: 'color', label: org_wso2_carbon_registry_resource_ui_jsi18n["font.color"], value: 'forecolor', disabled: true },
                            { type: 'color', label: org_wso2_carbon_registry_resource_ui_jsi18n["background.color"], value: 'backcolor', disabled: true }
                        ]
                    },
                    { type: 'separator' },
                    { group: 'indentlist', label: org_wso2_carbon_registry_resource_ui_jsi18n["lists"],
                        buttons: [
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["create.unordered.list"], value: 'insertunorderedlist' },
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["create.ordered.list"], value: 'insertorderedlist' }
                        ]
                    },
                    { type: 'separator' },
                    { group: 'insertitem', label: org_wso2_carbon_registry_resource_ui_jsi18n["insert.item"],
                        buttons: [
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["html.link"], value: 'createlink', disabled: true },
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["insert.image"], value: 'insertimage' }
                        ]
                    }
                ]

            }
        };

        myEditor = new YAHOO.widget.SimpleEditor('descEdit', myConfig);

        myEditor.render();
        */

    } else if (todo == 'saving') {
        save_button.style.display = "none";
        edit_button.style.display = "";
        view_div.style.display = "";

        //myEditor.saveHTML();
        var desc = document.getElementById("descEdit").value;//myEditor.get('textarea').value;
        document.getElementById("descEdit").style.display = "none";
        if(desc == "<br>" || desc==""){
            desc = "";
            view_div.style.display = "none";
        }else{
            view_div.style.display = "";
        }
        //myEditor.destroy();
        //myEditor = null;
        view_div.innerHTML = desc;

        sessionAwareFunction(function() {
            new Ajax.Request('../resources/set_description_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {description: desc, resourcePath: resourcePath, random:getRandom()},
                onSuccess: function() {

                },
                onFailure: function() {
                    CARBON.showWarningDialog(transport.responseText);
                    return false;
                }
            });
        }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);

    } else if (todo == 'cancel') {
        save_button.style.display = "none";
        edit_button.style.display = "";
        view_div.style.display = "";
        document.getElementById("descEdit").style.display = "none";        
        //myEditor.destroy();
        //myEditor = null;
    }


}

function loginOnEnter(e) {

    var keyCode;
    if (window.event) {
        keyCode = window.event.keyCode;
    } else {
        keyCode = e.which;
    }

    if (keyCode == 13) {
        login();
    }
}

function submitCustomViewUIForm(formID, action) {

    if(document.getElementById(formID) != null){
    	var params = $(formID).serialize();
    	params += "&";
    }else{
    	params = "";
    }
    params += "random="+ getRandom();

    new Ajax.Request(action,
    {
        method:'post',
        parameters: params,
        onSuccess: function(transport) {
            document.getElementById('customViewUIDiv').innerHTML = transport.responseText || org_wso2_carbon_registry_resource_ui_jsi18n["registry.not.responding"];
        },
        onFailure: function(transport) {
            showRegistryError(org_wso2_carbon_registry_resource_ui_jsi18n["form.processing.failed"] + transport.responseText);
        }
    });
}

function submitCustomAddUIForm(formID, action) {

    if(document.getElementById(formID) != null){
    	var params = $(formID).serialize();
    	params += "&";
    }else{
    	params = "";
    }
    params += "random="+ getRandom();

    new Ajax.Request(action,
    {
        method:'post',
        parameters: params,
        onSuccess: function(transport) {
            document.getElementById('customAddUIDiv').innerHTML = transport.responseText || org_wso2_carbon_registry_resource_ui_jsi18n["registry.not.responding"];
        },
        onFailure: function() {
            showRegistryError(org_wso2_carbon_registry_resource_ui_jsi18n["form.processing.failed"]);
        }
    });
}

/*function submitForm(formID, contentDiv, action) {

    //var options =
    //options.parameters = $(formID).serialize(true);
    //options.method = 'post';
    //options.onSuccess = function(transport) {
    //    var response = transport.responseText || org_wso2_carbon_registry_resource_ui_jsi18n["registry.not.responding"];
    //    $(contentDiv).innerHTML = response;
    //};

    //var params = document.getElementById(formID).serialize();

    new Ajax.Request(action,
    {
        method:'post',
        parameters: {myname: 'a', myphone: '1'},
        onSuccess: function(transport) {
            document.getElementById(contentDiv).innerHTML = transport.responseText || org_wso2_carbon_registry_resource_ui_jsi18n["registry.not.responding"];
        },
        onFailure: function() {
        }
    });

    //var action = $(formID).action;

    //var params = $(formID).serialize(true);
    //
    //new Ajax.Request(action, {
    //        method:'post',
    //        parameters:params});


    //Ajax.Request(action, options);

    //return false;

    //$(formID).request({
    //    onComplete: function(response, param){
    //        document.getElementById(contentDiv).innerHTML = response.responseText;
    //    }
    //})
}*/

function login() {
    var userName = document.getElementById('userName').value;
    var password = document.getElementById('password').value;

    new Ajax.Request('/wso2registry/system/signin',
    {
        method:'post',
        parameters: {userName: userName, password: password,random:getRandom()},
        onSuccess: function(transport) {
            var response = transport.responseText || org_wso2_carbon_registry_resource_ui_jsi18n["registry.not.responding"];
            if (response.indexOf(org_wso2_carbon_registry_resource_ui_jsi18n["error"]) == 0) {
                document.getElementById('loginMessage').innerHTML = response;
                document.getElementById('loginMessage').style.display = 'block';

            } else {
                prepareLoginBox();
                loginProgress();
                window.location.reload(false);
            }
        },
        onFailure: function() {
        }
    });

    //new Ajax.Updater('loginBox', '/wso2registry/system/signin', { method: 'post', parameters: {userName: userName, password: password} });
    //
    //document.refresh();
}

function prepareLoginBox() {
    document.getElementById('loginMessage').innerHTML = "";
    document.getElementById('loginMessage').style.display = 'none';
    document.getElementById('loginProgress').innerHTML = "";
    document.getElementById('loginProgress').style.display = 'none';
    document.getElementById('userName').value = "";
    document.getElementById('password').value = "";
}

function loginProgress() {

    //document.getElementById('loginProgress').innerHTML = org_wso2_carbon_registry_resource_ui_jsi18n["user.authenticated.successfully"];
    //document.getElementById('loginProgress').style.display='block';
}

/* Ajax Rating Functions */

function createVersion(resourcePath, viewMode, consumerID, targetDivID) {
    document.getElementById('checkpointDiv').style.display = "none";
    document.getElementById('checkpointWhileUpload').style.display = "";
    sessionAwareFunction(function() {
        new Ajax.Request('../resources/create_version_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {path: resourcePath, resourcePath: resourcePath,random:getRandom()},
            onSuccess: function(transport) {
                document.getElementById('metadataDiv').innerHTML = transport.responseText;
                showHideCommon('mainDetailsIconExpanded');showHideCommon('mainDetailsIconMinimized');showHideCommon('resourceMainDetails');showHideCommon('resourceMainDetailsMin');
                alternateTableRows('mainDetails', 'tableEvenRow', 'tableOddRow');
                CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["check.point.created"]);
                document.getElementById('checkpointDiv').style.display = "";
                document.getElementById('checkpointWhileUpload').style.display = "none";
                listComment(resourcePath);
            },
            onFailure: function() {
                CARBON.showWarningDialog(transport.responseText);
                document.getElementById('checkpointDiv').style.display = "";
                document.getElementById('checkpointWhileUpload').style.display = "none";
                return false;
            }
        });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function hideOthers(id, type) {
    var renamePanel = document.getElementById("rename_panel" + id);
    var movePanel = document.getElementById("move_panel" + id);
    var copyPanel = document.getElementById("copy_panel" + id);


    if (type == "rename") {
        if (movePanel.style.display != "none")   movePanel.style.display = "none";
        if (copyPanel.style.display != "none")   copyPanel.style.display = "none";
    }
    if (type == "copy") {
        if (movePanel.style.display != "none")   movePanel.style.display = "none";
        if (renamePanel.style.display != "none") renamePanel.style.display = "none";
    }
    if (type == "move") {
        if (copyPanel.style.display != "none")   copyPanel.style.display = "none";
        if (renamePanel.style.display != "none") renamePanel.style.display = "none";
    }
    if (type == "del") {
        if (copyPanel.style.display != "none")   copyPanel.style.display = "none";
        if (renamePanel.style.display != "none") renamePanel.style.display = "none";
        if (movePanel.style.display != "none")   movePanel.style.display = "none";
    }
}

function renameResource(parentPath, oldResourcePath, resourceEditDivID, wantedPage, type) {
    sessionAwareFunction(function() {
        var reason = "";
        document.getElementById(resourceEditDivID).value = ltrim(document.getElementById(resourceEditDivID).value);
        reason += validateEmpty(document.getElementById(resourceEditDivID), org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        if (reason == "") {
            reason += validateIllegal(document.getElementById(resourceEditDivID), org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateResourcePathAndLength(document.getElementById(resourceEditDivID));
        }
        var resourcePath= parentPath + '/' + document.getElementById(resourceEditDivID).value;
        resourcePath = resourcePath.replace("//", "/");
        if (reason == "") {
            reason += validateExists(resourcePath);
        }

        if (reason != "") {
            CARBON.showWarningDialog(reason);
            return false;

        } else {

            var newName = document.getElementById(resourceEditDivID).value;

            new Ajax.Request('../resources/rename_resource_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {parentPath: parentPath, oldResourcePath: oldResourcePath, newName: newName, type: type,random:getRandom()},

                onSuccess: function() {
                    displaySuccessMessage(resourcePath, org_wso2_carbon_registry_resource_ui_jsi18n["renamed"]);
                    refreshMetadataSection(parentPath);
                    fillContentSection(parentPath, wantedPage);
                },

                onFailure: function(transport) {
                    addSuccess = false;
                    CARBON.showErrorDialog(transport.responseText);
                }
            });
        }
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    loadData();
    return true;
}

function copyResource(parentPath, oldResourcePath, newPathId, resourceName, wantedPage) {
    sessionAwareFunction(function() {
        var reason = "";
        var newPath=document.getElementById(newPathId);
        newPath.value=ltrim(newPath.value);
        reason += validateEmpty(newPath, org_wso2_carbon_registry_resource_ui_jsi18n["destination.path"]);
        if (reason == "") {
            reason += validateIllegal(newPath, org_wso2_carbon_registry_resource_ui_jsi18n["destination.path"]);
        }
        var resourcePath= newPath.value + "/" + resourceName;
        resourcePath = resourcePath.replace("//", "/");
        if (reason == "") {
            reason += validateExists(resourcePath);
        }

        if (reason != "") {
            CARBON.showWarningDialog(reason);
            return false;
        } else {
            var destinationPath = newPath.value;

            new Ajax.Request('../resources/copy_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {parentPath: parentPath, oldResourcePath: oldResourcePath, destinationPath: destinationPath, resourceName:resourceName,random:getRandom()},

                onSuccess: function() {
                    displaySuccessMessage(resourcePath, org_wso2_carbon_registry_resource_ui_jsi18n["copied"]);
                    refreshMetadataSection(parentPath);
                    fillContentSection(parentPath, wantedPage);
                },

                onFailure: function(transport) {
                    CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["unable.to.copy.resource.to"] + " <strong>" + resourcePath + "</strong>. " + transport.responseText);
                    return false;
                }
            });
        }
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    loadData();
    return true;
}
function moveResource(parentPath, oldResourcePath, newPathId, resourceName, wantedPage) {
    sessionAwareFunction(function() {
        var reason = "";
        var newPath=document.getElementById(newPathId);
        newPath.value=ltrim(newPath.value);
        reason += validateEmpty(document.getElementById(newPathId), org_wso2_carbon_registry_resource_ui_jsi18n["destination.path"]);
        if (reason == "") {
            reason += validateIllegal(document.getElementById(newPathId), org_wso2_carbon_registry_resource_ui_jsi18n["destination.path"]);
        }
        var resourcePath= newPath.value + "/" + resourceName;
        resourcePath = resourcePath.replace("//", "/");
        if (reason == "") {
            reason += validateExists(resourcePath);
        }
        if (reason != "") {
            CARBON.showWarningDialog(reason);
            return false;
        } else {
            var destinationPath = document.getElementById(newPathId).value;
            new Ajax.Request('../resources/move_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {parentPath: parentPath, oldResourcePath: oldResourcePath, destinationPath: destinationPath,resourceName:resourceName,random:getRandom()},

                onSuccess: function() {
                    displaySuccessMessage(resourcePath, org_wso2_carbon_registry_resource_ui_jsi18n["moved"]);
                    refreshMetadataSection(parentPath);
                    fillContentSection(parentPath, wantedPage);
                },

                onFailure: function(transport) {
                    CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["unable.to.move.resource.to"] + " <strong>" + resourcePath + "</strong>. " + transport.responseText);
                    return false;
                }
            });

            fixResourceTable();
        }
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    loadData();
    return true;
}

function saveFriendlyName(userName) {
    var reason = "";

    reason += validateIllegal(document.getElementById('friendlyName'), org_wso2_carbon_registry_resource_ui_jsi18n["display.name"]);

    if (reason != "") {
        CARBON.showWarningDialog(reason);
        return false;
    } else {
        cleanField(document.getElementById('userReason'));
        var friendlyName = document.getElementById('friendlyName').value;
        new Ajax.Updater('friendlyNameDiv', '/wso2registry/system/saveFriendlyName', { method: 'post', parameters: {userName: userName, friendlyName: friendlyName,random:getRandom()} });
        showHideCommon('friendlyNameEdit');
        showHideCommon('friendlyNameView');
    }
    return true;
}
function saveNewPassword(userName) {
    var error = "";

    var newPassword = document.getElementById('newPassword').value;
    var newPasswordConfirm = document.getElementById('newPasswordConfirm').value;
    if (newPassword != newPasswordConfirm) {
        error = org_wso2_carbon_registry_resource_ui_jsi18n["verify.password"] + "<br />";
//        document.getElementById('newPassword').style.background = "Yellow";
//        document.getElementById('newPasswordConfirm').style.background = "Yellow";
    }
    error += validatePassword(document.getElementById('newPassword'));
    var passwordErrorMessage = document.getElementById('passwordErrorMessage');
    if (error != "") {
        CARBON.showWarningDialog(error);
    }
    else
    {
        passwordErrorMessage.style.display = "none";
        showHideCommon('passwordView');
        showHideCommon('passwordEdit1');
        showHideCommon('passwordEdit2');
        showHideCommon('passwordEdit3');
//        document.getElementById('newPassword').style.background = "White";
//        document.getElementById('newPasswordConfirm').style.background = "White";

        new Ajax.Updater({success:'passwordDiv',failure:'userReason'}, '/wso2registry/system/saveNewPassword', { method: 'post', parameters: {userName: userName, newPassword: newPassword,random:getRandom()} });
    }
}

// media type map to store file_extension -> media type pairs.
// these media type data will be retrieved upon the first request and stored in memory.
var mediaTypeMap = null;
var collectionMediaTypeMap = null;
var customUIMediaTypeMap = null;
var humanReadableMediaTypeMap = null;

var mediaTypeResponse = "txt:text,wsdl:wsdl/xml,xsd:xsd/xml,iml:idea/proj";
var collectionMediaTypeResponse;
var customUIMediaTypeResponse = "profiles:application/vnd.wso2-profiles+xml";
var humanReadableMediaTypeResponse = "";

function fillMediaTypes() {

    mediaTypeMap = new Object();

    new Ajax.Request('../resources/get_media_types_ajaxprocessor.jsp',
    {
        method:'get',
        parameters:{random:getRandom()},
        onSuccess: function(transport) {
            mediaTypeResponse = transport.responseText || "txt:text,wsdl:wsdl/xml";

            var mType = mediaTypeResponse.split(',');
            for (var i = 0; i < mType.length; i++) {
                var typeData = mType[i].split(':');
                if (typeData.length == 2) {
                    mediaTypeMap[typeData[0]] = typeData[1];
                }
            }
        },
        onFailure: function() {
            CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["could.not.get.media.type.information"]);
        }
    });
}

function fillCollectionMediaTypes() {

    collectionMediaTypeMap = new Object();

    new Ajax.Request('../resources/get_collection_media_types_ajaxprocessor.jsp',
    {
        method:'get',
        parameters:{random:getRandom()},
        onSuccess: function(transport) {
            collectionMediaTypeResponse = transport.responseText || "";

            var mType = collectionMediaTypeResponse.split(',');
            for (var i = 0; i < mType.length; i++) {
                var typeData = mType[i].split(':');
                if (typeData.length == 2) {
                    collectionMediaTypeMap[i] = typeData[1];
                }
            }
        },
        onFailure: function() {
            CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["could.not.get.collection.media.type.information"]);
        }
    });
}

function fillCustomUIMediaTypes() {

    customUIMediaTypeMap = new Object();

    new Ajax.Request('../resources/get_custom_ui_media_types_ajaxprocessor.jsp',
    {
        method:'get',
        parameters:{random:getRandom()},
        onSuccess: function(transport) {
            customUIMediaTypeResponse = transport.responseText || "mex:application/vnd.wso2-mex+xml";

            var mType = customUIMediaTypeResponse.split(',');
            for (var i = 0; i < mType.length; i++) {
                var typeData = mType[i].split(':');
                if (typeData.length == 2) {
                    customUIMediaTypeMap[i] = typeData[1];
                }
            }
        },
        onFailure: function() {
            CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["could.not.get.custom.ui.media.type.information"]);
        }
    });
}

function fillHumanReadableType() {

    humanReadableMediaTypeMap = new Object();

    new Ajax.Request('../resources/get_human_readable_mediatype_ajaxprocessor.jsp',
    {
        method:'get',
        parameters:{random:getRandom()},
        onSuccess: function(transport) {
             humanReadableMediaTypeResponse = transport.responseText || "";

            var mType = humanReadableMediaTypeResponse.split(',');
            for (var i = 0; i < mType.length; i++) {
                var typeData = mType[i].split(':');
                if (typeData.length == 2) {
                    humanReadableMediaTypeMap[trim(typeData[0])] = trim(typeData[1]);
                }
            }
        },
        onFailure: function() {
            CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["could.not.get.custom.ui.media.type.information"]);
        }
    });

}

function updateOther(controlId, otherVal) {
    if ($(controlId).value == otherVal) {
        $(controlId + "Other").style.display = "";
    } else {
        $(controlId + "Other").style.display = "none";
        $(controlId + "OtherValue").value = "";
    }
}

function loadMediaTypes() {

    if (mediaTypeMap == null) {
        fillMediaTypes();
    }
    if (customUIMediaTypeMap == null) {
        fillCustomUIMediaTypes();
    }
    if (collectionMediaTypeMap == null) {
        fillCollectionMediaTypes();
    }
    if(humanReadableMediaTypeMap == null){
        fillHumanReadableType();
    }

}

function getMediaType(fileExtension) {

    if (mediaTypeMap == null) {
        fillMediaTypes();
    }
    if(humanReadableMediaTypeMap == null){
        fillHumanReadableType();
    }

    if (humanReadableMediaTypeMap[mediaTypeMap[fileExtension.toLowerCase()]] != null) {
        return humanReadableMediaTypeMap[mediaTypeMap[fileExtension.toLowerCase()]];
    } else {
        return mediaTypeMap[fileExtension.toLowerCase()]
    }
    return null;
}

function getCustomUIMediaTypes() {

    if (customUIMediaTypeMap == null) {
        fillCustomUIMediaTypes();
    }

    return customUIMediaTypeMap;
}

function getCollectionMediaTypes() {

    if (collectionMediaTypeMap == null) {
        fillCollectionMediaTypes();
    }

    return collectionMediaTypeMap;
}

function fillResourceDetails() {

    var filepath = document.getElementById('resourceFile').value;

    var filename = "";
    if (filepath.indexOf("\\") != -1) {
        filename = filepath.substring(filepath.lastIndexOf('\\') + 1, filepath.length);
    } else {
        filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length);
    }

    document.getElementById('resourceName').value = filename;
    var extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length);

    var mediaType = "";
    if (extension.length > 0) {
        mediaType = getMediaType(extension);
        if (mediaType == undefined) {
            mediaType = "";
        }
    }

    document.getElementById('resourceMediaType').value = mediaType;
}

function fillResourceUploadDetails() {
    var filepath = document.getElementById('uResourceFile').value;

    var filename = "";
    if (filepath.indexOf("\\") != -1) {
        filename = filepath.substring(filepath.lastIndexOf('\\') + 1, filepath.length);
    } else {
        filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length);
    }

    document.getElementById('uResourceName').value = filename;
    var extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length);

    var mediaType = "";
    if (extension.length > 0) {
        mediaType = getMediaType(extension);
        if (mediaType == undefined) {
            mediaType = "";
        }
    }

    document.getElementById('uResourceMediaType').value = mediaType;
}

function fillResourceImportDetails() {

    var filepath = document.getElementById('irFetchURL').value;

    var filename = "";
    if (filepath.indexOf("\\") != -1) {
        filename = filepath.substring(filepath.lastIndexOf('\\') + 1, filepath.length);
		filename = filename.replace("?", ".");
	} else {
        filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length);
		filename = filename.replace("?", ".");
    }

    document.getElementById('irResourceName').value = unescape(filename);
    var extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length);

    var mediaType = "";
    if (extension.length > 0) {
        mediaType = getMediaType(extension);
        if (mediaType == undefined) {
            mediaType = "";
        }
    }
	else {
		extension = filename.substring(filename.lastIndexOf("?") + 1, filename.length);
	    if (extension.length > 0) {
	        mediaType = getMediaType(extension);
    	    if (mediaType == undefined) {
        	    mediaType = "";
	        }
    	}
	}

    document.getElementById('irMediaType').value = mediaType;
}

function fillResourceDetailsForURLs() {

    var filepath = document.getElementById('fetchURLID').value;

    var filename = "";
    filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length);

    document.getElementById('resourceName').value = filename;
    var extension = filename.substring(filename.lastIndexOf("?") + 1, filename.length);
    if (extension.indexOf("=") > 0) {
        extension = extension.substring(0, extension.lastIndexOf("="));
    }

    var mediaType = "";
    if (extension.length > 0) {
        mediaType = getMediaType(extension);
        if (mediaType == undefined) {
            mediaType = "";
        }
    }

    document.getElementById('resourceMediaType').value = mediaType;
}

function submitUploadUpdatedContentForm() {

    var rForm = document.forms["updateUploadForm"];
    /* Validate the form before submit */

    var reason = "";
    if (rForm.upload.value.length == 0) {
        reason += validateEmpty(rForm.upload, org_wso2_carbon_registry_resource_ui_jsi18n["file.path"]);
    }

    if (reason != "") {
        CARBON.showWarningDialog(reason);
        return false;
    } else {
        rForm.submit();
    }
    return true;
}

/**
 * We want to submit this form to different urls based on how the resource content is given.
 */
function submitResourceAddForm() {

    var rForm = document.forms["resourceupload"];
    /* Validate the form before submit */

    var reason = "";
    if (document.getElementById('contentFile').checked) {
        reason += validateEmpty(rForm.upload, org_wso2_carbon_registry_resource_ui_jsi18n["file.path"]);
    }
    else {
        reason += validateEmpty(rForm.fetchURL, org_wso2_carbon_registry_resource_ui_jsi18n["content.url"]);
    }
    if (reason == "") {
        reason += validateEmpty(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
    }
    if (reason == "") {
        reason += validateForInput(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
    }
    if (reason == "") {
        reason += validateForInput(rForm.mediaType, org_wso2_carbon_registry_resource_ui_jsi18n["media.type"]);
    }
    if (reason == "") {
        reason += validateForInput(rForm.description, org_wso2_carbon_registry_resource_ui_jsi18n["description"]);
    }
    if (reason == "") {
        reason += validateDescriptionLength(rForm.description);
    }
    var resourcePath= rForm.path.value + '/' + rForm.filename.value;
    resourcePath = resourcePath.replace("//", "/");
    if (reason == "") {
        reason += validateExists(resourcePath);
    }

    if (reason != "") {
        CARBON.showWarningDialog(reason);
        return false;
    } else {
        if (document.getElementById('contentURL').checked) {
            rForm.encoding = "application/x-www-form-urlencoded";
            rForm.enctype = "application/x-www-form-urlencoded";
            rForm.action = "/wso2registry/system/fetchResource";

        } else if (document.getElementById('contentFile').checked) {
            rForm.encoding = "multipart/form-data";
            rForm.enctype = "multipart/form-data";
            rForm.action = "/wso2registry/system/addResource";

        } else if (document.getElementById('contentFill').checked) {
            rForm.encoding = "application/x-www-form-urlencoded";
            rForm.enctype = "application/x-www-form-urlencoded";
            rForm.action = "/wso2registry/custom/fetchResource";
        }

        showHide('add-resource-div');
        rForm.submit();
    }
    return true;
}
function viewAddLinkUI() {
    var addSelector = document.getElementById('addLinkMethodSelector');
    var selectedValue = addSelector?(addSelector.options[addSelector.selectedIndex].value):"symlink";

    var symlinkUI = document.getElementById('symlinkContentUI');
    var remotelinkUI = document.getElementById('remotelinkContentUI');
    if (selectedValue == "symlink") {
        symlinkUI.style.display = "";
        remotelinkUI.style.display = "none";
    } else if (selectedValue == "remotelink") {
        symlinkUI.style.display = "none";
        remotelinkUI.style.display = "";
    }
    if ($('add-folder-div').style.display != "none") $('add-folder-div').style.display = "none";
    if ($('add-resource-div').style.display != "none") $('add-resource-div').style.display = "none";
}
function viewAddResourceUI() {
    var addSelector = document.getElementById('addMethodSelector');
    var selectedValue = addSelector.options[addSelector.selectedIndex].value;

    var uploadUI = document.getElementById('uploadContentUI');
    var importUI = document.getElementById('importContentUI');
    var textUI = document.getElementById('textContentUI');
    var customUI = document.getElementById('customContentUI');
    if (selectedValue == "upload") {

        uploadUI.style.display = "";
        importUI.style.display = "none";
        textUI.style.display = "none";
        customUI.style.display = "none";
    } else if (selectedValue == "import") {

        uploadUI.style.display = "none";
        importUI.style.display = "";
        textUI.style.display = "none";
        customUI.style.display = "none";
    } else if (selectedValue == "text") {

        uploadUI.style.display = "none";
        importUI.style.display = "none";
        customUI.style.display = "none";
        // text editor

        var myConfig = {
            height: '300px',
            width: '100%',
            dompath: false,
            focusAtStart: true,
            toolbar: {
                collapse: true,
                titlebar: false,
                draggable: false,
                buttons: [
                    { group: 'fontstyle', label: org_wso2_carbon_registry_resource_ui_jsi18n["font.name.and.size"],
                        buttons: [
                            { type: 'select', label: 'Arial', value: 'fontname', disabled: true,
                                menu: [
                                    { text: 'Arial', checked: true },
                                    { text: 'Arial Black' },
                                    { text: 'Comic Sans MS' },
                                    { text: 'Courier New' },
                                    { text: 'Lucida Console' },
                                    { text: 'Tahoma' },
                                    { text: 'Times New Roman' },
                                    { text: 'Trebuchet MS' },
                                    { text: 'Verdana' }
                                ]
                            },
                            { type: 'spin', label: '13', value: 'fontsize', range: [ 9, 75 ], disabled: true }
                        ]
                    },
                    { type: 'separator' },
                    { group: 'textstyle', label: org_wso2_carbon_registry_resource_ui_jsi18n["font.style"],
                        buttons: [
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["bold"], value: 'bold' },
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["italic"], value: 'italic' },
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["underline"], value: 'underline' },
                            { type: 'separator' },
                            { type: 'color', label: org_wso2_carbon_registry_resource_ui_jsi18n["font.color"], value: 'forecolor', disabled: true },
                            { type: 'color', label: org_wso2_carbon_registry_resource_ui_jsi18n["background.color"], value: 'backcolor', disabled: true }
                        ]
                    },
                    { type: 'separator' },
                    { group: 'indentlist', label: org_wso2_carbon_registry_resource_ui_jsi18n["lists"],
                        buttons: [
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["create.unordered.list"], value: 'insertunorderedlist' },
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["create.ordered.list"], value: 'insertorderedlist' }
                        ]
                    },
                    { type: 'separator' },
                    { group: 'insertitem', label: org_wso2_carbon_registry_resource_ui_jsi18n["insert.item"],
                        buttons: [
                            { type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["html.link"], value: 'createlink', disabled: true }
                            /*{ type: 'push', label: org_wso2_carbon_registry_resource_ui_jsi18n["insert.image"], value: 'insertimage' }*/
                        ]
                    }
                ]

            }
        };

        if (!textContentEditor) {
            textContentEditor = new YAHOO.widget.SimpleEditor('trContent', myConfig);
            textContentEditor.render();
        }
        document.getElementById('trMediaType').value =
            (humanReadableMediaTypeMap["text/plain"] == null)?"text/plain":humanReadableMediaTypeMap["text/plain"];
        textUI.style.display = "";

    } else if (selectedValue == "custom") {

        uploadUI.style.display = "none";
        importUI.style.display = "none";
        textUI.style.display = "none";
        customUI.style.display = "";
    }
    if ($('add-folder-div').style.display != "none") $('add-folder-div').style.display = "none";
    if ($('add-link-div').style.display != "none") $('add-link-div').style.display = "none";
}
function submitUploadContentForm() {
    sessionAwareFunction(function() {
        var rForm = document.forms["resourceUploadForm"];
        /* Validate the form before submit */
        var filePath = rForm.upload;
        var reason = "";

        if (filePath.value.length == 0) {
            reason +=  org_wso2_carbon_registry_resource_ui_jsi18n["file.path.not.filled"] + "<br />";
        }
        if (reason == "") {
            reason += validateEmpty(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateIllegal(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateResourcePathAndLength(rForm.filename);
        }
        if (reason == "") {
            reason += validateForInput(rForm.mediaType, org_wso2_carbon_registry_resource_ui_jsi18n["media.type"]);
        }
        if (reason == "") {
            reason += validateForInput(rForm.description, org_wso2_carbon_registry_resource_ui_jsi18n["description"]);
        }
        if (reason == "") {
            reason += validateDescriptionLength(rForm.description);
        }
        var resourcePath= rForm.path.value + '/' + rForm.filename.value;
        resourcePath = resourcePath.replace("//", "/");
        if (reason == "") {
            reason += validateExists(resourcePath);
        }

        if (reason != "") {
            document.getElementById('add-resource-div').style.display = "";
            document.getElementById('whileUpload').style.display = "none";
            CARBON.showWarningDialog(reason);
            showHide('add-resource-div');
            return false;
        } else {
            if (("application/wsdl+xml" == rForm.mediaType.value) || ("application/x-xsd+xml" == rForm.mediaType.value) || ("application/policy+xml" == rForm.mediaType.value)) {
                var el = document.createElement("input");
                el.type = "hidden";
                el.name = "symlinkLocation";
                el.value = document.getElementById("path").value;
                rForm.appendChild(el);

            }
            document.getElementById('add-resource-div').style.display = "none";
            rForm.submit();
        }
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    loadData();
    return true;
}
function resetResourceForms(){
    var addSelector = document.getElementById('addMethodSelector');
    addSelector.selectedIndex = 0;

    document.getElementById('uploadContentUI').style.display = "";
    document.getElementById('importContentUI').style.display = "none";
    document.getElementById('textContentUI').style.display = "none";
    document.getElementById('customContentUI').style.display = "none";

}

function resetLinkForms(){
    var addSelector = document.getElementById('addLinkMethodSelector');
    if (addSelector) {
        addSelector.selectedIndex = 0;
    }

    document.getElementById('symlinkContentUI').style.display = "";
    document.getElementById('remotelinkContentUI').style.display = "none";

}

function whileUpload(){
    if(document.getElementById('add-resource-div')!=null){
    	document.getElementById('add-resource-div').style.display = "none";
    }
    if(document.getElementById('add-link-div')!=null) {
    	document.getElementById('add-link-div').style.display = "none";
    }
    if(document.getElementById('whileUpload')!=null){
    	document.getElementById('whileUpload').style.display = "";
    }

}
function submitImportContentForm() {

    sessionAwareFunction(function() {
        var rForm = document.forms["resourceImportForm"];

        /* Validate the form before submit */

        var reason = "";
        reason += validateEmpty(rForm.fetchURL, org_wso2_carbon_registry_resource_ui_jsi18n["url"]);
        if (reason == "") {
            reason += validateEmpty(rForm.resourceName, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateIllegal(rForm.resourceName, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateResourcePathAndLength(rForm.resourceName);
        }
        if (reason == "") {
            reason += validateForInput(rForm.mediaType, org_wso2_carbon_registry_resource_ui_jsi18n["media.type"]);
        }
        if (reason == "") {
            reason += validateForInput(rForm.description, org_wso2_carbon_registry_resource_ui_jsi18n["description"]);
        }
        if (reason == "") {
            reason += validateDescriptionLength(rForm.description);
        }
        var resourcePath= rForm.path.value + '/' + rForm.resourceName.value;
        resourcePath = resourcePath.replace("//", "/");
        if (reason == "") {
            reason += validateExists(resourcePath);
        }

        if (reason != "") {
            CARBON.showWarningDialog(reason);
            document.getElementById('add-resource-div').style.display = "";
            document.getElementById('whileUpload').style.display = "none";
            return false;
        }

        var parentPath = document.getElementById('irParentPath').value;
        var resourceName = document.getElementById('irResourceName').value;
        var mediaType = document.getElementById('irMediaType').value;
        var description = document.getElementById('irDescription').value;
        var fetchURL = document.getElementById('irFetchURL').value;

        var isAsync = "false";

        // If this is a wsdl we need to make a async call to make sure we dont timeout cause wsdl
        // validation takes long.
        var params;
        if ((mediaType == "application/wsdl+xml") || (mediaType == "application/x-xsd+xml")  || (mediaType == "application/policy+xml")) {
            //                    isAsync = "true";
            params = {parentPath: parentPath, resourceName: resourceName, mediaType: mediaType, description: description, fetchURL: fetchURL, isAsync : isAsync, symlinkLocation: parentPath,random:getRandom()};
        } else {
            params = {parentPath: parentPath, resourceName: resourceName, mediaType: mediaType, description: description, fetchURL: fetchURL, isAsync : isAsync,random:getRandom()};
        }

        new Ajax.Request('../resources/import_resource_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: params,

            onSuccess: function() {
                refreshMetadataSection(parentPath);
                refreshContentSection(parentPath);
                //document.getElementById('add-resource-div').style.display = "";
                document.getElementById('whileUpload').style.display = "none";
                CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["successfully.uploaded"]);
            },

            onFailure: function(transport) {
                //refreshMetadataSection(parentPath);
                //refreshContentSection(parentPath);
                document.getElementById('whileUpload').style.display = "none";
                CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["unable.to.upload"] + transport.responseText,loadData);
            }
        });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function submitTextContentForm() {
    sessionAwareFunction(function() {
        var rForm = document.forms["textContentForm"];
        /* Validate the form before submit */

        var reason = "";
        reason += validateEmpty(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        if (reason == "") {
            reason += validateForInput(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateIllegal(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateResourcePathAndLength(rForm.filename);
        }
        if (reason == "") {
            reason += validateForInput(rForm.mediaType, org_wso2_carbon_registry_resource_ui_jsi18n["media.type"]);
        }
        if (reason == "") {
            reason += validateForInput(rForm.description, org_wso2_carbon_registry_resource_ui_jsi18n["description"]);
        }
        if (reason == "") {
            reason += validateDescriptionLength(rForm.description);
        }
        var resourcePath= rForm.path.value + '/' + rForm.filename.value;
        resourcePath = resourcePath.replace("//", "/");
        if (reason == "") {
            reason += validateExists(resourcePath);
        }

        if (reason != "") {
            CARBON.showWarningDialog(reason);
            document.getElementById('add-resource-div').style.display = "";
            document.getElementById('whileUpload').style.display = "none";
            return false;
        }

        var parentPath = document.getElementById('trParentPath').value;
        var fileName = document.getElementById('trFileName').value;
        var mediaType = document.getElementById('trMediaType').value;
        var description = document.getElementById('trDescription').value;
        var content = '';

        var radioObj = document.textContentForm.richText;
        var selected = "";
        for(var i=0;i<radioObj.length;i++){
            if(radioObj[i].checked)selected = radioObj[i].value;
        }
        if(selected == 'rich'){
            if (textContentEditor) {
                textContentEditor.saveHTML();
                content = textContentEditor.get('textarea').value;
                textContentEditor.destroy();
                textContentEditor = null;
            }
        } else{
            if (textContentEditor) {
                textContentEditor.destroy();
                textContentEditor = null;
            }
            content = $('trPlainContent').value;
        }

        new Ajax.Request('../resources/add_text_resource_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {parentPath: parentPath, fileName: fileName, mediaType: mediaType, description: description, content: content,random:getRandom()},

            onSuccess: function() {
                refreshMetadataSection(parentPath);
                refreshContentSection(parentPath);
                document.getElementById('whileUpload').style.display = "none";
                CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["successfully.added.text.content"],loadData);
            },

            onFailure: function(transport) {
                document.getElementById('whileUpload').style.display = "none";
                CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.add.text.content"] + transport.responseText,loadData);
            }
        });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    return true;
}

function submitSymlinkContentForm() {

    if(!validateTextForIllegal(document.forms["symlinkContentForm1"].targetpath)){
      CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+ "symlink content"+" " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"]);
        document.getElementById('add-link-div').style.display = "";
        document.getElementById('whileUpload').style.display = "none";
      return false;
    }

    sessionAwareFunction(function() {
        var rForm = document.forms["symlinkContentForm1"];

        /* Validate the form before submit */
        var reason = "";
        reason += validateEmpty(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        if (reason == "") {
            reason += validateForInput(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateIllegal(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateResourcePathAndLength(rForm.filename);
        }
        if (reason == "") {
            reason += validateEmpty(rForm.targetpath, org_wso2_carbon_registry_resource_ui_jsi18n["path"]);
        }
        if (reason == "" && rForm.targetpath.value == '/') {
            reason += org_wso2_carbon_registry_resource_ui_jsi18n["unable.create.symlink.to.root"];
        }
        var resourcePath= rForm.path.value + '/' + rForm.filename.value;
        resourcePath = resourcePath.replace("//", "/");
        if (reason == "") {
            reason += validateExists(resourcePath);
        }

        if (reason == "") {
            if (validateExists("/_system/local/repository/components/org.wso2.carbon.registry/mount") == "") {
                reason = org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.create.symbolic.link"] + ". " + org_wso2_carbon_registry_resource_ui_jsi18n["unauthorized.to.add.links"];
            }
        }

        if (reason != "") {
            CARBON.showWarningDialog(reason);
            document.getElementById('add-link-div').style.display = "";
            document.getElementById('whileUpload').style.display = "none";
            return false;
        }

        var parentPath = document.getElementById('srParentPath').value;
        var name = document.getElementById('srFileName').value;
        var targetPath = document.getElementById('srPath').value;
        new Ajax.Request('../resources/add_symlink_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {parentPath: parentPath, name: name, targetPath: targetPath,random:getRandom()},

            onSuccess: function() {
                refreshMetadataSection(parentPath);
                refreshContentSection(parentPath);
                document.getElementById('whileUpload').style.display = "none";
                CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["successfully.created.symbolic.link"],loadData);
            },

            onFailure: function(transport) {
                document.getElementById('whileUpload').style.display = "none";
                CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.create.symbolic.link"] + transport.responseText,loadData);
            }
        });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    return true;
}

function submitRemotelinkContentForm() {
    sessionAwareFunction(function() {
        var rForm = document.forms["remotelinkContentForm"];

        /* Validate the form before submit */
        var reason = "";
        reason += validateEmpty(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        if (reason == "") {
            reason += validateForInput(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateIllegal(rForm.filename, org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateResourcePathAndLength(rForm.filename);
        }

        if (reason == "") {
            if (validateExists("/_system/local/repository/components/org.wso2.carbon.registry/mount") == "") {
                reason = org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.create.remote.link"] + ". " + org_wso2_carbon_registry_resource_ui_jsi18n["unauthorized.to.add.links"];
            }
        }

        if (reason != "") {
            CARBON.showWarningDialog(reason);
            document.getElementById('add-link-div').style.display = "";
            document.getElementById('whileUpload').style.display = "none";
            return false;
        }
        var parentPath = document.getElementById('rrParentPath').value;
        var name = document.getElementById('rrFileName').value;

        var selector = document.getElementById('rrInstance');
        var instance = selector.options[selector.selectedIndex].value;
        var targetPath = document.getElementById('rrTargetPath').value;
        new Ajax.Request('../resources/add_remotelink_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {parentPath: parentPath, name: name, instance: instance, targetPath: targetPath,random:getRandom()},

            onSuccess: function() {
                refreshMetadataSection(parentPath);
                refreshContentSection(parentPath);
                document.getElementById('whileUpload').style.display = "none";
                CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["successfully.created.remote.link"]);
            },

            onFailure: function(transport) {
                CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.create.remote.link"] + transport.responseText,loadData);
            }
        });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    return true;
}

/*
 function removeForwardSlashes(name){
 var newName = name.slice(1);

 if(newName.startsWith('/')) {
 newName=removeForwardSlashes(newName);
 }
 else {
 alert(newName);
 return(newName);
 }
 }
 */

function fixParentPath(parentPath) {
    sessionAwareFunction(function() {
        if (validateExists(parentPath) == "" &&  parentPath.lastIndexOf("/") > -1) {
            parentPath = fixParentPath(parentPath.substring(0, parentPath.lastIndexOf("/")));
        }
        if (parentPath == "") {
            parentPath = "/";
        }
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    return parentPath;
}

var deleteConfirms = 0;
function deleteResource(pathToDelete, parentPath) {
    if(deleteConfirms != 0){
        return;
    }
    deleteConfirms++;
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog(
                org_wso2_carbon_registry_resource_ui_jsi18n["are.you.sure.you.want.to.delete"] +
                " <strong>'" + pathToDelete + "'</strong> " +
                org_wso2_carbon_registry_resource_ui_jsi18n["permanently"], function() {
            deleteConfirms = 0;
            var addSuccess = true;
            new Ajax.Request('../resources/delete_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {pathToDelete: pathToDelete,random:getRandom()},

                onSuccess: function() {
                    // This is to handle deleting of recursive symlinks
                    var newParentPath = fixParentPath(parentPath);
                    if (newParentPath != parentPath) {
                        window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + newParentPath.replace(/&/g, "%26");
                        // Return is required to stop processing of remainder.
                        return;
                    }
                    refreshMetadataSection(parentPath);
                    refreshContentSection(parentPath);

                    //Following code removed for fixing REGISTRY-968
//
//                    pathlength = parentPath.length;
//                    pathlength = pathlength-1;
//
//                    if ((pathToDelete.length - pathlength) > 25)
//                    {
//                        tempPath = pathToDelete.substring(pathlength,pathlength+25);
//                    }
//                    else
//                    {
//                        tempPath = pathToDelete.substring(pathlength);
//                    }
//
//                    if (document.getElementById(tempPath) == null)
//                    {
//                        removeAssociation(parentPath,pathToDelete,'depends','dependenciesDiv',1);
//                    }
//                    else
//                    {
//                        assoT = document.getElementById(tempPath).innerHTML;
//                        assoT = assoT.replace(/^\s+/,"");
//                        assoT = assoT.replace(/\s+$/,"");
//                        assoT = assoT.replace(" ","");
//                        assoT = assoT.replace("<br/>","");
//                        removeAssociation(parentPath,pathToDelete,assoT,'associationsDiv',1);
//                    }
                },

                onFailure: function(transport) {
                    addSuccess = false;
                    CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.delete"] +
                        " <strong>'" + pathToDelete + "'</strong>. " + transport.responseText);
                }
            });
        },function() {
            deleteConfirms = 0;
        }, function() {
            deleteConfirms = 0;
        });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    loadData();
}

function submitCollectionAddForm() {
    sessionAwareFunction(function() {
        var parentPath = document.getElementById('parentPath').value;
        var collectionName = document.getElementById('collectionName').value;
        var mediaTypeObj =  document.getElementById('collectionMediaType');
        var mediaType = mediaTypeObj[mediaTypeObj.selectedIndex].value;
        if (mediaType == "other") {
            mediaType = document.getElementById('mediaTypeOtherValue').value;
        }
        var description = document.getElementById('colDesc').value;

        var reason = "";
        reason += validateEmpty(document.getElementById('collectionName'), org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        if (reason == "") {
            reason += validateIllegal(document.getElementById('collectionName'), org_wso2_carbon_registry_resource_ui_jsi18n["name"]);
        }
        if (reason == "") {
            reason += validateResourcePathAndLength(document.getElementById('collectionName'));
        }
        if (reason == "") {
            reason += validateForInput(document.getElementById('colDesc'), org_wso2_carbon_registry_resource_ui_jsi18n["description"]);
        }
        if (reason == "") {
            reason += validateDescriptionLength(document.getElementById('colDesc'));
        }
        var resourcePath= document.getElementById('parentPath').value + '/' + document.getElementById('collectionName').value;
        resourcePath.replace("///","/").replace("//","/");
        if (reason == "") {
            reason += validateExists(resourcePath);
        }

        if (document.getElementById('collectionName').value.indexOf('/') == 0) {
            document.getElementById('collectionName').value = document.getElementById('collectionName').value.slice(1);
        }
        if (document.getElementById('collectionName').value.indexOf('//') == 0) {
            document.getElementById('collectionName').value = document.getElementById('collectionName').value.slice(2);
        }
        if (document.getElementById('collectionName').value.indexOf('///') == 0) {
            document.getElementById('collectionName').value = document.getElementById('collectionName').value.slice(3);
        }
        if (document.getElementById('collectionName').value.indexOf('////') == 0) {
            document.getElementById('collectionName').value = document.getElementById('collectionName').value.slice(4);
        }


        if (reason != "") {
            CARBON.showWarningDialog(reason);
            return false;
        }

        var addSuccess = true;
        var random = getRandom();
        new Ajax.Request('../resources/add_collection_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {parentPath: parentPath, collectionName: collectionName, mediaType: mediaType, description: description ,random:random},

            onSuccess: function() {
                refreshMetadataSection(parentPath);
                refreshContentSection(parentPath);
                CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["successfully.added.collection"],
                        function() {window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" +
                                                      (parentPath + "/" + collectionName).replace("///","/").replace("//", "/").replace(/&/g, "%26")},
                        function() {window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" +
                                                      (parentPath + "/" + collectionName).replace("///","/").replace("//", "/").replace(/&/g, "%26")});
            },

            onFailure: function(transport) {
                addSuccess = false;
                CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.add.collection"] + transport.responseText,loadData);
            }
        });

    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    loadData();
    return true;
}

function refreshMetadataSection(path) {
    var random = getRandom();
    new Ajax.Updater('metadataDiv', '../resources/metadata_ajaxprocessor.jsp', { method: 'get', parameters: {path: path,random:random}, evalScripts:true });
}

function viewStandardContentSection(path) {
    sessionAwareFunction(function() {
        var random = getRandom();
        new Ajax.Updater('contentDiv', '../resources/content_ajaxprocessor.jsp', { method: 'get', parameters:
            {path: path, mode: 'standard',random: random}, evalScripts: true });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function viewStandardContentSectionWithNoEdit(path) {
    sessionAwareFunction(function() {
        var random = getRandom();
        new Ajax.Updater('contentDiv', '../resources/content_ajaxprocessor.jsp', { method: 'get', parameters:
            {path: path, mode: 'standard',random: random, hideEdit: 'true'}, evalScripts: true });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function viewCustomContentSection(path) {
    sessionAwareFunction(function() {
        refreshContentSection(path)
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function refreshContentSection(path) {
    var random = getRandom();
    new Ajax.Updater('contentDiv', '../resources/content_ajaxprocessor.jsp', { method: 'get', parameters:
    {path: path, random:random}, onComplete:loadData, evalScripts: true });
}

function fillContentSection(path, pageNumber, viewMode, consumerID, targetDivID) {
    sessionAwareFunction(function() {
        var random = getRandom();
        new Ajax.Updater('contentDiv', '../resources/content_ajaxprocessor.jsp', { method: 'get',onComplete:loadData, parameters: {path: path, requestedPage: pageNumber, mode: 'standard',resourceViewMode:viewMode,resourcePathConsumer:consumerID,targetDivID:targetDivID,random:random} });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    loadData();
}

function refreshPermissionsSection(path) {
    sessionAwareFunction(function() {
        var random = getRandom();
        new Ajax.Request('../resources/permissions_ajaxprocessor.jsp', {
            method: 'get',
            parameters: {path: path,random:random},
            onSuccess: function(transport) {
                var perDiv = $('permissionsDiv');
                perDiv.innerHTML = transport.responseText;
                YAHOO.util.Event.onAvailable('perExpanded', function() {

                    //adding searchable drop-box component
                    makeSelectRoleSearchable();
                    makeSelectActionSelect2styled();

                    $('perIconExpanded').style.display = "";
                    $('perIconMinimized').style.display = "none";
                    $('perExpanded').style.display = "";
                });
            }
        });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);

    // new Ajax.Updater('permissionsDiv', './permissions_ajaxprocessor.jsp', { method: 'get', parameters: {path: path} });

    //YAHOO.util.Event.onAvailable('perExpanded',function(){showHideCommon('perIconExpanded');showHideCommon('perIconMinimized');showHideCommon('perExpanded');showHideCommon('perMinimized');})


}

//function to make role drop-box component, searchable
function makeSelectRoleSearchable(){
    jQuery('#roleToAuthorize').select2({
        width: "263px",
        placeholder: "-Select-"
    });
}

//function to make action drop-box component, select2 styled
function makeSelectActionSelect2styled(){
    jQuery('#roleActionToAuthorize').select2({
        minimumResultsForSearch: -1,
        width: "120px",
        placeholder: "-Select-"
    });
}

function submitUserAddForm() {
    var reason = "";
    reason += validateIllegal(document.getElementById('newUserName'), org_wso2_carbon_registry_resource_ui_jsi18n["user.name"]);
    if (reason == "") {
        reason += validateEmpty(document.getElementById('passwordMain'), org_wso2_carbon_registry_resource_ui_jsi18n["password"]);
    }
    if (document.getElementById('passwordMain').value != document.getElementById('confirmedPassword').value) {
//        document.getElementById('confirmedPassword').style.background = "Yellow";
//        document.getElementById('passwordMain').style.background = "Yellow";
        if (reason == "") {
            reason += org_wso2_carbon_registry_resource_ui_jsi18n["please.make.sure.passwords.are.matching"] + "<br />";
        }
    }
    else {
//        document.getElementById('confirmedPassword').style.background = "White";
//        document.getElementById('passwordMain').style.background = "White";
    }
    if (reason == "") {
        reason += validateForInput(document.getElementById('friendlyName'), org_wso2_carbon_registry_resource_ui_jsi18n["display.name"]);
    }

    if (reason != "") {
        CARBON.showWarningDialog(reason);
        return false;
    }
    else {
        document.forms["peopleAddForm"].submit();
        showHideCommon('userAddBox');
        return true;
    }
}
function submitRoleAddForm() {
    var reason = "";

    reason += validateIllegal(document.getElementById('newRoleName'), org_wso2_carbon_registry_resource_ui_jsi18n["role.name"]);

    if (reason != "") {
        CARBON.showWarningDialog(reason);
        return false;
    }
    else {
        document.forms["roleAddForm"].submit();
        showHideCommon('addRoleBox');
        return true;
    }
}

function doGreeting() {
    datetoday = new Date();
    timenow = datetoday.getTime();
    datetoday.setTime(timenow);
    thehour = datetoday.getHours();
    if (thehour > 18) display = org_wso2_carbon_registry_resource_ui_jsi18n["evening"];
    else if (thehour > 12) display = org_wso2_carbon_registry_resource_ui_jsi18n["afternoon"];
    else display = org_wso2_carbon_registry_resource_ui_jsi18n["morning"];
    var greeting = (org_wso2_carbon_registry_resource_ui_jsi18n["good"] + " " + display);
    document.write(greeting);
}

function getCommand(mediaType, command, params, resultDiv) {
    params["mediaType"] = mediaType;
    params["command"] = command;
    params["random"] = getRandom();
    new Ajax.Updater(resultDiv, '/wso2registry/custom', { method: 'get', evalJS:false, parameters:params });
}

function postCommand(mediaType, command, params, resultDiv) {
    params["mediaType"] = mediaType;
    params["command"] = command;
    params["random"] = getRandom();
    new Ajax.Updater(resultDiv, '/wso2registry/custom', { method: 'post', evalJS:false, parameters:params });
}

function generateNewUI(parentPath) {
    sessionAwareFunction(function() {
        var mediaType = document.getElementById('customMediaTypeID').value;
        if (mediaType == "other") {
             if(!validateTextForIllegal(document.getElementById('customMediaTypeIDOtherValue'))) {
               CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+ "media type content"+" " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"]);
               return false;
               }
            mediaType = document.getElementById('customMediaTypeIDOtherValue').value;
        }
        new Ajax.Updater('customAddUIDiv', '../resources/custom_add_ajaxprocessor.jsp', { method: 'get', evalScripts:true, parameters: {mediaType:mediaType, parentPath:parentPath,random:getRandom()} });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
    loadData();
}

function generateCustomCreateView(mediaType, viewName, parentPath) {

    new Ajax.Updater('customNewContent', '/wso2registry/custom/create', { method: 'get', evalJS:false, parameters: {mediaType:mediaType, view:viewName, parentPath:parentPath,random:getRandom()} });
}

function generateNewUIView(mediaType, viewName, parentPath) {

    new Ajax.Updater('customNewContent', '/wso2registry/system/getResourceCreationUI', { method: 'get', evalJS:false, parameters: {mediaType:mediaType, viewName:viewName, parentPath:parentPath,random:getRandom()} });
}

function fillCustomContentCreationUI(resourcePath) {

    new Ajax.Updater('customNewContent', '/wso2registry/system/getResourceCreationUI', { method: 'get', parameters: {resourcePath:resourcePath,random:getRandom()} });
}

/* This function will preform the disable and enabaling of the two input fiels on resource adding form */
function resourceFrom(type)
{
    resourceType = type;

    var normal_props_div = document.getElementById('normalProps');
    var custom_new_div = document.getElementById('customNewUISection');
    /*var text_new_div = document.getElementById('textContentUISection');*/

    /* Get radio button and file form field objects */
    /*var contentFile = document.getElementById('contentFile');*/
    var resourceFile = document.getElementById('resourceFile');

    /*var contentURL = document.getElementById('contentURL');*/
    var fetchURLID = document.getElementById('fetchURLID');

    /* Get the help text content */
    var fileHelpText = document.getElementById('fileHelpText');
    var urlHelpText = document.getElementById('urlHelpText');

    if (type == 'file') {

        resourceFile.removeAttribute('disabled');
        fetchURLID.setAttribute('disabled', '');

        fileHelpText.style.color = "Black";
        urlHelpText.style.color = "Gray";

        normal_props_div.style.display = "block";
        custom_new_div.style.display = "none";

    } else if (type == 'url') {

        resourceFile.setAttribute('disabled', '');
        fetchURLID.removeAttribute('disabled');

        fileHelpText.style.color = "Gray";
        urlHelpText.style.color = "Black";

        normal_props_div.style.display = "block";
        custom_new_div.style.display = "none";

    } else if (type = 'fill') {

        resourceFile.setAttribute('disabled', '');
        fetchURLID.setAttribute('disabled', '');
        //fetchURLID.style.background="Gray";

        fileHelpText.style.color = "Gray";
        urlHelpText.style.color = "Gray";

        normal_props_div.style.display = "none";
        custom_new_div.style.display = "block";
    }
}

function visualizeXML(path, type) {

    //This value is passed to address the cashing issue in IE
    var loadingContent = '<div class="ajax-loading-message"> <img src="../resources/images/ajax-loader.gif" align="top"/> <span>' +
            org_wso2_carbon_registry_resource_ui_jsi18n["xml.visualizer.loading"] + '</span> </div>';
    CARBON.showPopupDialog(loadingContent,
            org_wso2_carbon_registry_resource_ui_jsi18n["xml.visualizer"], 500, false);
    navigateToResourceInXMLVisualizer(path, path, type);
}

function navigateToResourceInXMLVisualizer(rootPath, path, type) {
    sessionAwareFunction(function() {
        var dialog = $('dialog');
        dialog.innerHTML = "<div style='background-color:#a9a9a9;text-align:right;height:34px;width:750px;'>" +
                "<input type='button' value='" + org_wso2_carbon_registry_resource_ui_jsi18n["back.to.parent"] +
                "' onclick='navigateToResourceInXMLVisualizer(\"" + rootPath + "\", \"" + rootPath + "\", \"" + type + "\")' " +
                "style='margin-right: 5px; margin-top: 5px;' /></div>" +
                    "<iframe frameborder='0' scrolling='auto' width='750px' height='440px' " +
                "src='../resources/xml_resource_visualizer_ajaxprocessor.jsp?rootPath=" + rootPath +
                "&path=" + path + "&type=" + type + "'></iframe>";
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function setResourceTreeExpansionPath(path, onSuccessCallback) {
    if (!path || path == null) {
        return;
    }
    new Ajax.Request('../resources/set_resource_tree_expansion_path_ajaxprocessor.jsp', {
        method: 'get',
        parameters: {path:path,random:getRandom()},
        onSuccess: function(transport) {
            if (onSuccessCallback) {
                onSuccessCallback();
            }
        }
    });
}

function showResourceTreeWithLoadFunction(loadFunction, textBoxId, onOKCallback, rootPath, relativeRoot, displayRootPath) {

    //This value is passed to address the cashing issue in IE
    var loadingContent = '<div class="ajax-loading-message"> <img src="../resources/images/ajax-loader.gif" align="top"/> <span>' + org_wso2_carbon_registry_resource_ui_jsi18n["resource.tree.loading"] + '</span> </div>';
    CARBON.showPopupDialog(loadingContent, org_wso2_carbon_registry_resource_ui_jsi18n["resource.tree"], 500, false);
    var random = getRandom();
    if (onOKCallback) {
        if (typeof onOKCallback == "function") {
            onOKCallback = onOKCallback.toString().substring('function '.length);
            onOKCallback = onOKCallback.substring(0, onOKCallback.indexOf("("));
            if (rootPath) {
                if (relativeRoot) {
                    new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                        method: 'get',
                        parameters: {textBoxId:textBoxId,rootPath:rootPath,relativeRoot:relativeRoot,displayRootPath:displayRootPath,onOKCallback:onOKCallback,random:random},
                        onSuccess: function(transport) {
                            var dialog = $('dialog');
                            dialog.innerHTML = transport.responseText;
                            if (loadFunction) {
                                loadSubTree(rootPath, 'root', textBoxId, 'false');
                            }
                        }
                    });
                } else {
                    new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                        method: 'get',
                        parameters: {textBoxId:textBoxId,rootPath:rootPath,displayRootPath:displayRootPath,onOKCallback:onOKCallback,random:random},
                        onSuccess: function(transport) {
                            var dialog = $('dialog');
                            dialog.innerHTML = transport.responseText;
                            if (loadFunction) {
                                loadSubTree(rootPath, 'root', textBoxId, 'false');
                            }
                        }
                    });
                }
            } else {
                new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,onOKCallback:onOKCallback,random:random},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree('/', 'root', textBoxId, 'false');
                        }
                    }
                });
            }
        } else {
            if (relativeRoot) {
                new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,rootPath:rootPath,relativeRoot:relativeRoot,displayRootPath:displayRootPath,random:random},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'false');
                        }
                    }
                });
            } else {
                if (!rootPath) {
                    rootPath = onOKCallback;
                }
                new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,rootPath:rootPath,displayRootPath:displayRootPath,random:random},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'false');
                        }
                    }
                });
            }
        }
        //new Ajax.Updater('dialog', '../resources/resource_tree_ajaxprocessor.jsp', { method: 'get', parameters: {textBoxId:textBoxId,onOKCallback:onOKCallback,random:random} });
    } else {
        new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
            method: 'get',
            parameters: {textBoxId:textBoxId,random:random},
            onSuccess: function(transport) {
                var dialog = $('dialog');
                dialog.innerHTML = transport.responseText;
                if (loadFunction) {
                    loadSubTree('/', 'root', textBoxId, 'false');
                }
            }
        });
        //new Ajax.Updater('dialog', '../resources/resource_tree_ajaxprocessor.jsp', { method: 'get', parameters: {textBoxId:textBoxId,random:random} });
    }
}

function showGovernanceResourceTree(textBoxId, onOKCallback) {
     if (!onOKCallback) {
         onOKCallback = "none";
     }
     showResourceTreeWithLoadFunction(true, textBoxId, onOKCallback, "/_system/governance", "true", "false");
 }
function showGovernanceResourceTreeWithCustomPath(textBoxId, path, onOKCallback) {
    if (!onOKCallback) {
        onOKCallback = "none";
    }
    showResourceTreeWithLoadFunction(true, textBoxId, onOKCallback, path, "true", "true");
}

 function showGovernanceCollectionTree(textBoxId, onOKCallback) {
     if (!onOKCallback) {
         onOKCallback = "none";
     }
     showCollectionTreeWithLoadFunction(true, textBoxId, onOKCallback, "/_system/governance", "true");
 }

 function showConfigResourceTree(textBoxId, onOKCallback) {
     if (!onOKCallback) {
         onOKCallback = "none";
     }
     showResourceTreeWithLoadFunction(true, textBoxId, onOKCallback, "/_system/config", "true", "false");
 }

 function showConfigCollectionTree(textBoxId, onOKCallback) {
     if (!onOKCallback) {
         onOKCallback = "none";
     }
     showCollectionTreeWithLoadFunction(true, textBoxId, onOKCallback, "/_system/config", "true");
 }

function showResourceTree(textBoxId, onOKCallback, rootPath) {
    showResourceTreeWithLoadFunction(true, textBoxId, onOKCallback, rootPath, null, "false");
}

function showCollectionTreeWithLoadFunction(loadFunction, textBoxId, onOKCallback, rootPath, relativeRoot) {

    //This value is passed to address the cashing issue in IE
    var loadingContent = '<div class="ajax-loading-message"> <img src="../resources/images/ajax-loader.gif" align="top"/> <span>' + org_wso2_carbon_registry_resource_ui_jsi18n["resource.tree.loading"] + '</span> </div>';
    CARBON.showPopupDialog(loadingContent, org_wso2_carbon_registry_resource_ui_jsi18n["resource.tree"], 500, false);
    var random = getRandom();
    if (onOKCallback) {
        if (typeof onOKCallback == "function") {
            onOKCallback = onOKCallback.toString().substring('function '.length);
            onOKCallback = onOKCallback.substring(0, onOKCallback.indexOf("("));
            if (rootPath) {
                if (relativeRoot) {
                new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                    method: 'get',
                     parameters: {textBoxId:textBoxId,rootPath:rootPath,relativeRoot:relativeRoot,onOKCallback:onOKCallback,random:random,hideResources:'true'},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'true');
                        }
                    }
                });
            } else {
                new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                    method: 'get',
                     parameters: {textBoxId:textBoxId,rootPath:rootPath,onOKCallback:onOKCallback,random:random,hideResources:'true'},
                         onSuccess: function(transport) {
                             var dialog = $('dialog');
                             dialog.innerHTML = transport.responseText;
                             if (loadFunction) {
                                 loadSubTree(rootPath, 'root', textBoxId, 'true');
                             }
                         }
                     });
                 }
             } else {
                 new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                     method: 'get',
                    parameters: {textBoxId:textBoxId,onOKCallback:onOKCallback,random:random,hideResources:'true'},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree('/', 'root', textBoxId, 'true');
                        }
                    }
                });
            }
        } else {
            if (relativeRoot) {
                new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,rootPath:rootPath,relativeRoot:relativeRoot,random:random,hideResources:'true'},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'true');
                        }
                    }
                });
            } else {
                if (!rootPath) {
                    rootPath = onOKCallback;
                }
                new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,rootPath:rootPath,random:random,hideResources:'true'},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'true');
                        }
                    }
                });
            }
        }
        //new Ajax.Updater('dialog', '../resources/resource_tree_ajaxprocessor.jsp', { method: 'get', parameters: {textBoxId:textBoxId,onOKCallback:onOKCallback,random:random,hideResources:'true'} });
    } else {
        new Ajax.Request('../resources/resource_tree_ajaxprocessor.jsp', {
            method: 'get',
            parameters: {textBoxId:textBoxId,random:random,hideResources:'true'},
            onSuccess: function(transport) {
                var dialog = $('dialog');
                dialog.innerHTML = transport.responseText;
                if (loadFunction) {
                    loadSubTree('/', 'root', textBoxId, 'true');
                }
            }
        });
        //new Ajax.Updater('dialog', '../resources/resource_tree_ajaxprocessor.jsp', { method: 'get', parameters: {textBoxId:textBoxId,random:random,hideResources:'true'} });
    }
}

function showCollectionTree(textBoxId, onOKCallback, rootPath) {
    showCollectionTreeWithLoadFunction(true, textBoxId, onOKCallback, rootPath);
}

function pickPath(path, textBoxId, reference) {
    if (!textBoxId || textBoxId == 'null') {
        setTreeNavigationPath(path, reference);
        window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + path.replace(/&/g, "%26");
        return;
    }
    document.getElementById('pickedPath').value = path;
    document.getElementById('pickedPath').focus();
    //document.getElementById(textBoxId).value = document.getElementById('pickedPath').value;
    //This is a hack to fix the ie7 issue with not getting the object using it's id
    //if (textBoxId == "associationPaths") document.forms.assoForm.associationPaths.value = document.getElementById('pickedPath').value;
}

function loadSubTree(path, parentId, textBoxId, hideResources, callback) {
    var theImg = document.getElementById("plus_" + parentId);
    sessionAwareFunction(function() {
        if (theImg) {
            var theDiv = document.getElementById('child_' + parentId);
            if (theDiv) {
                theDiv.style.display = '';
            }
            var isPlus = theImg.style.display != 'none';
            if (isPlus) {
                while (path.indexOf(" ") > 0) {
                    path = path.replace(" ", "%20")
                }
                while (path.indexOf("&") > 0) {
                    path = path.replace(/&/g, "%26")
                }
                var url = '../resources/resource_sub_tree_ajaxprocessor.jsp?path=' + path + '&parentId=' + parentId + '&textBoxId=' + textBoxId + (hideResources == 'true' ? '&hideResources=true' : '');
                jQuery("#child_" + parentId).load(url, null,
                        function(data, status, t) {
                            if (status != "success") {
                                //CARBON.showWarningDialog(org_wso2_carbon_registry_resource_ui_jsi18n["error.occured"]);
                                document.getElementById("local-registry-td").style.display = "none";
                            }
                            if (callback && typeof callback == "function") {
                                callback();
                            }
                            // Add necessary logic to handle these scenarios if needed
                            if (data || t) {}
                        });
            } else {
                if (theDiv) {
                    theDiv.innerHTML = "";
                    theDiv.style.display = 'none';
                }
            }
            showHideCommon('plus_' + parentId);
            showHideCommon('minus_' + parentId);
        }

    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function getWindowWidth() {
    var crossWidth = 0; //crossHeight = 0;
    if (typeof( window.innerWidth ) == 'number') {
        //Non-IE
        crossWidth = window.innerWidth;
        //crossHeight = window.innerHeight;
    } else if (document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight )) {
        //IE 6+ in 'standards compliant mode'
        crossWidth = document.documentElement.clientWidth;
        //crossHeight = document.documentElement.clientHeight;
    } else if (document.body && ( document.body.clientWidth || document.body.clientHeight )) {
        //IE 4 compatible
        crossWidth = document.body.clientWidth;
        //crossHeight = document.body.clientHeight;
    }
    return(crossWidth);
}

function fixResourceTable() {
    var wdWidth = getWindowWidth();
    var colWidth = (wdWidth * 65) / 100 - 560;
    if (document.getElementById('resourceSizer') == null) return;

    document.getElementById('resourceSizer').width = colWidth + "px";

    //Calculate the truncate length for each resource name
    var truncateLength = parseInt(colWidth / 12 + (wdWidth - 1016) / 9);

    //Truncate the resource names by getting there elements by class name
    /*var classnameRef = "__resourceNameRef";*/
    var classname = "__resourceName";

    var node = document.getElementById("entryList");

    var re = new RegExp('\\b' + classname + '\\b');
    var els = node.getElementsByTagName("*");
    var elsRef = node.getElementsByTagName("*");

    for (var i = 0,j = els.length; i < j; i++) {

        if (re.test(els[i].className)) els[i].innerHTML = elsRef[i - 1].innerHTML.truncate(truncateLength);

    }

}
/*
 function fixPropertyTabel(){
 var wdWidth=getWindowWidth();
 var givenWidth=(wdWidth*65)/100-660;
 var colWidth= givenWidth/2;
 if($('propertySizer')==null) return;

 $('propertySizer').width=colWidth + "px";

 //Calculate the truncate length for each resource name
 var truncateLength=parseInt(colWidth/12 + (wdWidth-1016)/9);

 //Truncate the property names by getting there elements by class name

 fixPropertyTabelRun("__propNameRef","__propName",truncateLength);
 fixPropertyTabelRun("__propValueRef","__propValue",truncateLength);


 }
 function fixPropertyTabelRun(classnameRef,classname,truncateLength){
 var node = document.getElementById("resourceProperties");

 var re = new RegExp('\\b' + classname + '\\b');
 var els = node.getElementsByTagName("*");
 var elsRef = node.getElementsByTagName("*");

 for(var i=0,j=els.length; i<j; i++){

 if(re.test(els[i].className)) els[i].innerHTML=elsRef[i-1].innerHTML.truncate(truncateLength);

 }
 }
 */
function validateWSDL() {
    showHideCommon('validationDiv');
}

function applyPanelState() {

    var promotionHideImg = document.getElementById('promotionHideImg');
    var promotionShowImg = document.getElementById('promotionShowImg');
    var proBox = document.getElementById('promotionBox');
    var leftState = get_cookie("left");

    if (leftState == "hidden" || leftState == null) {
        promotionHideImg.style.display = "";
        promotionShowImg.style.display = "none";
        proBox.style.display = "none";
        proBox.parentNode.className = "promotionDivHidden";
    } else {
        promotionHideImg.style.display = "none";
        promotionShowImg.style.display = "";
        proBox.style.display = "";
        proBox.parentNode.className = "promotionDiv";
    }
    //Right side panel states
    if (document.getElementById('rightColumn')) {
        var rightBox = document.getElementById('rightColumn');
        var leftColoum = document.getElementById('leftColoum');
        var rightHideImg = document.getElementById('rightHideImg');
        var rightShowImg = document.getElementById('rightShowImg');

        var rightState = get_cookie("right");

        if (rightState == "visible" || rightState == null) {
            rightBox.style.display = "";
            rightBox.className = "rightColumnSizer";
            leftColoum.className = "leftColoumSizer";
            rightHideImg.style.display = "";
            rightShowImg.style.display = "none";
        }
        else {
            rightBox.style.display = 'none';
            rightBox.className = "rightColumnSizerHidden";
            leftColoum.className = "leftColoumSizerFull";
            rightShowImg.style.display = "";
            rightHideImg.style.display = "none";
        }
    }
}

function closeResourceTree() {

    document.getElementById('resourceTree').style.display = "none";
    document.getElementById('popup-main').style.display = "none";
}

function navigatePages(wantedPage, resourcePath, viewMode, consumerID, targetDivID) {

    fillContentSection(resourcePath, wantedPage, viewMode, consumerID, targetDivID);
    YAHOO.util.Event.onAvailable("xx"+wantedPage,loadData);
}

function loadResourcePage(path, viewMode, consumerID, targetDiv) {
    return loadJSPPage('resource', path, viewMode, consumerID, targetDiv);
}

function loadVersionPage(path, viewMode, consumerID, targetDiv) {
    return loadJSPPage('versions', path, viewMode, consumerID, targetDiv);
}

function setResourcePathOnConsumer(consumerID, path, divNum) {
    setPathURL(divNum);
    if (consumerID == undefined || consumerID == null || "" == consumerID) {
        return true;
    }

    var consumer = document.getElementById(consumerID);
    if (consumer != undefined && consumer != null) {
        consumer.value = path;
    }

    var consumerHidden = document.getElementById(consumerID + "_hidden");
    if (consumerHidden != undefined && consumerHidden != null) {
        consumerHidden.value = path;
    }
    return true;
}
//private
function loadJSPPage(pagePrefixName, path, viewMode, consumerID, targetDiv) {

    if (viewMode != undefined && viewMode != null && 'inlined' == viewMode) {

        var suffix = "&resourceViewMode=inlined";
        if (consumerID != undefined && consumerID != null && consumerID != "") {
            suffix += "&resourcePathConsumer=" + consumerID;
        }

        if (targetDiv == null || targetDiv == undefined || targetDiv == "" || targetDiv == "null") {
            targetDiv = 'registryBrowser';
        }

        suffix += "&targetDivID=" + targetDiv;

        if (path == "#") {
            path = '/';
        }
        path = path.replace(/&/g, "%26");

        var url = '../resources/' + pagePrefixName + '_ajaxprocessor.jsp?path=' + path + suffix;

        /* document.getElementById(targetDiv).style.display = ""; */
        jQuery("#popupContent").load(url, null,
                function(res, status, t) {
                    if (status != "success") {
                        //CARBON.showWarningDialog(org_wso2_carbon_registry_resource_ui_jsi18n["error.occured"]);
                    }
                    // Add necessary logic to handle these scenarios if needed
                    if (res || t) {}
                });
        return false;
    } else {
        path = path.replace(/&/g, "%26");
        if (pagePrefixName == 'resource') {
            document.location.href = '../resources/' + pagePrefixName + '.jsp?region=region3&item=resource_browser_menu&path=' + path + "&screenWidth=" + screen.width;
        } else {
            document.location.href = '../resources/' + pagePrefixName + '.jsp?path=' + path + "&screenWidth=" + screen.width;

        }
    }
    return true;
}

function setPathURL(divNum)
{
	onceDone = 0;
	j = 2;
	fullString = "";

	screenWidth = screen.width;

	switch (screenWidth)
	{
	case 1024:
	  maxChars = 90;
	  break;
	default:
	  maxChars = screenWidth / 1024 * 90;
	}

	while (j < divNum)
	{
		divID = "pathResult" + j;
		fullString = fullString + document.getElementById(divID).innerHTML;


		if ((fullString.length > maxChars) && (onceDone<8))
		{
			document.getElementById(divID).innerHTML = "<br/>"+document.getElementById(divID).innerHTML;
			onceDone = onceDone + 1;
            fullString = "";
		}
		j = j + 1;
	}

}

function showInLinedRegistryBrowser(id) {
    showInLinedRegistryBrowserOnDiv('registryBrowser', id);
}

function showInLinedRegistryBrowserOnDiv(divID, id) {

    if (id == null || id == undefined || id == "") {
        CARBON.showInfoDialog(org_wso2_carbon_registry_resource_ui_jsi18n["registry.key.cannot.be.null.or.empty"]);
    }

    var url = '../resources/resource_tree_ajaxprocessor.jsp?resourceViewMode=inlined&resourcePathConsumer=' + id + '&targetDivID=' + divID + '&random=' + random + '&textBoxId=' + id;

      //This value is passed to address the cashing issue in IE
    var loadingContent = '<div class="ajax-loading-message"> <img src="../resources/images/ajax-loader.gif" align="top"/> <span>' + org_wso2_carbon_registry_resource_ui_jsi18n["resource.tree.loading"] + '</span> </div>';
    CARBON.showPopupDialog(loadingContent, org_wso2_carbon_registry_resource_ui_jsi18n["resource.tree"], 500, false);
    var random = getRandom();
    new Ajax.Updater('dialog', url, { method: 'get', parameters:{random:getRandom()} ,evalScripts:true});
}

function showLocalRegBrowser(id) {
    var url = '../sequences/local_registry-ajaxprocessor.jsp?resourceConsumer=' + id;
    new Ajax.Updater('local-registry-workArea', url, { method: 'get', parameters:{random:getRandom()}});
}
function hideInLinedRegistryBrowser(divID) {

    if (divID == null || divID == undefined || divID == "" || divID == "null") {
        divID = 'registryBrowser';
    }
    var nsDiv = document.getElementById(divID);
    if (nsDiv != null && nsDiv != undefined) {
        nsDiv.style.display = "none";
        nsDiv.innerHTML = "";
    }

    return false;
}

function restoreVersion(url, viewMode, targetDiv) {

    if (viewMode != undefined && viewMode != null && 'inlined' == viewMode) {
        if (targetDiv == null || targetDiv == undefined || targetDiv == "" || targetDiv == "null") {
            targetDiv = 'registryBrowser';
        }
        /*
        document.getElementById(targetDiv).style.display = "";
        */
        jQuery("#popupContent").load(url, null,
                function(res, status, t) {
                    if (status != "success") {
                        CARBON.showWarningDialog(org_wso2_carbon_registry_resource_ui_jsi18n["error.occured"]);
                    }
                    // Add necessary logic to handle these scenarios if needed
                    if (res || t) {}
                });
        return false;
    } else {
        document.location.href = url;
    }
    return true;
}

function onchangelocalregistrykeys(id) {
    setResourcePathOnConsumer(id, getRegistryKey());
}
function getRegistryKey() {
    var localregkey = document.getElementById("local-registry-keys-selection");
    var localregkey_value = null;
    if (localregkey != null && localregkey != undefined)
    {
        var localregkey_index = localregkey.selectedIndex;
        if (localregkey_index != null && localregkey_index != undefined) {
            localregkey_value = localregkey.options[localregkey_index].value;
        }
    }
    if (localregkey_value != null && localregkey_value != undefined && localregkey_value != "" && localregkey_value != org_wso2_carbon_registry_resource_ui_jsi18n["select.a.value"]) {
        isMediationLocalEntrySelected = true;
        return localregkey_value.substring(localregkey_value.indexOf("]-") + 2, localregkey_value.length);
    } else {
        isMediationLocalEntrySelected = false;
    }
    return null;
}

function setResolvedResourcePathOnConsumer(consumerID, synapseRoot) {

    if (consumerID == undefined || consumerID == null || "" == consumerID) {
        return false;
    }

    var path = document.getElementById("pickedPath").value;

    if (path == null || path == undefined || path == "" || path == "null") {
        return false;
    }
    var suffix = "&resourceViewMode=inlined";
    if (synapseRoot != undefined && synapseRoot != null && synapseRoot != "") {
        suffix += "&synapseroot=" + synapseRoot;
    } else {
        return false;
    }

    if (path == "#") {
        path = '/';
    }

    var url = '../resources/save-path_ajaxprocessor.jsp?path=' + path + suffix;

    /* document.getElementById(targetDiv).style.display = ""; */
    jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showWarningDialog(org_wso2_carbon_registry_resource_ui_jsi18n["error.occured"]);
                } else {
                    setResourcePathOnConsumer(consumerID, trim(data));
                }
            });
    return false;
}
function ltrim(str) {
    for (var k = 0; k < str.length && str.charAt(k) <= " "; k++) {}
    return str.substring(k, str.length);
}
function rtrim(str) {
    for (var j = str.length - 1; j >= 0 && str.charAt(j) <= " "; j--) {}
    return str.substring(0, j + 1);
}

//This function accepts a String and trims the string in both sides of the string ignoring space characters
function trim(stringValue) {
    //   var trimedString = stringValue.replace( /^\s+/g, "" );
    //   return trimedString.replace( /\s+$/g, "" );
    return ltrim(rtrim(stringValue));
}
/*function trim(str){
       var ret = str.replace(new RegExp("[\\s]+$", "g"), "");
       if (!ret){
               return "";
      }
       return ret.replace(new RegExp("^[\\s]+", "g"), "");
}*/
function showHideCommon(divId){
	var theDiv = document.getElementById(divId);
	if(theDiv.style.display=="none"){
		theDiv.style.display="";
	}else{
		theDiv.style.display="none";
	}
}
function blockManual(e){
    if (e) {
        //handle-event logic
    }
	var path=document.getElementById('uResourceFile');
	 return (path.value.length > 2);
}
function handleRichText(){
	var radioObj = document.textContentForm.richText;
	var selected = "";
	for(var i=0;i<radioObj.length;i++){
		if(radioObj[i].checked)selected = radioObj[i].value;
	}

	var textAreaPanel = $('textAreaPanel');
	var trPlainContent = $('trPlainContent');
	var content = "";
	if (textContentEditor) {
	        textContentEditor.saveHTML();
	        content = textContentEditor.get('textarea').value;
	}
	if(selected=="plain"){
		trPlainContent.style.display = "";
		textAreaPanel.style.display = "none";
		//var stripHTML = /<\S[^><]*>/g;
    		//textContentEditor.get('textarea').value = textContentEditor.get('textarea').value.replace(/<br>/gi, '\n').replace(stripHTML, '');
    		trPlainContent.value=textContentEditor.get('textarea').value;
	}
	if(selected=="rich"){
		trPlainContent.style.display = "none";
		textAreaPanel.style.display = "";
		textContentEditor.setEditorHTML(trPlainContent.value);
		textContentEditor.saveHTML();
		textContentEditor.render();
	}
}

function handleUpdateRichText(){
    var radioObj = new Array();
    radioObj[0] = $('editTextContentIDRichText0');
    radioObj[1] = $('editTextContentIDRichText1');
	var selected = "";
	for(var i=0;i<radioObj.length;i++){
		if(radioObj[i].checked)selected = radioObj[i].value;
	}

	var textAreaPanel = $('editTextContentTextAreaPanel');
	var trPlainContent = $('editTextContentIDPlain');
	var content = "";
	if (textContentUpdator) {
	        textContentUpdator.saveHTML();
	        content = textContentUpdator.get('textarea').value;
	}
	if(selected=="plain"){
		trPlainContent.style.display = "";
		textAreaPanel.style.display = "none";
		//var stripHTML = /<\S[^><]*>/g;
    		//textContentUpdator.get('textarea').value = textContentUpdator.get('textarea').value.replace(/<br>/gi, '\n').replace(stripHTML, '');
    		trPlainContent.value=textContentUpdator.get('textarea').value;
	}
	if(selected=="rich"){
		trPlainContent.style.display = "none";
		textAreaPanel.style.display = "";
		textContentUpdator.setEditorHTML(trPlainContent.value);
		textContentUpdator.saveHTML();
		textContentUpdator.render();
	}
}

function loadActionPane(rowNum,type){

        var clickedTD = "actionPaneHelper"+rowNum;
        var actionLink = type+"Link"+rowNum;
        var toShow=type+"Pane"+rowNum;
        var todo = "toShow";
        if($(toShow).style.display != "none"){
        	todo ="toHide";
        }
	var allElms = document.getElementById("entryList").getElementsByTagName("*");

	for (var i = 0; i < allElms.length; i++) {
		if(YAHOO.util.Dom.hasClass(allElms[i], "actionPaneSelector")){
			allElms[i].style.display="none";
		}
		if(YAHOO.util.Dom.hasClass(allElms[i], "action-pane-helper")){
			YAHOO.util.Dom.removeClass(allElms[i],"actionSelected");
		}
		if(YAHOO.util.Dom.hasClass(allElms[i], "entryName-expanded")){
			YAHOO.util.Dom.removeClass(allElms[i],"entryName-expanded");
		}
		if(YAHOO.util.Dom.hasClass(allElms[i], "copy-move-panel")){
			allElms[i].style.display="none";
		}
	}
	if(todo == "toShow"){
		YAHOO.util.Dom.addClass(clickedTD,"actionSelected");
		$(toShow).style.display = "";

		YAHOO.util.Dom.addClass(actionLink,"entryName-expanded");
	}
	else{
		//YAHOO.util.Dom.addClass(clickedTD,"actionSelected");
		$(toShow).style.display = "none";

		YAHOO.util.Dom.addClass(actionLink,"entryName-contracted");
	}
}
function validateResourcePathAndLength(fld) {

    var error = "";
    var regx = RegExp("(//)");
    if (fld.value.match(regx)) {
        error = org_wso2_carbon_registry_resource_ui_jsi18n["the.given.name"] + " '<strong>" + fld.value + "</strong>' " + org_wso2_carbon_registry_resource_ui_jsi18n["not.a.valid.path"] + "<br />";
    } else if (fld.value.length > 256) {
        error = org_wso2_carbon_registry_resource_ui_jsi18n["the.given.name"] + " '<strong>" + fld.value + "</strong>' " + org_wso2_carbon_registry_resource_ui_jsi18n["cannot.contain.more.than1"] + "<br />";
    }
    return error;
}

function validateDescriptionLength(fld) {

    var error = "";
    if (fld.value.length > 1000) {
        error = org_wso2_carbon_registry_resource_ui_jsi18n["the.given.description"] + " '<strong>" + fld.value + "</strong>' " + org_wso2_carbon_registry_resource_ui_jsi18n["cannot.contain.more.than2"] + "<br />";
    }
    return error;
}



function validateExists(pickedPath) {
    var error = "";
    var differentiate = "differentiate";
    new Ajax.Request('../resources/resource_exists_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {pickedPath: pickedPath, differentiate: differentiate,random:getRandom()},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----ResourceExists----/) != -1){
                    error = org_wso2_carbon_registry_resource_ui_jsi18n["resource.exits"]+ " <strong>" + pickedPath + "</strong>.";
                } else if (returnValue.search(/----CollectionExists----/) != -1){
                    error = org_wso2_carbon_registry_resource_ui_jsi18n["collection.exits"]+ " <strong>" + pickedPath + "</strong>.";
                }
            },
            onFailure: function() {

            }
        });
    return error;
}

function loadFromPath() {
    var pickedPath = $('uLocationBar');
    if(!validateTextForIllegal(pickedPath,"resource path")){
        CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+ "resource path"+" " + org_wso2_carbon_registry_common_ui_jsi18n["not.valid.path"]);
        return;
    }
//    validateIllegal(pickedPath,"resource path");
    if (validateProvidedResoucePath(pickedPath, org_wso2_carbon_registry_resource_ui_jsi18n["location.bar.empty"])) {
        window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + pickedPath.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '').replace(/&/g, "%26");
    }
}

function validateProvidedResoucePath(pickedPath, errorMessage) {
    var reason = "";
	var result = false;

	pickedPath.value = pickedPath.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
	if (pickedPath.value.length == 0) {
	        reason = errorMessage + "<br />";
	}
	if(reason ==""){
        new Ajax.Request('../resources/resource_exists_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {pickedPath: pickedPath.value,random:getRandom()},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----ResourceExists----/) == -1){
                    CARBON.showWarningDialog(org_wso2_carbon_registry_resource_ui_jsi18n["resource.does.not.exit"] + " <strong>" + pickedPath.value + "</strong>.");
                    result = false;
                } else {
                    result = true;
                }
            },
            onFailure: function() {

            }
        });
	} else{
		CARBON.showWarningDialog(reason);
		result = false;
	}
	return result;
}

function validateResoucePath(){
	var pickedPath=$('pickedPath');
    return validateProvidedResoucePath(pickedPath, org_wso2_carbon_registry_resource_ui_jsi18n["picked.path.empty"]);
}


YAHOO.util.Event.addListener(window, 'resize', truncateResourceNames);
YAHOO.util.Event.addListener(window, 'load', loadData);
var oldNodes = new Array();
var firstRun = true;
function truncateResourceNames(){
    if(document.getElementById('pointA') == null || $("entryList") == null){
		return;
	}
    var allNodes = YAHOO.util.Dom.getElementsByClassName("trimer");

	var wpWidth = YAHOO.util.Dom.getViewportWidth();
	var textAreaSize;
	if($('pointA') != null && $('pointA').style.display != "none"){
		textAreaSize = wpWidth - 770;
	}else{
		textAreaSize = wpWidth - 1030;
	}
	var textSize = (textAreaSize-(textAreaSize%6))/6;
    if(textSize <14){
        textSize = 14;
    }
    if(firstRun){
        oldNodes = new Array();
    }
	for (var i = 0; i < allNodes.length; i++) {
        var toTrim="";
        if(firstRun){
            oldNodes.push(allNodes[i].innerHTML);
            toTrim="" + allNodes[i].innerHTML;
        }else{
            toTrim="" + oldNodes[i];
        }

		if(toTrim.length>15) {
			allNodes[i].innerHTML = toTrim.truncate(textSize,'..');//toTrim.length+ " -- " +textSize + "/n";
		}

	}
    firstRun = false;
}
function loadData(){
    firstRun = true;
    truncateResourceNames();
}

function displaySuccessMessage(pickedPath, operation) {
    var message = "";
    var differentiate = "differentiate";
    new Ajax.Request('../resources/resource_exists_ajaxprocessor.jsp',
    {
        method:'post',
        parameters: {pickedPath: pickedPath, differentiate: differentiate,random:getRandom()},
        asynchronous:false,
        onSuccess: function(transport) {
            var returnValue = transport.responseText;
            if (returnValue.search(/----ResourceExists----/) != -1){
                message = org_wso2_carbon_registry_resource_ui_jsi18n["successfully"] + " " + operation + " " + org_wso2_carbon_registry_resource_ui_jsi18n["resource"] + ".";
            } else if (returnValue.search(/----CollectionExists----/) != -1){
                message = org_wso2_carbon_registry_resource_ui_jsi18n["successfully"] + " " + operation + " " + org_wso2_carbon_registry_resource_ui_jsi18n["collection"] + ".";
            }
        },
        onFailure: function() {

        }
    });
    CARBON.showInfoDialog(message);
}

function handleWindowOk(textBoxId, onOK){
     handleRelativeWindowOk("", textBoxId, onOK);
 }

function handleRelativeWindowOk(path, textBoxId, onOK) {
    if (!textBoxId) {
        return;
    }
    var theTextBox = document.getElementById(textBoxId);
    var pickedValue = document.getElementById('pickedPath').value;
    if (path != "") {
        pickedValue = pickedValue.replace(path, "");
    }
	if (textBoxId == "associationPaths") {
        // This is a hack to fix the ie7 issue with not getting the object using it's id
         document.forms['assoForm'].associationPaths.value = pickedValue;
    } else {
         theTextBox.value = pickedValue;
    }
    if (onOK && typeof onOK == "function") {
        onOK();
    }
}
function getRandom(){
    return Math.floor(Math.random() * 2000);
}

function toggleSaveMediaType() {
    if (jQuery('#toggleSaveMediaType_view').is(":visible")) {
        jQuery('#toggleSaveMediaType_view').hide();
        jQuery('#toggleSaveMediaType_edit').show();
        jQuery('#toggleSaveMediaType_editBtn').hide();
        jQuery('#toggleSaveMediaType_saveBtn').show();
        jQuery('#toggleSaveMediaType_cancelBtn').show();
    } else {
        jQuery('#toggleSaveMediaType_view').show();
        jQuery('#toggleSaveMediaType_edit').hide();
        jQuery('#toggleSaveMediaType_editBtn').show();
        jQuery('#toggleSaveMediaType_saveBtn').hide();
        jQuery('#toggleSaveMediaType_cancelBtn').hide();
    }
}

function updateMediaType(resourcePath, mediaType) {
    toggleSaveMediaType();
    sessionAwareFunction(function () {
        new Ajax.Request('../resources/update_mediatype_ajaxprocessor.jsp',
            {
                method:'post',
                parameters:{resourcePath:resourcePath, mediaType:mediaType},
                onSuccess:function (transport) {
                    $('toggleSaveMediaType_view').innerHTML = mediaType;
			var returnValue = transport.responseText;
            		if (returnValue.search(/----XmlToArtifactChange----/) != -1){
				window.location.replace("../resources/resource.jsp?region=region3&item=resource_browser_menu&path=/");
			}
                },
                onFailure:function () {
                    CARBON.showWarningDialog(transport.responseText);
                    return false;
                }
            });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

function downloadWithDependencies(path, hasAssociations) {
    sessionAwareFunction(function() {
            if (trim(hasAssociations.toString()) == "true") {
            CARBON.showConfirmationDialog(org_wso2_carbon_registry_resource_ui_jsi18n["download.with.all.dependencies"], function() {
                location.href =  path + '&withDependencies=true';

            }, function() {
                location.href = path ;
            });
        } else {
            location.href = path ;
        }
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
}

