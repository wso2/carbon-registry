var handlerOperationStarted = false;
    function editHandler(handlerName) {
        if (handlerOperationStarted) {
            CARBON.showWarningDialog(org_wso2_carbon_registry_handler_ui_jsi18n["handler.operation.in.progress"]);
            return;
        }
        handlerOperationStarted = true;
        sessionAwareFunction(function() {
            document.location.href = "../handler/source_handler.jsp?handlerName=" + handlerName;
        }, org_wso2_carbon_registry_handler_ui_jsi18n["session.timed.out"]);
        handlerOperationStarted = false;
    }

    function deleteHandler(handlerName) {
        if (handlerOperationStarted) {
            CARBON.showWarningDialog(org_wso2_carbon_registry_handler_ui_jsi18n["handler.operation.in.progress"]);
            return;
        }
        handlerOperationStarted = true;
        sessionAwareFunction(function() {
            CARBON.showConfirmationDialog(org_wso2_carbon_registry_handler_ui_jsi18n["are.you.sure.you.want.to.delete"] + " " + handlerName + "?", function() {
                new Ajax.Request('../handler/delete_handler-ajaxprocessor.jsp', {
                    method: 'post',
                    parameters: {handlerName: handlerName},
                    onSuccess: function(transport) {
                        if (!transport) {
                            handlerOperationStarted = false;
                            return;
                        }
                        var message = org_wso2_carbon_registry_handler_ui_jsi18n["configuration.deleted"];
                        handlerOperationStarted = false;
                        window.location = "handler.jsp?region=region3&item=registry_handler_menu";
                        CARBON.showInfoDialog(message);
                    },
                    onFailure: function(transport) {
                        CARBON.showErrorDialog(org_wso2_carbon_registry_handler_ui_jsi18n["failed.to.delete"] + transport.responseText);
                    }
                });
            });
        }, org_wso2_carbon_registry_handler_ui_jsi18n["session.timed.out"]);
        handlerOperationStarted = false;
    }

    function simulateHandler() {
        if(!validateRequiredFiledForOperation($("operation").value)) {
            $('simulationChart').innerHTML = "";
            return;
        }

        if (handlerOperationStarted) {
            CARBON.showWarningDialog(org_wso2_carbon_registry_handler_ui_jsi18n["handler.operation.in.progress"]);
            return;
        }
        handlerOperationStarted = true;
        sessionAwareFunction(function() {
            var operation = $("operation").value;
            var mediaType = $("mediaType").value;
            if (mediaType && mediaType != null && mediaType.length > 0) {
                mediaType = mediaType.replace("+", "%2B");
            }
            var path = $("path").value;
            var resourcePath = $("resourcePath").value;
            var param1 = (operation == "put") ? $("type").value : $("param1").value;
            var param2 = $("param2").value;
            var param3 = $("param3").value;
            var dataStr = "operation=" + operation + "&mediaType=" + mediaType + "&path=" + path + "&resourcePath=" + resourcePath + "&param1=" + param1 + "&param2=" + param2 + "&param3=" + param3;
 
            jQuery.ajax({
                type:"POST",
                url:'../handler/simulate_handler-ajaxprocessor.jsp',
                data: dataStr,
                dataType: "html",
                success:
                        function(data, status)
                        {
                             if (!data) {
                                handlerOperationStarted = false;
                                return;
                            }
                            jQuery('#simulation').html(data);
                            handlerOperationStarted = false;

                        },
                error:
                        function(data,status){
                            if (data) {
                                CARBON.showErrorDialog(org_wso2_carbon_registry_handler_ui_jsi18n["failed.to.simulate"] + " " + data.statusText);
                            } else {
                                CARBON.showErrorDialog(org_wso2_carbon_registry_handler_ui_jsi18n["failed.to.simulate"]);
                            }
                            $('simulationChart').innerHTML = "";
                        }
            });
           /* new Ajax.Request('../handler/simulate_handler-ajaxprocessor.jsp', {
                method: 'post',
                parameters: {operation: operation, mediaType: mediaType, path: path,
                    resourcePath: resourcePath, param1: param1, param2: param2, param3: param3},
                onSuccess: function(transport) {
                    if (!transport) {
                        handlerOperationStarted = false;
                        return;
                    }
                    $("simulation").innerHTML = transport.responseText;
                    handlerOperationStarted = false;
                },
                onFailure: function(transport) {
                    if (transport.responseText) {
                        CARBON.showErrorDialog(org_wso2_carbon_registry_handler_ui_jsi18n["failed.to.simulate"] + transport.responseText);
                    } else {
                        CARBON.showErrorDialog(org_wso2_carbon_registry_handler_ui_jsi18n["failed.to.simulate"]);   
                    }
                }
            });*/
        });
        handlerOperationStarted = false;
    }

