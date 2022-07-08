definition(
    name: "Device Timer",
    namespace: "TrevTelSolutions",
    author: "Howard Roberson",
    description: "Automatically resets devices to preferred on/off setting after specified amount of time",
    importUrl: "https://raw.githubusercontent.com/masterHman/HubitatApps/main/DeviceTimer/DeviceTimerApp.groovy",
    iconUrl: "",
    iconX2Url: "",
    singleInstance: true
)

preferences {
     page name: "mainPage", title: "", install: true, uninstall: true
} 

def installed() {
    logDebug("Installed with settings: ${settings}")
    initialize()
}

def updated() {
    logDebug("Updated with settings: ${settings}")
    unsubscribe()
    initialize()
}

def initialize() {
    logDebug( "There are ${childApps.size()} child apps")
    childApps.each { 
    child ->
        logDebug("Child app: ${child.label}")
    }
    loadSettings()
}

def mainPage() {
    dynamicPage(name: "") {
        initialize()    
        if(isInstalled()) {
            section() {
            paragraph(getFormattedText("config-label", "Configurations"))
                app(name: "openApp", appName: state.configAppName, namespace: state.namespace, title:"${getFormattedText("add-config", state.configAppTitle)}", multiple: true)
            }

            addLoggingSection()
            addVersionSection()
        }
        else{
            showCompleteInstallMsg()
        }
    }
}

def addLoggingSection(){
    section("Logging:", hideable: true, hidden: true) {
        input(name: "isInfoLogging", type: "bool", defaultValue: "true", title: "Enable Info (descriptionText) Logging")  
        input(name: "isDebugLogging", type: "bool", defaultValue: "false", title: "Enable Debug Logging")            
    }
}

def addVersionSection(){
    section() {
        paragraph(getFormattedText("version",state.version))
    }
}


def loadSettings(){
    def params = [
        uri: "https://raw.githubusercontent.com/masterHman/HubitatApps/main/DeviceTimer/appSettings.json",
        requestContentType: "application/json",
        contentType: "application/json",
        timeout: 30
    ]
    
    try {
        def result = null
        httpGet(params)
        {
            resp ->
            state.version = resp.data.version
            state.appName = resp.data.name
            state.namespace = resp.data.namespace
            state.configAppVersion = resp.data.config.version
            state.configAppName = resp.data.config.name
            state.configAppTitle = resp.data.config.title
        }
    }
    catch (e) {
        logDebug("Exception in loadSettings: ${e}")       
    }
}

def boolean isInstalled(){
    state.appInstalled = app.getInstallationState() 
    if(state.appInstalled != 'COMPLETE'){
        logDebug( "${app.label} NOT Installed!")
        return false
    }
    else{
        logDebug( "${app.label} Installed.")
    }
    return true
}

def showCompleteInstallMsg(){
    section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
}

def getIcon(type) {
    if(type == "Add") return "<span class='he-add_2'></span>"
}

def getFormattedText(type, innerText="") {
    if(type == "config-label") return "<div style='font-weight: bold;background-color:#81BC00;'>${innerText}</div>"
    if(type == "add-config") return "${getIcon('Add')} ${innerText}"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${innerText}</h2>"
    if(type == "version") return "<div class='mdl-cell mdl-cell--12-col mdl-textfield mdl-js-textfield' style='font-size:9px'><div style='white-space:pre-wrap; text-align: left'>Version ${innerText}</div></div>"
}

private logDebug(msg) {
    if (isDebugLogging) 
        log.debug(msg)
}

private logInfo(msg) {
    if (isInfoLogging) 
        log.debug(msg)
}