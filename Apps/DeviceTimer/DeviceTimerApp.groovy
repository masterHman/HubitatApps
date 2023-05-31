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

            addHeaderSection()              
            addBodySection()
            addLoggingSection()
            addFooterSection()
        }
        else{
            showCompleteInstallMsg()
        }
    }
}

def addBodySection(){
    section() {
        app(name: "openApp", appName: state.appInfo.childApp.name, namespace: state.appInfo.namespace, title:"${getFormattedText("add-config", state.appInfo.childApp.label)}", multiple: true)
    }
}

def loadSettings(){
    getAppInfoFromUri("https://raw.githubusercontent.com/masterHman/HubitatApps/main/DeviceTimer/DeviceTimerAppSettings.json")
}