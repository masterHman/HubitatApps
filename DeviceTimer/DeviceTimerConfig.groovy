
import java.text.SimpleDateFormat

definition(
    name: "Device Timer Configuration",
    namespace: "masterHman",
    author: "Howard Roberson",
    description: "Automatically resets devices to preferred on/off setting after specified amount of time",
    importUrl: "",
    iconUrl: "",
    iconX2Url: "",
    singleInstance: false
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
            label(title: "Name", required: false)
            input(name: "devices", type: "capability.switch", title: "When one of these devices:", required: true, multiple: true)        
            input(name: "whenDeviceIsTurnedOn", type: "bool", title: "Is turned <b>" + ((whenDeviceIsTurnedOn == true) ? "on</b>" : "off</b>"), defaultValue: false, submitOnChange:true)  
            input(name: "timerValue", type: "number", title: "Wait for...(in minutes)", required: true)            
            paragraph("And turn it back <b>" + ((whenDeviceIsTurnedOn == true) ? "off</b>" : "on</b>")) 
            input(name: "master", type: "capability.switch", title: "But, only if this Switch:", multiple: false, submitOnChange: true)
            if(master){
                input(name: "isMasterOn", type: "bool", title: "Is turned <b>" + ((isMasterOn == true) ? "on</b>" : "off</b>"), defaultValue: false, submitOnChange:true)            
            }          
        }

        section("Logging:", hideable: true, hidden: true) {
            input(name: "isInfoLogging", type: "bool", defaultValue: "true", title: "Enable Info (descriptionText) Logging")  
            input(name: "isDebugLogging", type: "bool", defaultValue: "false", title: "Enable Debug Logging")            
        }
    }
}

/**
Event handler called when any of the devices are toggled.
**/
def onDeviceToggle(evt) {
    logDeviceToggle(evt)

    if(evt.value == "on" && whenDeviceIsTurnedOn == true){
        logDebug("Device id:[${evt.device.id}] was turned [${evt.value}]. Adding to device list.")
        state.deviceList[evt.device.id] = getTimeOutValue()
        return        
    }else if(evt.value == "off" && whenDeviceIsTurnedOn == false){   
        logDebug("Device id:[${evt.device.id}] was turned [${evt.value}]. Adding to device list.")     
        state.deviceList[evt.device.id] = getTimeOutValue()
        return    
    }
    
    logDebug("Device id:[${evt.device.id}] was turned [${evt.value}]. Removing from device list.") 
    state.deviceList.remove(evt.device.id)
}

def scheduleHandler() {
    // Find all map entries with an off-time that is earlier than now
    def actionList = state.deviceList.findAll { it.value < now() }

    // Find all devices that match the off-entries from above
    def deviceList = devices.findAll { device -> actionList.any { it.key == device.id } }

    logDebug ("scheduleHandler now:${now()} offList:${state.deviceList} actionList:${actionList} deviceList:${deviceList}")

    // Call off(), or on() if inverted, on all relevant devices and remove them from offList
    if (!master || master.latestValue("switch") == "on") {
        if (whenDeviceIsTurnedOn) deviceList*.on()
        else deviceList*.off()
    } else {
        logDebug "Skipping actions because MasterSwitch '${master?.displayName}' is Off"
    }
    state.deviceList -= actionList
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