library (
 author: "masterHman",
 category: "utility",
 description: "Common methods.",
 name: "Common",
 namespace: "TrevTelSolutions",
 documentationLink: "http://www.example.com/"
)


def getAppInfoFromUri(uri){
    def params = [
        uri: uri,
        requestContentType: "application/json",
        contentType: "application/json",
        timeout: 30
    ]
    state.appInfo = [:]
    
    try {
        def result = null
        httpGet(params)
        {
            resp ->
            state.appInfo = 
            [
            name: resp.data.name,
            version: resp.data.version,
            namespace: resp.data.namespace,
            title: resp.data.title,
            description: resp.data.description,
            childApp : [
                name: resp.data.childApp.name,
                label: resp.data.childApp.label                
                ]
            ]
        }
    }
    catch (e) {
        log.error("Exception in loadSettings: ${e}")       
    }
}


def addHeaderSection(){
    section() {
        paragraph(getFormattedText("title",state.appInfo.title))
        paragraph(getFormattedText("description",state.appInfo.description))
    }
}

def addFooterSection(){   
    addVersionSection()
}

def addVersionSection(){
    section() {
        paragraph(getFormattedText("version",state.appInfo.version))
    }
}

def boolean isInstalled(){
    state.appInstalled = app.getInstallationState() 
    if(state.appInstalled != 'COMPLETE'){
        logDebug( "${app.label} NOT Installed!")
        return false
    }
    else{
        logDebug( "${app.label} Installed.")
    }
    return true
}

def showCompleteInstallMsg(){
    section(){ 
        paragraph("Please hit 'Done' to complete the install for '${app.label}'")
    }
}

def getIcon(type) {
    if(type == "Add") return "<span class='he-add_2'></span>"
}

def getFormattedText(type, innerText="") {
    if(type == "title") return "<h3 style='font-weight: bold'>${innerText}</h3>"
    if(type == "description") return "<div>${innerText}</div>"
    if(type == "add-config") return "${getIcon('Add')} ${innerText}"
    if(type == "version") return "<div class='mdl-cell mdl-cell--12-col mdl-textfield mdl-js-textfield' style='font-size:9px'><div style='white-space:pre-wrap; text-align: left'>Version ${innerText}</div></div>"
    return "<div>${innerText}</div>"
}