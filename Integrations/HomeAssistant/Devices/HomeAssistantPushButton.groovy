#include TrevTelSolutions.Logging
#include TrevTelSolutions.Common

metadata
{
    definition(
        name: "Home Assistant Push Button", 
        namespace: "TrevTelSolutions", 
        author: "Howard Roberson", 
        importUrl: "",
        )
    {
        capability "PushableButton"    
    }
    preferences {                  
        input(name: "isInfoLogging", type: "bool", defaultValue: "true", title: "Enable Info (descriptionText) Logging")  
        input(name: "isDebugLogging", type: "bool", defaultValue: "false", title: "Enable Debug Logging")   
    }
    command "push"
}

def push(bn = 1) {
    parent?.componentPress(this.device)
}

def installed() {
    logInfo("Installed...")
    initialize()
}

void updated() {
    logInfo("Updated...")
    initialize()
}

private initialize() {
    logDebug("Initialize with settings: ${settings}")
    refresh()
}

void updateAttr(String aKey, aValue, String aUnit = ""){
    sendEvent(name:aKey, value:aValue, unit:aUnit)
}

void parse(String description) { log.warn "parse(String description) not implemented" }

void parse(List<Map> description) {    
    description.each {
        if (it.name in ["unknown"]) {            
            logInfo(it.descriptionText)
            
            updateAttr("healthStatus", getHealthStatusValue(it.value), it.unit_of_measurement)
        }
    }
}

String getHealthStatusValue(String value) {
    return value == "unavailable" ? "offline" : "online";
}

void refresh() {
    parent?.componentRefresh(this.device)
}

void ping() {
    refresh()
}
