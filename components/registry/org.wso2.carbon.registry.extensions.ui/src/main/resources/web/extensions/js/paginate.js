
function submitExtension(page, pageNumber){
    sessionAwareFunction(function(){
        location.href="list_extensions.jsp?region=region3&item=list_extensions_menu&requestedPage="+pageNumber;

    }, org_wso2_carbon_registry_extensions_ui_jsi18n["session.timed.out"])

}