function validateRequiredFiledForOperation(operation) {
    var requiredFields = new Array();
    for (var i = 0; i < operationArray.length; i++) {
        if (operationArray[i].opName == operation) {
            requiredFields = operationArray[i].compulsory.split(";"); //split by semicolon
            break;
        }
    }
    var reqFileds = new Array();
    var fieldName = "";
    for (var i = 0; i < requiredFields.length; i ++) {
        reqFileds = requiredFields[i].split("_");
        if (reqFileds.length > 1) {
            fieldName = reqFileds[1];
        } else {
            //fieldName = reqFileds[0];//TODO: fix this to anything other than 'Path'
            fieldName = 'Path';
        }
        var msg = validateEmpty(document.getElementById(reqFileds[0]), "'" + fieldName + "'");  //adding single quotes.
        if( msg != "") {
            CARBON.showWarningDialog(msg);
            return false;
        }
    }
    return true;
}
    function saveHandler(handlerName, isNew) {
        if (handlerOperationStarted) {
            CARBON.showWarningDialog(org_wso2_carbon_registry_handler_ui_jsi18n["handler.operation.in.progress"]);
            return;
        }
        handlerOperationStarted = true;
        sessionAwareFunction(function() {
            var param1 = "";
            if (isNew) {
                param1 = editAreaLoader.getValue("payload");//$("payload").value;
            } else {
                param1 = handlerName;
            }

            var payloadVar = editAreaLoader.getValue("payload");
            if (payloadVar!="") {
                new Ajax.Request('../handler/save_handler-ajaxprocessor.jsp', {
                    method: 'post',
                    parameters: {handlerName: handlerName, isNew: isNew, payload: payloadVar},
                    onSuccess: function(transport) {
                        if (!transport) {
                            handlerOperationStarted = false;
                            return;
                        }
                        var message = org_wso2_carbon_registry_handler_ui_jsi18n["configuration.saved"];
                        handlerOperationStarted = false;
                        CARBON.showInfoDialog(message, function() {window.location = "handler.jsp?region=region3&item=registry_handler_menu";});
                    },
                    onFailure: function(transport) {
                        CARBON.showErrorDialog(org_wso2_carbon_registry_handler_ui_jsi18n["failed.to.save"] + transport.responseText);
                    }
                });
            } else{
                var message = org_wso2_carbon_registry_handler_ui_jsi18n["configuration.empty"];
                CARBON.showWarningDialog(message);
            }
        }, org_wso2_carbon_registry_handler_ui_jsi18n["session.timed.out"]);
        handlerOperationStarted = false;
    }

