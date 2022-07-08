library (
 author: "masterHman",
 category: "utility",
 description: "Common methods for logging",
 name: "Logging",
 namespace: "TrevTelSolutions",
 documentationLink: "http://www.example.com/"
)

def addLoggingSection(){
    section("Logging:", hideable: true, hidden: true) {
        input(name: "isInfoLogging", type: "bool", defaultValue: "true", title: "Enable Info (descriptionText) Logging")  
        input(name: "isDebugLogging", type: "bool", defaultValue: "false", title: "Enable Debug Logging")            
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