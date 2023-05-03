
import java.text.SimpleDateFormat
#include TrevTelSolutions.Logging
#include TrevTelSolutions.Common

definition(
    name: "Smart Switch Rule Configuration",
    namespace: "TrevTelSolutions",
    author: "Howard Roberson",
    description: "Add Rules to all the buttons of a switch at once.",
    importUrl: "https://raw.githubusercontent.com/masterHman/HubitatApps/main/SmartSwitchRules/SmartSwitchRulesConfig.groovy",
    iconUrl: "",
    iconX2Url: "",
    singleInstance: false,
    parent: "TrevTelSolutions:Smart Switch Rules"
)

preferences {
    page name: "mainPage", title: "", install: true, uninstall: true
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

private initialize() {
    logDebug("Initialize with settings: ${settings}")
    state.deviceList = [:]
    state.mainSwitchValue = "on"
    logDebug("mainSwitch: ${mainSwitch}")
    subscribe(mainSwitch, "switch", onDeviceToggle)
    subscribe(mainSwitch, "pushed", onDevicePress)
    loadSettings()
}

def mainPage() {
    dynamicPage(name: "") {
        loadSettings()
        addHeaderSection()              
        addBodySection()
        addLoggingSection()
        addFooterSection()       
    }
}

def addBodySection(){        
    section() {  
        if(overrideLabel){ 
            app.updateLabel(configLabel)
        } 
        else {
            def name = "(switch not set)"
            if(mainSwitch){
                name = "<b>" + mainSwitch + "</b>"
            }
            
            def dynamicLabel = name + " Rule Configuration"
            app.updateLabel(dynamicLabel)
            paragraph(app.label)
        } 

        input(name: "overrideLabel", type: "bool", title: "Override Configuration Name", submitOnChange: true, defaultValue: false)
        if(overrideLabel){
            input(name: "configLabel", type: "string", title: "Name", required: false,submitOnChange: true)       
        } 
        else{
            configLabel = ""
        }
        
        addSwitchSelector("mainSwitch","Main Switch:",true)
        //input(name: "mainSwitch", type: "capability.switch", title: "Main Switch:", required: true, multiple: false, submitOnChange: true, width: 4)  
        if(mainSwitch) {
            state.numberOfButtons = mainSwitch.currentValue("numberOfButtons")
            state.switch = mainSwitch.currentValue("switch")
            if(isInfoLogging){
                paragraph("<b>Current States:</b>")
                paragraph("numberOfButtons: <b>" + state.numberOfButtons  + "</b>")
                paragraph("switch : <b>" + state.switch + "</b>")
            }
        } 
           
    }    
    
    section(){
        //section(hideable: true, hidden:true, "Single Press (Passthrough)"){
            input(name: "applyPassthroughSwitch", type: "bool", title: "Enable Single Press (Passthrough) Switch", submitOnChange: true, defaultValue: false)
            if(applyPassthroughSwitch){
                addSwitchSelector("passthroughSwitch","Single Press (Passthrough) Switch", false) 
                if(passthroughSwitch)
                    displayDeviceInfo(passthroughSwitch)               
            }
        //}
        input(name: "applyDoublePressSwitch", type: "bool", title: "Enable Double Press Switch", submitOnChange: true, defaultValue: false)
        if(applyDoublePressSwitch){
            input(name: "doublePressSwitch", type: "capability.switch", title: "Double Press Switch Switch:", required: false, multiple: false, submitOnChange: true)  
            if(doublePressSwitch)
                    displayDeviceInfo(doublePressSwitch)
        }

        input(name: "applyHeldSwitch", type: "bool", title: "Enable Held Switch", submitOnChange: true, defaultValue: false)
        if(applyHeldSwitch){
            input(name: "heldSwitch", type: "capability.switch", title: "Held Switch:", required: false, multiple: false, submitOnChange: true)  
            if(heldSwitch)
                    displayDeviceInfo(heldSwitch)
        }
        }    
}

def loadSettings(){
    getAppInfoFromUri("https://raw.githubusercontent.com/masterHman/HubitatApps/main/SmartSwitchRules/SmartSwitchRulesConfigAppSettings.json")
}

def onDeviceToggle(evt) {
    // if (overrideSwitch && overrideSwitch.id == evt.device.id)
    //     return
    state.mainSwitchValue = evt.value
    logDeviceToggle(evt)
    
     
}

def onDevicePress(evt)
{
    logInfo("onDevicePress -> Device:[${evt.device}] button [${evt.value}] was pressed.")
    if(passthroughSwitch)
    {
        def desiredValue = getOnOffValue(evt.value)
        if(state.mainSwitchValue == desiredValue){
            logDebug("Device id:[${evt.device.id}] was turned [${evt.value}]. Adding to device list.")
            if(desiredValue == "on")
                passthroughSwitch.on()
            else
                passthroughSwitch.off()
        }
    }
}

private addSwitchSelector(name,label,required){
    input(name: name, type: "capability.switch", title: label, required: required, multiple: false, submitOnChange: true, width: 4)  
}

private displayDeviceInfo(device){
    if(isInfoLogging){
        paragraph("<b>Current States:</b>")
        paragraph("numberOfButtons: <b>" + getNumberOfButtons(device)  + "</b>")
        paragraph("switch : <b>" + getSwitchStatus(device) + "</b>")
    }
}

private String getNumberOfButtons(device){
    return device.currentValue("numberOfButtons")
}

private String getSwitchStatus(device){
    return device.currentValue("switch")
}

private String getOnOffValue(String buttonNumber){
    return buttonNumber == "1" ? "on" : "off"
}

private toggleDevices(devicesToToggle){
    logInfo("Toggling Devices: ${devicesToToggle}")
    if (whenDeviceIsTurnedOn) {
        devicesToToggle*.off()
    }
    else {
        devicesToToggle*.on()
    }
    devicesToToggle.each{ device -> state.deviceList.remove(device.id) }    
}

private logDeviceToggle(evt){    
    logInfo("onDeviceToggle -> Device:[${evt.device}] was turned [${evt.value}]")       
    logDebug("onDeviceToggle -> now:${now()} evt.device:${evt.device}, evt.value:${evt.value}, state:${state}, " +
        "${evt.value == "on"} ^ ${whenDeviceIsTurnedOn==true} = ${(evt.value == "on") ^ (whenDeviceIsTurnedOn == true)}")
}

private long getTimeOutValue(){
    logDebug("getTimeOutValue -> timerValue in milliseconds:${(timerValue * 60 * 1000)} " )
    return now() + (timerValue * 60 * 1000)
}