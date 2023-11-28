#include HomeAssistantBridge.Logging
#include HomeAssistantBridge.Common

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

definition(
    name: "Home Assistant",
    namespace: "TrevTelSolutions",
    author: "Howard Roberson",
    description: "",
    category: "Integration",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "")

preferences
{
    page(name: "mainPage")
    page(name: "discoveryPage")
    page(name: "advOptionsPage")
}

def mainPage()
{
    dynamicPage(name: "mainPage", title: "", install: true, uninstall: true)
    {
        section("<b>Home Assistant Information:</b>")
        {
            input ("ip", "text", title: "IP Address", description: "Home Assistant IP Address", required: true)
            input ("port", "text", title: "Port", description: "Home Assistant Port Number", required: true, defaultValue: "8123")
            input ("token", "text", title: "Long-Lived Access Token", description: "Home Assistant Access Token", required: true)
            input ("template", "text", title: "Template", description: "Home Assistant Template to filter devices.", required: true)
            input (name: "secure", type: "bool", title: "Require secure connection", defaultValue: false, required: true)
            input (name: "ignoreSSLIssues", type: "bool", title: "Ignore SSL Issues", defaultValue: false, required: true)        
        }
        section("<b>Configuration options:</b>")
        {
            href(page: "discoveryPage", title: "<b>Discover and select devices</b>", description: "Query Home Assistant for all currently configured devices.  Then select which entities to Import to Hubitat.", params: [runDiscovery : true])
            href(page: "advOptionsPage", title: "<b>Configure advanced options</b>", description: "Advanced options for manual configuration")
        }
        section("App Name") {
            label title: "Optionally assign a custom name for this app", required: false
        }        
        addLoggingSection()
    }
}

def linkToMain()
{
    section
    {
        href(page: "mainPage", title: "<b>Return to previous page</b>", description: "")
    }
}

def discoveryPage(params)
{
    dynamicPage(name: "discoveryPage", title: "", install: true, uninstall: true)
    {
        state.validEntityTypes = [
                        "fan", 
                        "switch", 
                        "light", 
                        "binary_sensor",
                        "button", 
                         "device_tracker", 
                         "cover", 
                         "lock", 
                         "climate", 
                         "input_boolean",
                          "sensor"
                          ]
            logDebug(state.validEntityTypes)

        if(params?.runDiscovery)
        {
            state.entityList = [:]            
            // state.domainList = [
            //     "fan":[:], 
            //     "switch":[:], 
            //     "light":[:],
            //     "binary_sensor":[:], 
            //     "sensor":[:], 
            //     "device_tracker":[:], 
            //     "cover":[:], 
            //     "lock":[:],
            //     "climate":[:],
            //     "input_boolean":[:],
            //     "button":[:]
            //     ]

            // def domain
            def resp = httpPostExec(genParamsMain("template", getTemplateBody()))
            logDebug("resp: ${resp}")

            if(resp?.data)
            {
               logDebug("data size: ${resp.data.getText()}")
               // logDebug("resp.data: ${resp.data}")
                resp.data.each
                {
                    domain = it.entity_id?.tokenize(".")?.getAt(0)
                    logDebug(domain)
                    if(state.validEntityTypes.contains(domain))
                    {
                        //state.domainList[domain].put(it.entity_id, "${it.attributes?.friendly_name} (${it.entity_id})")
                        state.entityList.put(it.entity_id, "${it.attributes?.friendly_name} (${it.entity_id})")
                        logDebug(state.entityList[it.entity_id])
                    }
                }

                state.entityList = state.entityList.sort { it.value }
            }
            else 
            {                
               logDebug("resp was... null? ${resp}")
            }
        }
        
        section
        {
            input(name: "includeList", type: "enum", title: "Select any devices to <b>include</b> from Home Assistant Device Bridge", options: state.entityList, required: false, multiple: true, offerAll: true)
        }
        
        linkToMain()
    }
}

def checkIfFiltered(entity)
{
    if(enableFiltering || (null == enableFiltering))
    {
        return shouldFilter(entity)
    }
    
    return false
}

def shouldFilter(entity)
{
    return !(includeList?.contains(entity) || accessCustomFilter("get")?.contains(entity))    
}

def cullGrandchildren()
{
    // remove all child devices that aren't currently on either filtering list
    
    def ch = getChild()
    
    ch?.getChildDevices()?.each()
    {
        def entity = it.getDeviceNetworkId()?.tokenize("-")?.getAt(1)        
        if(shouldFilter(entity))
        {
            ch.removeChild(entity)
        }
    }
}

def accessCustomFilter(op, val = null)
{
    if(!["add", "del", "clear", "get"].contains(op))
    {
        return
    }
    
    def list = state.customFilterList ?: []
    
    switch(op)
    {
        case "add":
            !list.contains(val.toString()) ? ((val?.toString()) ? list.add(val.toString()) : null) : null
            break
        case "del":
            list.remove(val.toString())
            break
        case "clear":
            list.clear()
            break
        case "get":
            return list
            break
    }
    
    state.customFilterList = list
}

