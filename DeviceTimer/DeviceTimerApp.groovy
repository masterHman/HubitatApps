definition(
    name: "Device Timer",
    namespace: "masterHman",
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
    childApps.each {child ->
        logDebug( "Child app: ${child.label}")
    }
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        installCheck()
        if(state.appInstalled == 'COMPLETE'){
            section("Instructions:", hideable: true, hidden: true) {
                paragraph "<b>Information</b>"
                paragraph "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
            }
            section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
                app(name: "anyOpenApp", appName: "Device Timer Configuration", namespace: "masterHman", title: "<b>Add a new timer configuration</b>", multiple: true)
            }
           
            display2()
        }
    }
}

def installCheck(){
    display()
    state.appInstalled = app.getInstallationState() 
    if(state.appInstalled != 'COMPLETE'){
        section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
    }
    else{
        logDebug( "Parent Installed OK")
    }
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {			// Modified from @Stephack Code   
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    getHeaderAndFooter()
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
        paragraph getFormat("line")
    }
}

def display2() {
    section() {
        paragraph getFormat("line")
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        paragraph "${state.footerMessage}"
    }       
}

def getHeaderAndFooter() {
    if(logEnable) logDebug "In getHeaderAndFooter (${state.version})"
    def params = [
        uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json",
        requestContentType: "application/json",
        contentType: "application/json",
        timeout: 30
    ]
    
    try {
        def result = null
        httpGet(params) { resp ->
            state.headerMessage = resp.data.headerMessage
            state.footerMessage = resp.data.footerMessage
        }
        if(logEnable) logDebug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        if(logEnable) logDebug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}

private logDebug(msg) {
    if (debugEnable) 
        log.debug(msg)
}