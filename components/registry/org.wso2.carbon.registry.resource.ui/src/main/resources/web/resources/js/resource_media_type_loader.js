/*load media types function is defined in the resource_util.js
  since resource_util.js is used even the user is not login, by placing
  the following function inside the resource_util.js gives
  user not authorized errors. so we are separating the media type loader
  to a separate js file.*/
sessionAwareFunction(function() {
    loadMediaTypes();
}, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
