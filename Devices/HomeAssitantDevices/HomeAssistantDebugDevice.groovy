#include TrevTelSolutions.Logging
#include TrevTelSolutions.Common

metadata
{
    definition(
        name: "Home Assistant Debug Device",
        namespace: "TrevTelSolutions", 
        author: "Howard Roberson", 
        importUrl: "",
        )
    {
        capability "Refresh"
        capability "Health Check"
        capability "Switch"  
    }
    preferences {               
        addLoggingSection()
    }
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