function genHandlerUI(handlers){
    /*tmp data feed*/
    /*
    handlers.push({handler:'testHndler1',status:'Successful'});
    handlers.push({handler:'testHndler2',status:'Successful'});
    handlers.push({handler:'testHndler3',status:'Error man'});
    handlers.push({handler:'testHndler4',status:'Successful'});
    handlers.push({handler:'testHndler5',status:'Error man'});
    handlers.push({handler:'testHndler6',status:'Error man'});
    handlers.push({handler:'testHndler7',status:'Error man'});
    handlers.push({handler:'testHndler8',status:'Error man'});
    handlers.push({handler:'testHndler9',status:'Error man'});
    */
    /*end tmp data feed */

    var wpWidth = YAHOO.util.Dom.getViewportWidth() - 260;
    var numDivs = (Math.round((wpWidth-170)/210)) + 1;
    var htmlData = "";
    var pageWidth = 170 + (numDivs-1)*210;
    htmlData = '<div align="center"><div style="width:' + pageWidth + 'px">';

    var direction = "toLeft";
    var otherDirection = "toRight";
    var depth = 1;
    if(handlers.length>numDivs){
        depth = (handlers.length - handlers.length % numDivs) / numDivs+1;
    }

    for (var currentDepth = 1; currentDepth <= depth; currentDepth++) {
        if(currentDepth%2 == 1){
            direction = "toLeft";
            otherDirection = "toRight";
        }else{
            direction = "toRight";
            otherDirection = "toLeft";
        }
        htmlData += '<ul class="simulationChart '+direction+'">';
        if(currentDepth == 1){
        	htmlData += '<li class="handlerStartSymbol"></li>';
        }
        if((currentDepth > 1 && direction == "toLeft") && !((currentDepth == depth) && (handlers.length == ((depth - 1) * numDivs)))){
        	htmlData += '<li class="handlerSpaceSymbol"></li>';
        }
        
        for (var i=0;i<handlers.length;i++) {
            var indexDepth = 1;
            if(handlers.length>numDivs){
                indexDepth = (i - i % numDivs) / numDivs+1;
            }

            if(currentDepth==indexDepth){
                var status = 'fail';
                var statusClass = 'handlerRed';
                if(handlers[i].status == "Successful"){
                    statusClass =   "handlerGreen";
                }
                htmlData+='<li class="'+statusClass+'" onmouseover="showHandlerDetails(\'handlerDetails'+currentDepth+'-'+i+'\')" onmouseout="hideHandlerDetails(\'handlerDetails'+currentDepth+'-'+i+'\')">'+handlers[i].handler.truncate(17, '..')+' <div class="handlerDetails" id="handlerDetails'+currentDepth+'-'+i+'" style="display:none"> <div class="handlerDetailsTitle">'+handlers[i].handler+'</div>'+handlers[i].status+'</div></li>';
                if(handlers.length != (i+1) && (i % numDivs)!=(numDivs-1) ){
                    htmlData+='<li class="handlerArrow'+otherDirection+'"></li>';
                }
            }
        }
        //Adding the end symbol if the depth is greater than 1
        if(depth > 1 && (currentDepth == depth && (((depth - 1) * numDivs) != handlers.length))){
        	htmlData += '<li class="handlerArrow'+otherDirection+'"></li><li class="handlerEndSymbol"></li>';
        }else if(depth == 1 && (((depth) * numDivs) != handlers.length)){
        	htmlData += '<li class="handlerArrow'+otherDirection+'"></li><li class="handlerEndSymbol"></li>';
        }
 
        htmlData += '</ul>';
        htmlData += '<div style="clear:both"></div>';
        if(currentDepth != depth){
        //Handling the normal scenario where the flow is continuing and the arrow is to the down side
	       	if(otherDirection == "toLeft"){
	            htmlData += '<div class="handlerArrowDown '+otherDirection+'" style=\"margin-left: 40px;\"></div>';
	            htmlData += '<div style="clear:both"></div>';
	       	}else{
	            htmlData += '<div class="handlerArrowDown '+otherDirection+'"></div>';
	            htmlData += '<div style="clear:both"></div>';
	        }
        } else if(depth > 1){
        //Handling the scenarion where depth is greater than 1 and the end tag is with a down arrow
	        if(((depth - 1) * numDivs) == handlers.length){
	            htmlData += '<div class="handlerEndSymbolSpecial '+direction+'" style=\"margin-left: 40px;\"></div>';
	            htmlData += '<div style="clear:both"></div>';
	        }
        } else {
        //Handling the depth == 1 scenario
        	if(((depth) * numDivs) == handlers.length){
        	    htmlData += '<div class="handlerArrowDown '+otherDirection+'"></div>';
        	    htmlData += '<div style="clear:both"></div>';
	            htmlData += '<div class="handlerEndSymbolSpecial '+otherDirection+'" style=\"margin-left: 40px;\"></div>';
	            htmlData += '<div style="clear:both"></div>';
	        }
        }
    }
    htmlData += "</div></div>";


    $('simulationChart').innerHTML = htmlData;
}
function showHandlerDetails(id){
    var detailsDiv = document.getElementById(id);
    detailsDiv.style.display = "";
}
function hideHandlerDetails(id){
    var detailsDiv = document.getElementById(id);
    detailsDiv.style.display = "none";
}
/*
* Filling the  operation select html element
* */
var operationArray = new Array();
function fillOperation(){
    var operation = $('operation');
    /*
     * Convention to add elements
     * opName = item to add to options list
     * Items seperated by semicolens. Items which are parm1,parm2 and parm3 must be given a second value followed by a underscore character
     */

    /* Operation criteria:
     *     get - path
     *     put - path, resourcePath (existing resource), optional : mediaType
     *     resourceExists - path
     *     delete - path
     *     importResource - path, param1 (URL: source URL), optional : mediaType
     *     copy - path, param1 (target path)
     *     move - path, param1 (target path)
     *     rename - path, param1 (target path)
     *     removeLink - path
     *     createLink - path, param1 (target path), optional : param2 (target sub-path)
     *     invokeAspect - path, param1 (aspect name), param2 (action)
     *     addAssociation - path, param1 (target path), param2 (association type)
     *     removeAssociation - path, param1 (target path), param2 (association type)
     *     getAssociations - path, param1 (association type)
     *     getAllAssociations - path
     *     createVersion - path
     *     restoreVersion - path
     *     getVersions - path
     *     applyTag - path, param1 (tag)
     *     removeTag - path, param1 (tag)
     *     getTags - path
     *     getResourcePathsWithTag - param1 (tag)
     *     rateResource - path, param1 (Number: rating)
     *     getRating - path, param1 (username)
     *     getAverageRating - path
     *     addComment - path, param1 (comment)
     *     removeComment - path
     *     editComment - path, param1 (comment)
     *     getComments - path
     *     searchContent - param1 (keywords)
     *     executeQuery - param1 (Map: parameters, ex:- key1:val1,key2:val2,...), optional: path
     *
     * Operations not-supported
     *     dump
     *     restore
     */

    operationArray.push({opName:"addAssociation",compulsory:"path;param1_Target Path;param2_Association Type",optional:"",browse:"path;param1"});
    operationArray.push({opName:"addComment",compulsory:"path;param1_Comment",optional:"",browse:"path"});
    operationArray.push({opName:"applyTag",compulsory:"path;param1_Tag",optional:"",browse:"path"});
    operationArray.push({opName:"copy",compulsory:"path;param1_Target Path",optional:"",browse:"path;param1"});
    operationArray.push({opName:"createLink",compulsory:"path;param1_Target Path",optional:"param2_Target Sub-path",browse:"path;param1"});
    operationArray.push({opName:"createVersion",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"delete",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"editComment",compulsory:"path;param1_Comment",optional:"",browse:"path"});
    operationArray.push({opName:"executeQuery",compulsory:"param1_Parameters, ex:-<br />key1:val1,key2:val2,...<br />",optional:"path",browse:"path"});
    operationArray.push({opName:"get",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"getAllAssociations",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"getAssociations",compulsory:"path;param1_Association Type",optional:"",browse:"path"});
    operationArray.push({opName:"getAverageRating",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"getComments",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"getRating",compulsory:"path;param1_Username",optional:"",browse:"path"});
    operationArray.push({opName:"getResourcePathsWithTag",compulsory:"param1_Tag",optional:"",browse:"path"});
    operationArray.push({opName:"getTags",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"getVersions",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"importResource",compulsory:"path;param1_Source URL",optional:"mediaType",browse:"path"});
    operationArray.push({opName:"invokeAspect",compulsory:"path;param1_Aspect Name;param2_Action",optional:"",browse:"path"});
    operationArray.push({opName:"move",compulsory:"path;param1_Target Path",optional:"",browse:"path;param1"});
    operationArray.push({opName:"put",compulsory:"path",optional:"mediaType;resourcePath;type",browse:"path;resourcePath"});
    operationArray.push({opName:"rateResource",compulsory:"path;param1_Rating",optional:"",browse:"path"});
    operationArray.push({opName:"removeAssociation",compulsory:"path;param1_Target Path;param2_Association Type",optional:"",browse:"path;param1"});
    operationArray.push({opName:"removeComment",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"removeLink",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"removeTag",compulsory:"path;param1_Tag",optional:"",browse:"path"});
    operationArray.push({opName:"rename",compulsory:"path;param1_Target Path",optional:"",browse:"path;param1"});
    operationArray.push({opName:"resourceExists",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"restoreVersion",compulsory:"path",optional:"",browse:"path"});
    operationArray.push({opName:"searchContent",compulsory:"param1_Keywords",optional:"",browse:""});

    for(var i=0;i<operationArray.length;i++){
        var op = document.createElement("OPTION");
        op.setAttribute("value",operationArray[i].opName);
        op.innerHTML = (operationArray[i].opName);
        operation.appendChild(op);
    }
    //load the initial form
    changeOperation("addAssociation");
}

