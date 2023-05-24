
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
    logDebug("mainSwitch: ${mainSwitch}")   
    if(mainSwitch){
        state.switch = mainSwitch.currentValue("switch")
        subscribe(mainSwitch, "switch", onDeviceToggle)
        subscribe(mainSwitch, "pushed", onDevicePress)
        subscribe(mainSwitch, "held", onDevicePress)
        subscribe(mainSwitch, "doubleTapped", onDevicePress)
    }
    else
        state.switch = "unknown"

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
        addLabelOverrideSection();
        
        input(name: "isPhysicalRequired", type: "bool", title: "Physical Actions Only", submitOnChange: true, defaultValue: true)

        addSwitchSelector("mainSwitch","Main Switch:",true,false)
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
            input(name: "applyPassthroughSwitch", type: "bool", title: "Enable Single Press (Passthrough) Switch", submitOnChange: true, defaultValue: false)
            if(applyPassthroughSwitch){
                addSwitchSelector("passthroughSwitch","Single Press (Passthrough) Switch", false) 
                if(passthroughSwitch)
                    displayDeviceInfo(passthroughSwitch)               
            }
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

def addLabelOverrideSection(){
    if(overrideLabel){ 
            app.updateLabel(configLabel)
        } 
        else {
            def dynamicLabel = "(switch not set)"
            if(mainSwitch){
                dynamicLabel = "<b>${mainSwitch}</b> Rule Configuration"
            }            
            app.updateLabel(dynamicLabel)
            paragraph(app.label)
        } 

        input(name: "overrideLabel", type: "bool", title: "Override Configuration Name", submitOnChange: true, defaultValue: false)
        if(overrideLabel){
            input(name: "configLabel", type: "string", title: "Name", required: false, submitOnChange: true)       
        } 
        else{
            configLabel = ""
        }    
}

def loadSettings(){
    getAppInfoFromUri("https://raw.githubusercontent.com/masterHman/HubitatApps/main/SmartSwitchRules/SmartSwitchRulesConfigAppSettings.json")
}

def onDeviceToggle(evt) {
    
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
    logDebug("mainSwitch: ${mainSwitch}")   
    if(mainSwitch){
        state.switch = mainSwitch.currentValue("switch")
        subscribe(mainSwitch, "switch", onDeviceToggle)
        subscribe(mainSwitch, "pushed", onDevicePress)
        subscribe(mainSwitch, "held", onDevicePress)
        subscribe(mainSwitch, "doubleTapped", onDevicePress)
    }
    else
        state.switch = "unknown"

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
        addLabelOverrideSection();
        
        input(name: "isPhysicalRequired", type: "bool", title: "Physical Actions Only", submitOnChange: true, defaultValue: true)

        addSwitchSelector("mainSwitch","Main Switch:",true,false)
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
            input(name: "applyPassthroughSwitch", type: "bool", title: "Enable Single Press (Passthrough) Switch", submitOnChange: true, defaultValue: false)
            if(applyPassthroughSwitch){
                addSwitchSelector("passthroughSwitch","Single Press (Passthrough) Switch", false) 
                if(passthroughSwitch)
                    displayDeviceInfo(passthroughSwitch)               
            }
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

def addLabelOverrideSection(){
    if(overrideLabel){ 
            app.updateLabel(configLabel)
        } 
        else {
            def dynamicLabel = "(switch not set)"
            if(mainSwitch){
                dynamicLabel = "<b>${mainSwitch}</b> Rule Configuration"
            }            
            app.updateLabel(dynamicLabel)
            paragraph(app.label)
        } 

        input(name: "overrideLabel", type: "bool", title: "Override Configuration Name", submitOnChange: true, defaultValue: false)
        if(overrideLabel){
            input(name: "configLabel", type: "string", title: "Name", required: false, submitOnChange: true)       
        } 
        else{
            configLabel = ""
        }    
}

def loadSettings(){
    getAppInfoFromUri("https://raw.githubusercontent.com/masterHman/HubitatApps/main/SmartSwitchRules/SmartSwitchRulesConfigAppSettings.json")
}

def onDeviceToggle(evt) {
    state.switch = evt.value
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
    if(passthroughSwitch){
        if(state.switch == desiredValue){
            toggleDevices(passthroughSwitch, desiredValue)
        }
    }   
}

def toggleDevice(device, desiredValue){
    if(device){
        toggleDevices(device, desiredValue)
    }
}

private addSwitchSelector(name,label,required,allowMultiple = true){
    input(name: name, type: "capability.switch", title: label, required: required, multiple: allowMultiple, submitOnChange: true, width: 4)  
}

private displayDeviceInfo(devices){
    if(isInfoLogging){
        paragraph("<b>Current States:</b>")
        for (device in devices) {            
            paragraph("Name:     <b>${device.name}</b>")
            paragraph("NumberOfButtons:     <b>${getNumberOfButtons(device)}</b>")
            paragraph("Switch:     <b>${getSwitchStatus(device)}</b>")
        }
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

private toggleDevices(devicesToToggle, desiredValue){
    logInfo("Toggling Devices: ${devicesToToggle}")
    if (desiredValue == "on") {        
        devicesToToggle*.on()
    }
    else {
        devicesToToggle*.off()
    }
}

private logDeviceToggle(evt){        
    logInfo("onDeviceToggle -><br/> Device:[${evt.device}] was turned [${evt.value}]")       
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
    if(passthroughSwitch){
        if(state.switch == desiredValue){
            toggleDevices(passthroughSwitch, desiredValue)
        }
    }   
}

def toggleDevice(device, desiredValue){
    if(device){
        toggleDevices(device, desiredValue)
    }
}

private addSwitchSelector(name,label,required,allowMultiple = true){
    input(name: name, type: "capability.switch", title: label, required: required, multiple: allowMultiple, submitOnChange: true, width: 4)  
}

private displayDeviceInfo(devices){
    if(isInfoLogging){
        paragraph("<b>Current States:</b>")
        for (device in devices) {            
            paragraph("Name:     <b>${device.name}</b>")
            paragraph("NumberOfButtons:     <b>${getNumberOfButtons(device)}</b>")
            paragraph("Switch:     <b>${getSwitchStatus(device)}</b>")
        }
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

private toggleDevices(devicesToToggle, desiredValue){
    logInfo("Toggling Devices: ${devicesToToggle}")
    if (desiredValue == "on") {        
        devicesToToggle*.on()
    }
    else {
        devicesToToggle*.off()
    }
}

private logDeviceToggle(evt){        
    logInfo("onDeviceToggle -><br/> Device:[${evt.device}] was turned [${evt.value}]")       
}