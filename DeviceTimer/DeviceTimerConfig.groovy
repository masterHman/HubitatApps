
import java.text.SimpleDateFormat

definition(
    name: "Device Timer Configuration",
    namespace: "masterHman",
    author: "Howard Roberson",
    description: "Automatically resets devices to preferred on/off setting after specified amount of time",
    importUrl: "https://raw.githubusercontent.com/masterHman/HubitatApps/main/DeviceTimer/DeviceTimerConfig.groovy",
    iconUrl: "",
    iconX2Url: "",
    singleInstance: false,
    parent: "masterHman:Device Timer"
)

preferences {
    page name: "mainPage", title: "Timer Configuration", install: true, uninstall: true
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

    subscribe(devices, "switch", onDeviceToggle)
    runEvery1Minute(scheduleHandler)
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        
        section() {  
            if(overrideLabel){ 
                app.updateLabel(configLabel)
            } 
            else{
                def dynamicLabel = timerValue + " minute " + ((whenDeviceIsTurnedOn == true) ? "off" : "on") + " timer"
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

            input(name: "configuredDeviceList", type: "capability.switch", title: "When one of these devices:", required: true, multiple: true)        
            input(name: "whenDeviceIsTurnedOn", type: "bool", title: "Is turned <b>" + ((whenDeviceIsTurnedOn == true) ? "on</b>" : "off</b>"), defaultValue: false, submitOnChange:true)  
            input(name: "timerValue", type: "number", title: "Wait for...(in minutes)", required: true, defaultValue: 10, submitOnChange: true)            
            paragraph("And turn it back <b>" + ((whenDeviceIsTurnedOn == true) ? "off</b>" : "on</b>")) 
            input(name: "overrideSwitch", type: "capability.switch", title: "But, only if this Switch:", multiple: false, submitOnChange: true)
            if(overrideSwitch){
                input(name: "isOverrideSwitchOn", type: "bool", title: "Is turned <b>" + ((isOverrideSwitchOn == true) ? "on</b>" : "off</b>"), defaultValue: false, submitOnChange:true)            
            }
        }

        section("Logging:", hideable: true, hidden: true) {
            input(name: "isInfoLogging", type: "bool", defaultValue: "true", title: "Enable Info (descriptionText) Logging")  
            input(name: "isDebugLogging", type: "bool", defaultValue: "false", title: "Enable Debug Logging")            
        }
    }
}


def onDeviceToggle(evt) {
    logDeviceToggle(evt)
    
    def desiredValue = getOnOffValue(whenDeviceIsTurnedOn)
    if(evt.value == desiredValue){
        logDebug("Device id:[${evt.device.id}] was turned [${evt.value}]. Adding to device list.")
        state.deviceList[evt.device.id] = getTimeOutValue()
    }else {
        logDebug("Device id:[${evt.device.id}] was turned [${evt.value}]. Removing from device list.") 
        state.deviceList.remove(evt.device.id)
    }
    
}

def scheduleHandler() {
    def expiredDevices = state.deviceList.findAll { it.value < now() }
    def devicesToToggle = configuredDeviceList.findAll { device -> expiredDevices.any { it.key == device.id } }

    logDebug ("scheduleHandler now:${now()} active/state deviceList:${state.deviceList} expiredDevices:${expiredDevices} devicesToToggle:${devicesToToggle}")

    if (overrideSwitch){    
        def desiredOverrideValue = getOnOffValue(isOverrideSwitchOn)
        def overrideValue = overrideSwitch.latestValue("switch") 
        
        logDebug("Override Switch: '${overrideSwitch?.displayName}' is " + overrideSwitch.latestValue("switch") )
        
        if(overrideValue == desiredOverrideValue){ 
            toggleDevices(devicesToToggle)
        }
    }else {
        toggleDevices(devicesToToggle)
    }

    state.deviceList -= actionList
}

private String getOnOffValue(Boolean isSwitchOn){
    return isSwitchOn == true ? "on" : "off"
}

private toggleDevices(Map devicesToToggle){
    logDebug ("toggleDevices: devicesToToggle:${devicesToToggle}")
    if (whenDeviceIsTurnedOn) {
        devicesToToggle*.off()
    }
    else {
        devicesToToggle*.on()
    }
}


private logDebug(msg) {
    if (isDebugLogging) 
        log.debug(msg)
}

private logInfo(msg) {
    if (isInfoLogging) 
        log.debug(msg)
}

private logDeviceToggle(evt){    
    logInfo("onDeviceToggle -> Device:[${evt.device}] was turned [${evt.value}] will start timer if the device was turned [" + ((whenDeviceIsTurnedOn == true) ? "on" : "off") + "]")       
    logDebug("onDeviceToggle -> now:${now()} evt.device:${evt.device}, evt.value:${evt.value}, state:${state}, " +
        "${evt.value == "on"} ^ ${whenDeviceIsTurnedOn==true} = ${(evt.value == "on") ^ (whenDeviceIsTurnedOn == true)}")
}

private int getTimeOutValue(){
    return now() + timerValue * 60 * 1000
}