var elementId;
var rootPath;

function showRegistryBrowser(id, path) {
    elementId = id;
    rootPath = path;
    showResourceTree(id, setValue , path);
}

function setValue() {
    if (rootPath == "/_system/config") {
        $(elementId).value = $(elementId).value.replace(rootPath, "conf:");
    } else if (rootPath == "/_system/governance") {
        $(elementId).value = $(elementId).value.replace(rootPath, "gov:");
    }
}
