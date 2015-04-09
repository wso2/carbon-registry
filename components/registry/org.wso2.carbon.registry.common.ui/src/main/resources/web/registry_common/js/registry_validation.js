function validateEmpty(fld,fldName) {
    var error = "";
    fld.value = fld.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    if (fld.value.length == 0) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["the.required.field"] + " "+fldName+" " + org_wso2_carbon_registry_common_ui_jsi18n["not.filled"] + "<br />";
    } else {
//        fld.style.background = 'White';
    }
    return error;
}

function validateRegex(regexStr, fldEleArr, fldName) {
    var regex = new RegExp(regexStr);
    var actual = "";
    for (var i=0; i<fldEleArr.size(); ++i) {
        if (i != 0) {
            actual += ":";
        }
        var val = (!fldEleArr[i] ||  fldEleArr[i].value == null || fldEleArr[i].value == undefined)
            ? "" : fldEleArr[i].value;
        actual += val.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    }

    if (!regex.test(actual)) {
        return fldName + " " + org_wso2_carbon_registry_common_ui_jsi18n["invalid.regex"] + ": " +
            regexStr  + "<br />";
    } else {
        return "";
    }
}

function validateUrl(fld,fldName) {

	var error = "";
	var regx = RegExp("(http|https|ftp|file)://[^\\s]*?.*");
	if(!(fld.value.match(regx))){
		error = org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+fldName+" " + org_wso2_carbon_registry_common_ui_jsi18n["not.valid.url"] + "<br />";
	}


	return error;
}
function validateTextForIllegal(fld,fldName) {

    var illegalChars = /([?#^\|<>\"\'])/;
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
       return false;
    } else {
       return true;
    }
}
function validateIllegal(fld,fldName){
    var error = "";
    //var illegalChars = /([^a-zA-Z0-9_\-\x2E\&\?\/\:\,\s\(\)\[\]])/;
    var illegalChars = /([~!@#;%^*+={}\|\\<>\"\',])/; // disallow ~!@#$;%^*+={}|\<>"',
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+fldName+" " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"] + "<br />";
    } else{
//        fld.style.background = 'White';
    }

   return error;
}
function validateIllegalNoPercent(fld,fldName){
    var error = "";
    //var illegalChars = /([^a-zA-Z0-9_\-\x2E\&\?\/\:\,\s\(\)\[\]])/;
    var illegalChars = /([~!@#;^*+={}\|\\<>\"\',])/; // disallow ~!@#$;%^*+={}|\<>"',
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+fldName+" " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"] + "<br />";
    } else{
//        fld.style.background = 'White';
    }

    return error;
}

function validateForInput(fld,fldName){
    var error = "";
    var illegalChars = /(\<[a-zA-Z0-9\s\/]*>)/; // match any starting tag

    if (illegalChars.test(fld.value)) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+fldName+" " + org_wso2_carbon_registry_common_ui_jsi18n["input.contains.illegal.chars"] + "<br />";
    } else{
//        fld.style.background = 'White';
    }
   return error;
}

function validateValueForInput(fldValue,fldName){
    var error = "";
    var illegalChars = /(\<[a-zA-Z0-9\s\/]*>)/; // match any starting tag

    if (illegalChars.test(fldValue)) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["the"] + " " + fldName + " " + org_wso2_carbon_registry_common_ui_jsi18n["input.contains.illegal.chars"] + "<br />";
    }
   return error;
}

function validateToFromDate(from, to) {
    var error = "";
    if (Date.parse(from.value) > Date.parse(to.value)) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["invalid.tofrom.date"];
    }
    return error;
}

function validateDate(fld,fldName){
    var error = "";
    var allowed= /^((0[1-9]|1[0-2])\/([0-2][0-9]|3[0-1])\/[1-2][0-9][0-9][0-9])$/;
    if (fld.value.length == 0 || allowed.test(fld.value)) {
//        fld.style.background = 'White';
    }
    else {
        error = org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+fldName+ " " + org_wso2_carbon_registry_common_ui_jsi18n["invalid.date"] + "<br />";
    }
    return error;
}
function validateUsername(fld) {

    //validates the user name against the given regex given in user-mgt.xml
    var error = "";
    var regexString = document.getElementById('userNameRegex');
    if(regexString != null && regexString.value != null){
        regexString = regexString.value;
    } else {
        regexString = '^[\\S]{3,30}$';
    }
    var regex = RegExp(regexString);
    if (fld.value == "") {
        error = org_wso2_carbon_registry_common_ui_jsi18n["no.username"] + "<br />";
    }  else if (!(fld.value.match(regex))) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["illegal.username"] + "<br />";
    } else {
//        fld.style.background = 'White';
    }
    return error;
}

function validatePassword(fld) {
    var error = "";
    var illegalChars = /[\W_]/; // allow only letters and numbers

    if (fld.value == "") {
        error = org_wso2_carbon_registry_common_ui_jsi18n["no.password"] + "<br />";
    } else if ((fld.value.length < 3) || (fld.value.length > 15)) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["wrong.password"] + "<br />";

    } else if (illegalChars.test(fld.value)) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["illegal.password"] + "<br />";
    } /*else if (!((fld.value.search(/(a-z)+/)) && (fld.value.search(/(0-9)+/)))) {
        error = "The password must contain at least one numeral.<br />";
    } */else {
//        fld.style.background = 'White';
    }
   return error;
}

