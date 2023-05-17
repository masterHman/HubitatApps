
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
    subscribe(mainSwitch, "held", onDevicePress)
    subscribe(mainSwitch, "doubleTapped", onDevicePress)
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
        
        input(name: "isPhysicalRequired", type: "bool", title: "Physical Actions Only", submitOnChange: true, defaultValue: true)

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
            addSwitchSelector("doublePressSwitch","Double Press Switch Switch", false)  
            if(doublePressSwitch)
                displayDeviceInfo(doublePressSwitch)
        }

        input(name: "applyHeldSwitch", type: "bool", title: "Enable Held Switch", submitOnChange: true, defaultValue: false)
        if(applyHeldSwitch){
            addSwitchSelector("heldSwitch","Held Switch:", false)  
            if(heldSwitch)
                displayDeviceInfo(heldSwitch)
        }
        
    }    
}

def loadSettings(){
    getAppInfoFromUri("https://raw.githubusercontent.com/masterHman/HubitatApps/main/SmartSwitchRules/SmartSwitchRulesConfigAppSettings.json")
}

def onDeviceToggle(evt) {
    state.mainSwitchValue = evt.value
    logDeviceToggle(evt)
}

def onDevicePress(evt)
{  
    logInfo("${evt.descriptionText}")    
    
    logDebug("onDevicePress -> <br/> evt.descriptionText:[${evt.descriptionText}] <br/> evt.displayed:[${evt.displayed}] <br/> evt.name:[${evt.name}]")    

    if(isPhysicalRequired && evt.isDigital())
    {
        logInfo("Digital Button press detected skipping -> evt.isDigital()=[${evt.isDigital()}]") 
        return
    }
    def desiredValue = getOnOffValue(evt.value)

    if(evt.name == "pushed")
        togglePassthroughDevice(passthroughSwitch, desiredValue)
    if(evt.name == "doubleTapped")
        toggleDevice(doublePressSwitch, desiredValue)
    if(evt.name == "held")
        toggleDevice(heldSwitch, desiredValue)

}

def togglePassthroughDevice(passthroughSwitch, desiredValue){
    if(passthroughSwitch)
    {
        logDebug("togglePassthroughDevice =><br/> [${passthroughSwitch}]")
        if(state.mainSwitchValue == desiredValue){
            if(desiredValue == "on")
                passthroughSwitch.on()
            else
                passthroughSwitch.off()
        }
    }   
}

def toggleDevice(device, desiredValue){
    if(device)
    {
        logDebug("toggleDevice =><br/> [${device}]")
        if(desiredValue == "on")
            device.on()
        else
            device.off()        
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