#include TrevTelSolutions.Logging
#include TrevTelSolutions.Common

definition(
    name: "Smart Switch Rules",
    namespace: "TrevTelSolutions",
    author: "Howard Roberson",
    description: "Add Rules to all the buttons of a switch at once.",
    importUrl: "https://raw.githubusercontent.com/masterHman/HubitatApps/main/SmartSwitchRules/SmartSwitchRulesApp.groovy",
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
    getAppInfoFromUri("https://raw.githubusercontent.com/masterHman/HubitatApps/main/SmartSwitchRules/SmartSwitchRulesAppSettings.json")
}