def advOptionsPage()
{
    dynamicPage(name: "advOptionsPage", title: "", install: true, uninstall: true)
    {
        if(wasButtonPushed("clickToAdd"))
        {
            accessCustomFilter("add", eId)
            clearButtonPushed()
        }
        
        if(wasButtonPushed("clickToRemove"))
        {
            accessCustomFilter("del", eId)
            clearButtonPushed()
        }
        
        if(wasButtonPushed("removeAll"))
        {
            accessCustomFilter("clear")
            clearButtonPushed()
        }        
        app.updateSetting("eId", "")
        
        if(wasButtonPushed("cleanupUnused"))
        {
            cullGrandchildren()
            clearButtonPushed()
        }
        
        section(hideable: true, hidden: false, title: "Entity filtering options")
        {
            input("enableFiltering", "bool", title: "Only pass through user-selected and manually-added entities? (disable this option to pass all through)<br><br>", defaultValue: true, submitOnChange: true)
        }
        
        section(hideable: true, hidden: false, title: "Manually add an entity to be included")
        {
            paragraph "<b>Manually added entities:</b> ${accessCustomFilter("get")}"
            input name: "eId", type: "text", title: "Entity ID", description: "ID"
            input(name: "clickToAdd", type: "button", title: "Add entity to list", width:2)
            input(name: "clickToRemove", type: "button", title: "Remove entity from list", width:2)
            input(name: "removeAll", type: "button", title: "Remove all that were manually added to list? (use carefully!)")
        }
        
        section(hideable: true, hidden: false, title: "System administration options")
        {
            input(name: "cleanupUnused", type: "button", title: "Remove all child devices that are not currently either user-selected or manually-added (use carefully!)")
        }
        
        linkToMain()
    }
}

def installed()
{
    def ch = getChild()
    if(!ch)
    {
        ch = addChildDevice("ymerj", "HomeAssistant Hub Parent", now().toString(), [name: "Home Assistant Device Bridge", label: "Home Assistant Device Bridge (${ip})", isComponent: false])
    }
    
    if(ch)
    {
        // propoagate our settings to the child
        ch.updateSetting("ip", ip)
        ch.updateSetting("port", port)
        ch.updateSetting("token", token)
        ch.updateSetting("secure", secure)

        ch.updated()
    }
}

def getChild()
{
    return getChildDevices()?.getAt(0)
}

def uninstalled()
{
    deleteChildren()
}

def deleteChildren()
{
    getChildDevices()?.each
    {
        deleteChildDevice(it.getDeviceNetworkId())
    }
}

def updated()
{
    installed()
}

void appButtonHandler(btn)
{
    // flag button pushed and let pages sort it out
    setButtonPushed(btn)
}

def setButtonPushed(btn)
{
    state.button = [btn: btn]
}

def wasButtonPushed(btn)
{
    return state.button?.btn == btn
}

def clearButtonPushed()
{
    state.remove("button")
}

def getTemplateBody()
{ 
    def templateValue = '{"template": "' + "${template}" + '"}'
    logDebug("templateValue: ${templateValue}")
    
    return templateValue
}

def genParamsMain(suffix, body = null)
{
    def url = getBaseURI() + suffix
    logDebug("HomeAssistant URL: ${url}")
    def params =
        [
            uri: url,
            headers:
            [
                'Authorization': "Bearer ${token}",
                'Content-Type': "application/json"
            ],
            ignoreSSLIssues: ignoreSSLIssues
        ]
    
    if(body)
    {
        params['body'] = body
    }
 
    return params
}

def getBaseURI()
{
    if(secure) return "https://${ip}:${port}/api/"
    return "http://${ip}:${port}/api/"
}

def httpGetExec(params, throwToCaller = false)
{
    //logDebug("httpGetExec(${params})")
    
    try
    {
        def result
        httpGet(params)
        { resp ->
            if (resp)
            {
                logDebug("resp.data = ${resp.data}")
                result = resp
            }
        }
        return result
    }
    catch (Exception e)
    {
        logDebug("httpGetExec() failed: ${e.message}")
        //logDebug("status = ${e.getResponse().getStatus().toInteger()}")
        if(throwToCaller)
        {
            throw(e)
        }
    }
}

def httpPostExec(params, throwToCaller = false)
{
    logDebug("httpPostExec(${params})")
    
    try
    {
        def result
        httpPost(params)
        { response ->
            if (response.status == 200)
            {
                logDebug("Success! response.data = ${response.data}")
                result = response.data
            }
            else{
                log.error("Status Code: ${response.status}")
            }
        }
        return result
    }
    catch (Exception e)
    {
        logDebug("httpPostExec() failed: ${e.message}")
        if(throwToCaller)
        {
            throw(e)
        }
    }
}
