#include TrevTelSolutions.Logging
#include TrevTelSolutions.Common

// definition(
//     name: "Generic Status/Variable Driver",
//     namespace: "TrevTelSolutions",
//     author: "Howard Roberson",
//     description: "Generice HE-HA-control Device for Home Assistant Device Status'",
//     importUrl: "https://raw.githubusercontent.com/masterHman/HubitatApps/main/StatusDevice/StatusDevice.groovy",
//     iconUrl: "",
//     iconX2Url: "",
//     singleInstance: true
// )

// preferences {
//      page(name: "mainPage", title: "", install: true, uninstall: true)
// } 


metadata
{
    definition(
        name: "Generic Component Variable", 
        namespace: "TrevTelSolutions", 
        author: "Howard Roberson", 
        importUrl: "https://raw.githubusercontent.com/masterHman/HubitatApps/main/StatusDevice/StatusDevice.groovy",
        )
    {
        capability "Refresh"
        capability "Health Check"
        capability "Sensor"
		capability "Variable"
    }
    preferences {       
        input(name: "variableName", type: "string", title: "Variable Name", required: false, defaultValue: "")             
        input(name: "isInfoLogging", type: "bool", defaultValue: "true", title: "Enable Info (descriptionText) Logging")  
        input(name: "isDebugLogging", type: "bool", defaultValue: "false", title: "Enable Debug Logging")   
    }
    attribute "value", "number"   
    attribute "valueStr", "string"
    attribute "unit", "string"
    attribute "healthStatus", "enum", ["offline", "online"]
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
    
    //https://docs2.hubitat.com/developer/interfaces/hub-variable-api
    //Get list of variables //Map getAllGlobalVars()
    //Save the type of variable
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
            updateAttr("value", it.value, it.unit_of_measurement)
            updateAttr("valueStr", it.value, it.unit_of_measurement)
            updateAttr("unit", it.unit_of_measurement)

             if (variableName != ""){        
                setVariable(it.value)
             }
        }
    }
}

void setVariable(String value) {    
    logDebug("Updating variable:"+variableName +" to value:" + value)
    
    
    if(setGlobalVar(variableName.trim(), value) == false)
        logError("Failed to update variable!")
}

BigDecimal AsBigDecimal(String value)
{
    return new BigDecimal(value);
}

Integer AsInt(String value)
{
    return Integer.parseInt(value);
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
