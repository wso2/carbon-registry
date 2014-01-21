function loadSubscriptionDiv(resourcePath, page) {
    var fillingDiv = "subscriptionDiv";
    if ($('updateFix')) {
        $('updateFix').parentNode.removeChild($('updateFix'));
    }
    var tempSpan = document.createElement('span');
    tempSpan.id = "updateFix";
    sessionAwareFunction(function () {
        new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
            method: 'post',
            parameters: {path: resourcePath, page: page},
            onSuccess: function (transport) {
                $(fillingDiv).innerHTML = transport.responseText;
                $(fillingDiv).style.display = "";
                $('subscriptionIconExpanded').style.display = "";
                $('subscriptionIconMinimized').style.display = "none";

                YAHOO.util.Event.onAvailable('updateFix', function () {
                    $('subscriptionsList').style.display = "";

                });
                alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
            },
            onFailure: function (transport) {
                showRegistryError(transport.responseText);
            }
        });
    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"]);
}

function addComment(path) {
    var reason="";
    var commentElement = $('comment');
    var comment = commentElement.value;
    reason+=validateForInput(commentElement,org_wso2_carbon_registry_info_ui_jsi18n["comment.body"]);
    reason+=validateEmpty(commentElement,org_wso2_carbon_registry_info_ui_jsi18n["comment.body"]);

    if (comment.length > 500) {
        reason += org_wso2_carbon_registry_info_ui_jsi18n["comment.too.long"];
    }
    sessionAwareFunction(function() {
        if(reason==""){
            new Ajax.Request('../info/comment-ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {comment: comment, path: path},

                onSuccess: function(transport) {

                    $('commentsList').innerHTML = transport.responseText;
                    $('commentsList').style.display = "";

                    $('commentsIconExpanded').style.display = "";
                    $('commentsIconMinimized').style.display = "none";
                    alternateTableRows('commentsTable', 'tableEvenRow', 'tableOddRow');
                },

                onFailure: function() {
                }
            });

            commentElement.value = '';
//            commentElement.style.background="White";
            showHideCommon('add-comment-div');
            return true;
        }else{
            CARBON.showWarningDialog(reason);
            return false;
        }
    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"]);
}

function listComment(path){
    sessionAwareFunction(function() {
        new Ajax.Request('../info/comment-ajaxprocessor.jsp', {
            method: 'post',
            parameters: {path: path, list: "true"},
            onSuccess: function(transport) {
                $('commentsList').innerHTML = transport.responseText;
                alternateTableRows('commentsTable', 'tableEvenRow', 'tableOddRow');
            },

            onFailure: function(transport) {
                showRegistryError(transport.responseText);
            }
        });
    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"]);
}

function applyTag(e) {
    var reason="";

    var keyCode;
    if(e!='null'){
	    if (window.event) {
	        keyCode = window.event.keyCode;
	    } else {
	        keyCode = e.which;
	    }
    }

    if (keyCode == 13 || keyCode == 0 || keyCode == 1) {
      
        var path = document.getElementById('tfPath').value;
        var tag = $('tfTag').value;
        reason+=validateForInput($('tfTag'),org_wso2_carbon_registry_info_ui_jsi18n["tag.name"]);
        reason+=validateEmpty($('tfTag'),org_wso2_carbon_registry_info_ui_jsi18n["tag.name"]);
        reason+=validateTags($('tfTag'),org_wso2_carbon_registry_info_ui_jsi18n["tag.name"]);

        //reason+=validateIllegal($('tfTag'),"Tag Name");
        if(reason==""){
            sessionAwareFunction(function() {
                new Ajax.Request('../info/tag-ajaxprocessor.jsp', {
                    method: 'post',
                    parameters: {tag: tag, path: path},

                    onSuccess: function(transport) {
                        $('tagList').innerHTML = transport.responseText;
                    },

                    onFailure: function(transport) {
                        showRegistryError(transport.responseText);
                    }
                });
                $('tfTag').value = "";
                showHideCommon('tagAddTable');
            }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"]);
        }
        else{
            CARBON.showWarningDialog(reason);
        }
    }
}

function setRating(path, rating) {
    sessionAwareFunction(function() {
        new Ajax.Request('../info/rating-ajaxprocessor.jsp', {
            method: 'post',
            parameters: {path: path, rating: rating},

            onSuccess: function(transport) {
                $('ratingDiv').innerHTML = transport.responseText;
            },

            onFailure: function(transport) {
                showRegistryError(transport.responseText);
            }

        });
    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"]);
}

var subscribeConfirms = 0;
function subscribe(path) {
    sessionAwareFunction(function() {

    if(subscribeConfirms != 0){
        return;
    }
    subscribeConfirms++;
    var endpoint = "";
    var digest = "";
    var reason = "";
    var eventName = $('eventList').value;
    var delimiter = "";
    var notification = $('notificationMethodList').value;

    if($('hierarchicalSubscriptionInfo')!= null){
        if ($('hierarchicalSubscriptionInfo').style.display != null){
            delimiter = $('hierarchicalSubscriptionList').value;
        }
    }


    if ($('subscriptionDataEmail').style.display == "") {
        reason += validateEmail($('subscriptionEmail'));
        switch ($('digestDeliveryEmail').value) {
            case "0":
                digest = "";
                break;
            case "1":
                digest = "digest://h/";
                break;
            case "2":
                digest = "digest://d/";
                break;
            case "3":
                digest = "digest://w/";
                break;
            case "4":
                digest = "digest://f/";
                break;
            case "5":
                digest = "digest://m/";
                break;
        }
        endpoint += digest + "mailto:" + $('subscriptionEmail').value;
    } else if ($('subscriptionDataUserProfile').style.display == "") {
        switch ($('digestDeliveryUser').value) {
            case "0":
                digest = "";
                break;
            case "1":
                digest = "digest://h/";
                break;
            case "2":
                digest = "digest://d/";
                break;
            case "3":
                digest = "digest://w/";
                break;
            case "4":
                digest = "digest://f/";
                break;
            case "5":
                digest = "digest://m/";
                break;
        }
        endpoint += digest + "user://" + $('subscriptionUserProfile').value;
        reason += validateEmpty($('subscriptionUserProfile'), org_wso2_carbon_registry_info_ui_jsi18n["user.name"]);
        if (reason == "") {
            reason += validateUserExists($('subscriptionUserProfile').value);
        }
        if (reason == "") {
            reason += validateProfileExists($('subscriptionUserProfile').value);
            if (reason != "") {
                CARBON.showConfirmationDialog(reason + " " +
                        org_wso2_carbon_registry_info_ui_jsi18n["are.you.sure.you.want.to.continue"], 
                        function() {
                    subscribeConfirms = 0;
                    sessionAwareFunction(function() {
                    new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                        method: 'post',
                        parameters: {path: path, endpoint: endpoint, eventName: eventName, delimiter:delimiter},
                        onSuccess: function(transport) {
                            $('subscriptionDiv').innerHTML = transport.responseText;
                            alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
                        },
                        onFailure: function(transport) {
                            showRegistryError(transport.responseText);
                        }
                    });
                    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                },function() {
                    subscribeConfirms = 0;
                }, function() {
                    subscribeConfirms = 0;
                });
                return;
            }
        }
    } else if ($('subscriptionDataRoleProfile').style.display == "") {
        switch ($('digestDeliveryRole').value) {
            case "0":
                digest = "";
                break;
            case "1":
                digest = "digest://h/";
                break;
            case "2":
                digest = "digest://d/";
                break;
            case "3":
                digest = "digest://w/";
                break;
            case "4":
                digest = "digest://f/";
                break;
            case "5":
                digest = "digest://m/";
                break;
        }
        endpoint += digest + "role://" + $('subscriptionRoleProfile').value;
        reason += validateEmpty($('subscriptionRoleProfile'), org_wso2_carbon_registry_info_ui_jsi18n["role.name"]);
        if (reason == "") {
            reason += validateRoleExists($('subscriptionRoleProfile').value);
        }
        if (reason == "") {
            reason += validateRoleProfileExists($('subscriptionRoleProfile').value);
            if (reason != "") {
                CARBON.showConfirmationDialog(reason + " " +
                        org_wso2_carbon_registry_info_ui_jsi18n["are.you.sure.you.want.to.continue"],
                        function() {
                    subscribeConfirms = 0;
                    sessionAwareFunction(function() {
                    new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                        method: 'post',
                        parameters: {path: path, endpoint: endpoint, eventName: eventName, delimiter:delimiter},
                        onSuccess: function(transport) {
                            $('subscriptionDiv').innerHTML = transport.responseText;
                            alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
                        },
                        onFailure: function(transport) {
                            showRegistryError(transport.responseText);
                        }
                    });
                    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                },function() {
                    subscribeConfirms = 0;
                }, function() {
                    subscribeConfirms = 0;
                });
                return;
            }
        }
    } else if ($('subscriptionDataWorkList').style.display == "") {
        endpoint += digest + "work://" + $('subscriptionWorkList').value;
        reason += validateEmpty($('subscriptionWorkList'), org_wso2_carbon_registry_info_ui_jsi18n["role.name"]);
        if (reason == "") {
            reason += validateRoleExists($('subscriptionWorkList').value);
        }
        if (reason != "") {
            CARBON.showConfirmationDialog(reason + " " +
                    org_wso2_carbon_registry_info_ui_jsi18n["are.you.sure.you.want.to.continue"],
                    function() {
                        subscribeConfirms = 0;
                        sessionAwareFunction(function() {
                            new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                                method: 'post',
                                parameters: {path: path, endpoint: endpoint, eventName: eventName, delimiter:delimiter},
                                onSuccess: function(transport) {
                                    $('subscriptionDiv').innerHTML = transport.responseText;
                                    alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
                                },
                                onFailure: function(transport) {
                                    showRegistryError(transport.responseText);
                                }
                            });
                        }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                    },function() {
                        subscribeConfirms = 0;
                    }, function() {
                        subscribeConfirms = 0;
                    });
            return;
        }
    } else if ($('subscriptionDataJMX').style.display == "") {
        endpoint += "jmx://";
    }else if($('subscriptionDataREST').style.display == ""){
        reason += validateUrl($('subscriptionREST'), org_wso2_carbon_registry_info_ui_jsi18n["web.service.url"]);
        endpoint += $('subscriptionREST').value;
    }else {
        reason += validateUrl($('subscriptionSOAP'), org_wso2_carbon_registry_info_ui_jsi18n["web.service.url"]);
        endpoint += $('subscriptionSOAP').value;
    }
    if (reason == "") {
        switch (notification) {
            case "2":
                var doRest = true;
                sessionAwareFunction(function() {
                    new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                        method: 'post',
                        parameters: {path: path, endpoint: endpoint, eventName: eventName, doRest: doRest,delimiter:delimiter},
                        onSuccess: function(transport) {
                            $('subscriptionDiv').innerHTML = transport.responseText;
                            alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
                            subscribeConfirms = 0;
                        },
                        onFailure: function(transport) {
                            showRegistryError(transport.responseText);
                            subscribeConfirms = 0;
                        }
                    });
                }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                break;
            case 1:
            case 4:
            case 5:
                sessionAwareFunction(function() {
                    new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                        method: 'post',
                        parameters: {path: path, endpoint: endpoint, eventName: eventName,delimiter:delimiter},
                        onSuccess: function(transport) {
                            $('subscriptionDiv').innerHTML = transport.responseText;
                            alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
                            subscribeConfirms = 0;
                            subscriptionConfirmationAlert($('subscriptionEmail').value);
                        },
                        onFailure: function(transport) {
                            showRegistryError(transport.responseText);
                            subscribeConfirms = 0;
                        }
                    });
                }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                break;
            default:
                sessionAwareFunction(function() {
                    new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                        method: 'post',
                        parameters: {path: path, endpoint: endpoint, eventName: eventName,delimiter:delimiter},
                        onSuccess: function(transport) {
                            $('subscriptionDiv').innerHTML = transport.responseText;
                            alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
                            subscribeConfirms = 0;
                        },
                        onFailure: function(transport) {
                            showRegistryError(transport.responseText);
                            subscribeConfirms = 0;
                        }
                    });
                }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                break;
        }
    } else {
        CARBON.showWarningDialog(reason);
        subscribeConfirms = 0;
    }
    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"]);
}

function subscriptionConfirmationAlert(email_id) {
    var confPath= "/_system/config/repository/components/org.wso2.carbon.email-verification/emailIndex";

            new Ajax.Request('../properties/subscription_confirmation_alert_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {path: confPath, email: email_id},
                onSuccess: function(transport) {
                 var returnValue = transport.responseText;
                 if (returnValue.search(/----EmailExists----/) == -1) {
                     CARBON.showInfoDialog("You are successfully subscribed and confirmation email sent to "+ email_id);
                 }
                },

                onFailure: function(transport) {
                    CARBON.showErrorDialog(transport.responseText);
                    return;
                }
            });
}

function unsubscribe(path, id) {
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog(org_wso2_carbon_registry_info_ui_jsi18n["are.you.sure.you.want.to.unsubscribe"], function() {
            new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                method: 'post',
                parameters: {path: path, id: id},
                onSuccess: function(transport) {
                    $('subscriptionDiv').innerHTML = transport.responseText;
                    alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
                },
                onFailure: function() {
                }
            });

        }, null);
    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"]);
}

function previewRating(ratingDivId, value) {

    var images = $(ratingDivId).getElementsByTagName("img");
    for (var i = 0; i < 5; i++) {
        if (i < value) {
            images[((i + 1) * 2)].src = "../info/images/r4x.gif";
            images[((i + 1) * 2) + 1].src = "../info/images/r4x.gif";
        } else {
            images[((i + 1) * 2)].src = "../info/images/r00.gif";
            images[((i + 1) * 2) + 1].src = "../info/images/r00.gif";
        }
    }

}

function clearPreview(ratingDivId) {

    var ratingDiv = $(ratingDivId);
    var initialState = ratingDiv.getElementsByTagName("span")[0].getAttribute('initialState').split(" ");
    var images = ratingDiv.getElementsByTagName("img");
    for (var i = 0; i < 5; i++) {
        images[((i + 1) * 2)].src = "../info/images/r" + initialState[i] + ".gif";
        images[((i + 1) * 2) + 1].src = "../info/images/r" + initialState[i] + ".gif";
    }

}
/*function showComments(){
showHideCommon('commentsIconExpanded');
showHideCommon('commentsIconMinimized');
showHideCommon('commentsTable');
}*/
function showComments(){
	if($('commentsIconExpanded').style.display == "none"){
	//We have to expand all and hide sum
	$('commentsIconExpanded').style.display = "";
	$('commentsIconMinimized').style.display = "none";
	$('commentsExpanded').style.display = "";
	$('commentsMinimized').style.display = "none";
	}
	else{
	$('commentsIconExpanded').style.display = "none";
	$('commentsIconMinimized').style.display = "";
	$('commentsExpanded').style.display = "none";
	$('commentsMinimized').style.display = "";
	}
}
function showRating(){
	if($('ratingIconExpanded').style.display == "none"){
	//We have to expand all and hide sum
	$('ratingIconExpanded').style.display = "";
	$('ratingIconMinimized').style.display = "none";
	$('ratingExpanded').style.display = "";
	$('ratingMinimized').style.display = "none";
	}
	else{
	$('ratingIconExpanded').style.display = "none";
	$('ratingIconMinimized').style.display = "";
	$('ratingExpanded').style.display = "none";
	$('ratingMinimized').style.display = "";
	}
}
function showTags(){
	if($('tagsIconExpanded').style.display == "none"){
	//We have to expand all and hide sum
	$('tagsIconExpanded').style.display = "";
	$('tagsIconMinimized').style.display = "none";
	$('tagsExpanded').style.display = "";
	$('tagsMinimized').style.display = "none";
	}
	else{
	$('tagsIconExpanded').style.display = "none";
	$('tagsIconMinimized').style.display = "";
	$('tagsExpanded').style.display = "none";
	$('tagsMinimized').style.display = "";
	}
}
function showSubscription(){
        if($('subscriptionIconExpanded').style.display == "none"){
        //We have to expand all and hide sum
        $('subscriptionIconExpanded').style.display = "";
        $('subscriptionIconMinimized').style.display = "none";
        $('subscriptionExpanded').style.display = "";
        $('subscriptionMinimized').style.display = "none";
        }
        else{
        $('subscriptionIconExpanded').style.display = "none";
        $('subscriptionIconMinimized').style.display = "";
        $('subscriptionExpanded').style.display = "none";
        $('subscriptionMinimized').style.display = "";
        }
}

function changeVisibility() {
    var visible = $('eventList').value;
    switch (visible) {
        case "0":
            $('notificationMethodList').disabled = true;
            visible = "0";
            break;
        default:
            $('notificationMethodList').disabled = false;
            visible = $('notificationMethodList').value;
            break;
    }
    resetInputVisibility();
    switch (visible) {
        case "1":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataEmail').style.display = "";
            $('digestDeliveryEmail').disabled = false;
            $('subscribeButton').disabled = false;
            break;
        case "2":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataREST').style.display = "";
            $('subscribeButton').disabled = false;
            break;
        case "3":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataSOAP').style.display = "";
            $('subscribeButton').disabled = false;
            break;
        case "4":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataUserProfile').style.display = "";
            $('digestDeliveryUser').disabled = false;
            $('subscribeButton').disabled = false;
            break;
        case "5":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataRoleProfile').style.display = "";
            $('digestDeliveryRole').disabled = false;
            $('subscribeButton').disabled = false;
            break;
        case "6":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataWorkList').style.display = "";
            $('subscribeButton').disabled = false;
            break;
        case "7":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataJMX').style.display = "";
            $('subscribeButton').disabled = false;
            break;
    }

}

function resetInputVisibility() {
    $('subscribeButton').disabled = true;
    $('subscriptionEmail').value = "";
    $('subscriptionREST').value = "";
    $('subscriptionSOAP').value = "";
//    $('subscriptionEmail').style.background = 'White';
//    $('subscriptionREST').style.background = 'White';
//    $('subscriptionSOAP').style.background = 'White';
    $('subscriptionDataInputRecord').style.display = "none";
    $('subscriptionDataEmail').style.display = "none";
    $('digestDeliveryEmail').disabled = true;
    $('digestDeliveryEmail').value = "0";
    $('subscriptionDataREST').style.display = "none";
    $('subscriptionDataSOAP').style.display = "none";
    $('subscriptionDataUserProfile').style.display = "none";
    $('digestDeliveryUser').disabled = true;
    $('digestDeliveryUser').value = "0";
    $('subscriptionDataRoleProfile').style.display = "none";
    $('subscriptionDataWorkList').style.display = "none";
    $('subscriptionDataJMX').style.display = "none";
    $('digestDeliveryRole').disabled = true;
    $('digestDeliveryRole').value = "0";
}

function validateUserExists(username) {
    var error = "";
    new Ajax.Request('../info/is_user_valid-ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {username: username},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----UserExists----/) == -1){
                    error = org_wso2_carbon_registry_info_ui_jsi18n["no.valid.user.exists"] + " <strong>" + username + "</strong>.";
                }
            },
            onFailure: function() {

            }
        });
    return error;
}

