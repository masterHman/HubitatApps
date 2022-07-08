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
        logDebug("Exception in loadSettings: ${e}")       
    }
}
