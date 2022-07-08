#include TrevTelSolutions.Logging
#include TrevTelSolutions.Common

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
     page(name: "mainPage", title: "", install: true, uninstall: true)
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
            
            addHeaderSection()   
            
            paragraph(getFormattedText(""))
                app(name: "openApp", appName: state.appInfo.childApp.name, namespace: state.appInfo.namespace, title:"${getFormattedText("add-config", state.appInfo.childApp.label)}", multiple: true)
            }

            addLoggingSection()
        }
        else{
            showCompleteInstallMsg()
        }
        addFooterSection()
    }
}

def addHeaderSection(){
    paragraph(getFormattedText("title",state.appInfo.title))
    paragraph(getFormattedText("description",state.appInfo.description))
}

def addFooterSection(){   
    addVersionSection()
}

def addVersionSection(){
    section() {
        paragraph(getFormattedText("version",state.appInfo.version))
    }
}

def loadSettings(){
    getAppInfoFromUri("https://raw.githubusercontent.com/masterHman/HubitatApps/main/DeviceTimer/DeviceTimerAppSettings.json")
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
    section()
    { 
        paragraph("Please hit 'Done' to complete the install for '${app.label}'")
    }
}

def getIcon(type) {
    if(type == "Add") return "<span class='he-add_2'></span>"
}

def getFormattedText(type, innerText="") {
    if(type == "title") return "<h3 style='font-weight: bold'>${innerText}</h3>"
    if(type == "description") return "<div>${innerText}</div>"
    if(type == "add-config") return "${getIcon('Add')} ${innerText}"
    if(type == "version") return "<div class='mdl-cell mdl-cell--12-col mdl-textfield mdl-js-textfield' style='font-size:9px'><div style='white-space:pre-wrap; text-align: left'>Version ${innerText}</div></div>"
    return "<div>${innerText}</div>"
}
