function addReport() {
    var reason = validateEmpty($('reportName'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.name"]);
    if (reason.length != 0) {
        CARBON.showWarningDialog(reason);
        return;
    }
    reason = validateEmpty($('reportTemplate'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.template"]);
    if (reason.length != 0) {
        CARBON.showWarningDialog(reason);
        return;
    }
    reason = validateEmpty($('reportClass'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.class"]);
    if (reason.length != 0) {
        CARBON.showWarningDialog(reason);
        return;
    }

    var reportName = $('reportName').value;
    var reportTemplate = $('reportTemplate').value;
    var reportType = $('reportType').value;
    var reportClass = $('reportClass').value;
    var customTable = $('customTable');
    var attributes = '';
    if (customTable) {
        var rows = $('customTable').getElementsByTagName('input');
        for (var i = 0; i < rows.length; i++) {
            if ((rows[i].id.indexOf("attribute") == 0) && rows[i].value != "") {
                reason = validateIllegal(rows[i], rows[i].id.substr("attribute".length));
                if (reason.length != 0) {
                    CARBON.showWarningDialog(reason);
                    return;
                }
                attributes += rows[i].id + "|" + rows[i].value + "^";
            }
        }
    }
    sessionAwareFunction(function() {
        new Ajax.Request('../registry-reporting/add_report_ajaxprocessor.jsp', {
            method: 'post',
            parameters: {reportName: reportName, reportTemplate: reportTemplate,
                reportType: reportType, reportClass: reportClass, attributes: attributes},
            onSuccess: function(transport) {
                if (!transport) {return;}
                window.location = "../registry-reporting/reports.jsp?region=region3&item=registry_reporting_menu";
            },
            onFailure: function(transport) {
                showRegistryError(org_wso2_carbon_registry_reporting_ui_jsi18n["unable.to.add.report"]);
            }
        });
    }, org_wso2_carbon_registry_reporting_ui_jsi18n["session.timed.out"]);
}

function stopReport(reportName) {
    sessionAwareFunction(function() {
        new Ajax.Request('../registry-reporting/stop_report_ajaxprocessor.jsp', {
            method: 'post',
            parameters: {reportName: reportName},
            onSuccess: function(transport) {
                if (!transport) {return;}
                window.location = "../registry-reporting/reports.jsp?region=region3&item=registry_reporting_menu";
            },
            onFailure: function(transport) {
                showRegistryError(org_wso2_carbon_registry_reporting_ui_jsi18n["unable.to.stop.report"]);
            }
        });
    }, org_wso2_carbon_registry_reporting_ui_jsi18n["session.timed.out"]);
}

function generateReport() {
    var reason = validateEmpty($('reportName'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.name"]);
    if (reason.length == 0) {
        reason = validateEmpty($('reportTemplate'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.template"]);
    }
    if (reason.length == 0) {
        reason = validateEmpty($('reportType'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.type"]);
    }
    if (reason.length == 0) {
        reason = validateEmpty($('reportClass'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.class"]);
    }
    if (reason.length != 0) {
        CARBON.showWarningDialog(reason);
        return;
    }

    var reportName = $('reportName').value;
    var reportTemplate = $('reportTemplate').value;
    var reportType = $('reportType').value;
    var reportClass = $('reportClass').value;
    var customTable = $('customTable');
    var attributes = '';
    if (customTable) {
        var rows = $('customTable').getElementsByTagName('input');
        for (var i = 0; i < rows.length; i++) {
            if ((rows[i].id.indexOf("attribute") == 0) && rows[i].value != "") {
                reason = validateIllegal(rows[i], rows[i].id.substr("attribute".length));
                if (reason.length != 0) {
                    CARBON.showWarningDialog(reason);
                    return;
                }
                attributes += rows[i].id + "|" + rows[i].value + "^";
            } else if ((rows[i].id.indexOf("attribute") == 0) && $(rows[i].id + "Required")) {
                CARBON.showWarningDialog(org_wso2_carbon_registry_reporting_ui_jsi18n["the.required.attribute"] + " " + rows[i].id.substr("attribute".length) + " " + org_wso2_carbon_registry_reporting_ui_jsi18n["is.empty"])
                return;
            }
        }
    } else {
        CARBON.showWarningDialog(org_wso2_carbon_registry_reporting_ui_jsi18n["report.attributes.have.not.been.loaded"]);
        return;
    }
    sessionAwareFunction(function() {
        window.open('../../registry/resourceReport?reportName=' + reportName + "&reportTemplate=" + reportTemplate + "&reportType=" + reportType + "&reportClass=" + reportClass + "&attributes=" + attributes, '_self');
    }, org_wso2_carbon_registry_reporting_ui_jsi18n["session.timed.out"]);
}

function scheduleReport() {
    var reason = validateEmpty($('reportName'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.name"]);
    if (reason.length == 0) {
        reason = validateEmpty($('reportTemplate'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.template"]);
    }
    if (reason.length == 0) {
        reason = validateEmpty($('reportType'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.type"]);
    }
    if (reason.length == 0) {
        reason = validateEmpty($('reportClass'), org_wso2_carbon_registry_reporting_ui_jsi18n["report.class"]);
    }
    if (reason.length == 0) {
        reason = validateEmpty($('cronExpression'), org_wso2_carbon_registry_reporting_ui_jsi18n["cron.expression"]);
    }
    if (reason.length == 0) {
        reason = validateEmpty($('registryURL'), org_wso2_carbon_registry_reporting_ui_jsi18n["registry.url"]);
    }
    if (reason.length == 0) {
        reason = validateEmpty($('username'), org_wso2_carbon_registry_reporting_ui_jsi18n["username"]);
    }
    if (reason.length == 0) {
        reason = validateEmpty($('password'), org_wso2_carbon_registry_reporting_ui_jsi18n["password"]);
    }
    if (reason.length == 0) {
        reason = validateEmpty($('resourcePath'), org_wso2_carbon_registry_reporting_ui_jsi18n["resource.path"]);
    }
    if (reason.length == 0) {
        reason = validateIllegal($('resourcePath'), org_wso2_carbon_registry_reporting_ui_jsi18n["resource.path"]);
    }
    if (reason.length == 0) {
        reason = validateResourcePathAndLength($('resourcePath'));
    }
    if (reason.length == 0) {
        reason = validateConnection($('registryURL').value, $('username').value, $('password').value);
    }
    if (reason.length != 0) {
        CARBON.showWarningDialog(reason);
        return;
    }

    var reportName = $('reportName').value;
    var reportTemplate = $('reportTemplate').value;
    var reportType = $('reportType').value;
    var reportClass = $('reportClass').value;
    var cronExpression = $('cronExpression').value;
    var registryURL = $('registryURL').value;
    var username = $('username').value;
    var password = $('password').value;
    var resourcePath = $('resourcePath').value;

    var customTable = $('customTable');
    var attributes = '';
    if (customTable) {
        var rows = $('customTable').getElementsByTagName('input');
        for (var i = 0; i < rows.length; i++) {
            if ((rows[i].id.indexOf("attribute") == 0) && rows[i].value != "") {
                reason = validateIllegal(rows[i], rows[i].id.substr("attribute".length));
                if (reason.length != 0) {
                    CARBON.showWarningDialog(reason);
                    return;
                }
                attributes += rows[i].id + "|" + rows[i].value + "^";
            } else if ((rows[i].id.indexOf("attribute") == 0) && $(rows[i].id + "Required")) {
                CARBON.showWarningDialog(org_wso2_carbon_registry_reporting_ui_jsi18n["the.required.attribute"] + " " + rows[i].id.substr("attribute".length) + " " + org_wso2_carbon_registry_reporting_ui_jsi18n["is.empty"])
                return;
            }
        }
    } else {
        CARBON.showWarningDialog(org_wso2_carbon_registry_reporting_ui_jsi18n["report.attributes.have.not.been.loaded"]);
        return;
    }
    sessionAwareFunction(function() {
        new Ajax.Request('../registry-reporting/schedule_report_ajaxprocessor.jsp', {
            method: 'post',
            parameters: {reportName: reportName, reportTemplate: reportTemplate,
                reportType: reportType, cronExpression: cronExpression,reportClass: reportClass,
                registryURL: registryURL, username: username, password: password,
                resourcePath: resourcePath, attributes: attributes},
            onSuccess: function(transport) {
                if (!transport) {return;}
                window.location = "../registry-reporting/reports.jsp?region=region3&item=registry_reporting_menu";
            },
            onFailure: function(transport) {
                showRegistryError(org_wso2_carbon_registry_reporting_ui_jsi18n["unable.to.add.report"]);
            }
        });
    }, org_wso2_carbon_registry_reporting_ui_jsi18n["session.timed.out"]);
}

function validateConnection(registryURL, username, password) {
    var error = "";
    new Ajax.Request('../registry-reporting/connection_valid_ajaxprocessor.jsp',
    {
        method:'post',
        parameters: {registryURL: registryURL, username: username, password: password, random:getRandom()},
        asynchronous:false,
        onSuccess: function(transport) {
            var returnValue = transport.responseText;
            if (returnValue.search(/----ConnectionExists----/) == -1){
                error = org_wso2_carbon_registry_reporting_ui_jsi18n["unable.to.connect"]+ " <strong>" + registryURL + "</strong>.";
            }
        },
        onFailure: function() {

        }
    });
    return error;
}

function submitPage(page, pageNumber){
    sessionAwareFunction(function(){
        location.href="reports.jsp?region=region3&item=registry_reporting_menu&requestedPage="+pageNumber;

    }, org_wso2_carbon_registry_reporting_ui_jsi18n["session.timed.out"])

}

function loadAttributes(reportClass, reportName, isAutoLoad){
    if (!isAutoLoad && (!reportClass || reportClass.length == 0)) {
        CARBON.showWarningDialog(org_wso2_carbon_registry_reporting_ui_jsi18n["unable.to.load.class"]);
    }
    sessionAwareFunction(function(){
        $('report-attributes-div').innerHTML = '';
        new Ajax.Request('../registry-reporting/report_attributes_ajaxprocessor.jsp', {
            method: 'post',
            parameters: {reportClass: reportClass, reportName: reportName},
            onSuccess: function(transport) {
                if (!transport) {return;}
                $('report-attributes-div').innerHTML = transport.responseText;
            },
            onFailure: function(transport) {
                showRegistryError(org_wso2_carbon_registry_reporting_ui_jsi18n["unable.to.load.report.attributes"]);
            }
        });
    }, org_wso2_carbon_registry_reporting_ui_jsi18n["session.timed.out"])

}

function deleteReport(reportName){
    sessionAwareFunction(function(){
        CARBON.showConfirmationDialog(org_wso2_carbon_registry_reporting_ui_jsi18n["are.you.sure.you.want.to.delete"] + "<strong>'" + reportName + "'</strong> " + org_wso2_carbon_registry_reporting_ui_jsi18n["permanently"], function() {
            new Ajax.Request('../registry-reporting/delete_report_ajaxprocessor.jsp', {
                method: 'post',
                parameters: {reportName: reportName},
                onSuccess: function(transport) {
                    if (!transport) {return;}
                    window.location = "../registry-reporting/reports.jsp?region=region3&item=registry_reporting_menu";
                },
                onFailure: function(transport) {
                    showRegistryError(org_wso2_carbon_registry_reporting_ui_jsi18n["unable.to.delete.report"]);
                }
            });
        }, null);
    }, org_wso2_carbon_registry_reporting_ui_jsi18n["session.timed.out"])

}

function copyReport(reportName){
    sessionAwareFunction(function(){
        CARBON.showInputDialog(org_wso2_carbon_registry_reporting_ui_jsi18n["please.provide.name.of.new.report"], function(newName){
            new Ajax.Request('../registry-reporting/copy_report_ajaxprocessor.jsp', {
                method: 'post',
                parameters: {reportName: reportName, newName: newName},
                onSuccess: function(transport) {
                    if (!transport) {return;}
                    window.location = "../registry-reporting/reports.jsp?region=region3&item=registry_reporting_menu";
                },
                onFailure: function(transport) {
                    showRegistryError(org_wso2_carbon_registry_reporting_ui_jsi18n["unable.to.copy.report"]);
                }
            });
        }, function() {});
    }, org_wso2_carbon_registry_reporting_ui_jsi18n["session.timed.out"])

}

function validateIllegal(fld, fldName) {
    var error = "";
    var illegalChars = /([?#^\|<>\"\'])/;
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
        error = org_wso2_carbon_registry_reporting_ui_jsi18n["the"] + " " + fldName + " " +
                org_wso2_carbon_registry_reporting_ui_jsi18n["contains.illegal.chars"] + "<br />";
    } else {
//        fld.style.background = 'White';
    }

    return error;
}