function changeOperation(selected){
    var compulsory = new Array();
    var optional = new Array();
    var browse = new Array();
    for(var i=0;i<operationArray.length;i++){
        if(operationArray[i].opName == selected){
            compulsory = operationArray[i].compulsory.split(";");//split by semicolon
            optional = operationArray[i].optional.split(";");
            browse = operationArray[i].browse.split(";");
        }
    }

    //Hide all rows exept  Operation Name row
    var allRows = $('simulatorTable').getElementsByTagName("tr");
    for(i=0;i<allRows.length;i++){
        if(allRows[i].id != "operationRow"){
            allRows[i].style.display = "none";
        }
    }

    //Hide all required *s
    var allReqs = YAHOO.util.Dom.getElementsByClassName("required","span");
    for(i=0;i<allReqs.length;i++){
        allReqs[i].style.display = "none";    
    }

    //Hide all registry browser links
    var allBrowse = YAHOO.util.Dom.getElementsByClassName("browse_button","input");
    for(i=0;i<allBrowse.length;i++){
        allBrowse[i].style.display = "none";
    }

    createData(compulsory,"comp");
    createData(optional,"");
    createData(browse,"browse");
}
function createData(parms,type){
    //adding compulsory rows
    for(i=0;i<parms.length;i++){
        var paramItems = parms[i].split("_");
        if (!paramItems[0]) {
            break;
        }
        document.getElementById(paramItems[0]).parentNode.parentNode.style.display = "";
        if (type != "browse") {
            if(paramItems[0] == "param1" || paramItems[0] == "param2" || paramItems[0] == "param3" ){
                document.getElementById(paramItems[0]+"_txt").innerHTML = paramItems[1];
            }
            if (type == "comp"){
                document.getElementById(paramItems[0]+"_required").style.display = "";
            }
        } else {
            document.getElementById(paramItems[0]+"_browser").style.display = "";
        }
    }

}

function fillResourceMediaType() {

    if (!document.getElementById('mediaType') ||
    document.getElementById('mediaType').style.display == "none") {
        return;
    }
    //fillMediaTypes();

    //setTimeout(function(){
        var path = document.getElementById('path').value;
        var ext = "";
        if (path.indexOf("\\") != -1) {
            ext = path.substring(path.lastIndexOf('\\') + 1, path.length);
        } else {
            ext = path.substring(path.lastIndexOf('/') + 1, path.length);

        }
        ext = ext.replace("?", ".");
        ext = ext.substring(ext.lastIndexOf(".") + 1, ext.length);
        var mediaType = "";
        if (ext.length > 0) {
            mediaType = getMediaType(ext);
            if (mediaType == undefined) {
                mediaType = "";
            }
        }
        document.getElementById('mediaType').value = mediaType;
    //}, 2000);
}