function validateProfileExists(username) {
    var error = "";
    new Ajax.Request('../info/is_profile_valid-ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {username: username},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----ProfileExists----/) == -1){
                    error = org_wso2_carbon_registry_info_ui_jsi18n["no.email.exists.on.default.profile"] + " <strong>" + username + "</strong>.";
                }
            },
            onFailure: function() {

            }
        });
    return error;
}

function validateRoleExists(role) {
    var error = "";
    new Ajax.Request('../info/is_role_valid-ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {role: role},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----RoleExists----/) == -1){
                    error = org_wso2_carbon_registry_info_ui_jsi18n["no.valid.role.exists"] + " <strong>" + role + "</strong>.";
                }
            },
            onFailure: function() {

            }
        });
    return error;
}

function validateRoleProfileExists(role) {
    var error = "";
    new Ajax.Request('../info/is_role_profile_valid-ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {role: role},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----RoleProfileExists----/) == -1){
                    error = org_wso2_carbon_registry_info_ui_jsi18n["no.email.exists.on.default.role.profile"] + " <strong>" + role + "</strong>.";
                }
            },
            onFailure: function() {

            }
        });
    return error;
}

function showDel(id){
	var wanted = $('close'+id);
	var all = $('tagList').getElementsByTagName("*");
	for(var i=0;i<all.length;i++){
		if(YAHOO.util.Dom.hasClass(all[i],'closeButton')){
			all[i].style.display="none";
		}
	}
	wanted.style.display = "";
}
function delTag(tag,path){
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog(org_wso2_carbon_registry_info_ui_jsi18n["are.you.sure.you.want.to.delete.this.tag"], function() {
            new Ajax.Request('../info/tag-delete-ajaxprocessor.jsp', {
                method: 'post',
                parameters: {path: path, tag: tag},
                onSuccess: function(transport) {
                    $('tagList').innerHTML = transport.responseText;
                },

                onFailure: function(transport) {
                    if (transport.responseText.search(/----tagDeleteFailed----/) != -1){
                        CARBON.showWarningDialog(org_wso2_carbon_registry_info_ui_jsi18n["unable.to.delete.tag"]);
                    } else {
                        showRegistryError(transport.responseText);
                    }
                }
            });

        }, null);
    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"]);
}
function delComment(path, commentpath){
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog(org_wso2_carbon_registry_info_ui_jsi18n["are.you.sure.you.want.to.delete.this.comment"], function() {
            new Ajax.Request('../info/comment-delete-ajaxprocessor.jsp', {
                method: 'post',
                parameters: {path: path, commentpath: commentpath},
                onSuccess: function(transport) {
                    $('commentsList').innerHTML = transport.responseText;
                    alternateTableRows('commentsTable', 'tableEvenRow', 'tableOddRow');
                },

                onFailure: function(transport) {
                    if (transport.responseText.search(/----commentDeleteFailed----/) != -1){
                        CARBON.showWarningDialog(org_wso2_carbon_registry_info_ui_jsi18n["unable.to.delete.comment"]);
                    } else {
                        showRegistryError(transport.responseText);
                    }
                }
            });

        }, null);
    }, org_wso2_carbon_registry_info_ui_jsi18n["session.timed.out"]);
}

function validateTags(fld,fldName){
    var error = "";
    //var illegalChars = /([^a-zA-Z0-9_\-\x2E\&\?\/\:\,\s\(\)\[\]])/;
    var illegalChars = /([~!@#;%^*+={}\|\\<>\"\'])/; // disallow ~!@#;%^*+={}|\<>"',
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+fldName+" " + org_wso2_carbon_registry_info_ui_jsi18n["tag.contains.illegal.chars"] + "<br />";
    } else{
//        fld.style.background = 'White';
    }

   return error;
}