function trim(str)
{
  return str.replace(/^\s+|\s+$/, '');
}
function ltrim(str) { 
	for(var k = 0; k < str.length && isWhitespace(str.charAt(k)); k++) {}
	return str.substring(k, str.length);
}
function isWhitespace(charToCheck) {
	var whitespaceChars = " \t\n\r\f";
	return (whitespaceChars.indexOf(charToCheck) != -1);
}

function validateEmail(fld) {
    var error="";
    var tfld = trim(fld.value);                        // value of field with whitespace trimmed off
    var emailFilter = /^[^@]+@[^@.]+\.[^@]*\w\w$/ ;
    var illegalChars= /[\(\)\<\>\,\;\:\\\"\'\$\[\]]/ ;

    if (fld.value == "") {
        error = org_wso2_carbon_registry_common_ui_jsi18n["no.email"] + "<br />";
    } else if (!emailFilter.test(tfld)) {             //test email for illegal characters
        error = org_wso2_carbon_registry_common_ui_jsi18n["wrong.email"] + "<br />";
    } else if (fld.value.match(illegalChars)) {
        error = org_wso2_carbon_registry_common_ui_jsi18n["illegal.email"] + "<br />";
    } else {
//        fld.style.background = 'White';
    }
    return error;
}

function validateSimpleSearch() {
    // JS injection validation
    if (!validateTextForIllegal(document.forms["searchForm"]["criteria"], "resource path")) {
        CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] +
        " " + "search content" + " " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"]);
        return false;
    }

    var searchText = document.forms["searchForm"]["criteria"].value;
    if (searchText == null || searchText == "") {
        CARBON.showWarningDialog(org_wso2_carbon_registry_search_ui_jsi18n["validate.simple.search"]);
        return false;
    }
    document.forms['searchForm'].submit();
    return true;
}

function validateFilterName(fld, fldName) {

    var illegalChars = /\//; // do not allow slash
    var fnReason = "";

    fnReason += validateEmpty(fld, fldName);

    if (fnReason == "") {
        fnReason += validateForInput(fld, fldName);
    }
    if (fnReason == "") {
        fnReason += validateIllegal(fld, fldName);
    }
    if (fnReason == "" && illegalChars.test(fld.value)) {
        fnReason += org_wso2_carbon_registry_search_ui_jsi18n["filter.name.cannot.contain.slash"];
    }

    return fnReason;
}

function validatePropertyValues() {
    var leftVal = parseInt(document.getElementById('valueLeft').value);
    var rightVal = parseInt(document.getElementById('valueRight').value);

    if (leftVal != "" && rightVal != "") {
        if (leftVal >= rightVal) {
            return 0;
        }
    }
    return 1;
}

function validateEmptyPropertyValues() {

    var leftVal = document.getElementById('valueLeft').value;
    var rightVal = document.getElementById('valueRight').value;
    var opRight = document.getElementById('opRight');
    var propertyName = document.getElementById('#_propertyName').value;

    if (leftVal != "" || rightVal != "") {
        if (propertyName == "") {
            return 1;
        }
    }

    return 0;
}

function validateIllegalContentSearchString(fld, fldName) {
    var error = "";
    //var illegalChars = /([^a-zA-Z0-9_\-\x2E\&\?\/\:\,\s\(\)\[\]])/;
    var illegalChars = /([~!@#$;%^*+{}\|\\<>\"\',\[\]\(\)])/; // disallow ~!@#$;%^*+={}|\<>"',[]()
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
        error = org_wso2_carbon_registry_search_ui_jsi18n["the"] +
        " " + fldName + " " + org_wso2_carbon_registry_search_ui_jsi18n["contains.illegal.chars"] + "<br />";
    } else {
//        fld.style.background = 'White';
    }

    return error;
}

function validateTagsInput(fld, fldName) {
    var error = "";
    var illegalChars = /(^,+$)/; // match any starting tag

    if (illegalChars.test(fld.value)) {
        error = org_wso2_carbon_registry_search_ui_jsi18n["the"] +
        " " + fldName + " " + org_wso2_carbon_registry_search_ui_jsi18n["contains.invalid.tag.search"] + "<br />";
    } else {
//        fld.style.background = 'White';
    }
    if (error != "") {
        return error;
    }

    return validateForInput(fld, fldName);
}

function validateIllegalSearchString(fld, fldName) {
    var error = "";
    var illegalChars = /([~!@#$;^*+{}\|\\<>\"\',\[\]\(\)])/;
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
        error = org_wso2_carbon_registry_search_ui_jsi18n["the"] + " " + fldName + " " + org_wso2_carbon_registry_search_ui_jsi18n["contains.illegal.chars.second"] + "<br />";
    }

    return error;
}
