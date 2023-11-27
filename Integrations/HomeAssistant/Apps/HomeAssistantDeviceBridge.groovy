/*
* Home Assistant to Hubitat Integration
*
* Description:
* Allow control of HA devices.
*
* Required Information:
* Home Asisstant IP and Port number
* Home Assistant long term Access Token
*
* Features List:
*
* Licensing:
* Copyright 2021 tomw
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
*
* Version Control:
* 0.1.22     2021-02-24 tomw               Optional configuration app to selectively filter out Home Assistant devices
* 0.1.23     2021-02-25 Dan Ogorchock      Switched logic from Exclude to Include to make more intuitive.  Sorted Device List.
* 0.1.32     2021-09-27 kaimyn             Add option to use HTTPS support in configuration app
* 0.1.45     2022-06-06 tomw               Added confirmation step before completing select/de-select all
* 0.1.46     2022-07-04 tomw               Advanced configuration - manual add/remove of devices; option to disable filtering; unused child cleanup
* 0.1.52     2023-02-02 tomw               UI improvements for app usability
* 0.1.53     2023-02-19 tomw               Allow multiple instances of HADB app to be installed
*/

#include HomeAssistantBridge.Logging
#include HomeAssistantBridge.Common


definition(
    name: "Home Assistant Device Bridge",
    namespace: "tomw",
    author: "tomw",
    description: "",
    category: "Convenience",
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
        section("<b>Home Assistant Device Bridge</b>")
        {
            input ("ip", "text", title: "Home Assistant IP Address", description: "HomeAssistant IP Address", required: true)
            input ("port", "text", title: "Home Assistant Port", description: "HomeAssistant Port Number", required: true, defaultValue: "8123")
            input ("token", "text", title: "Home Assistant Long-Lived Access Token", description: "HomeAssistant Access Token", required: true)
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
        if(params?.runDiscovery)
        {
            state.entityList = [:]
            def domain
            // query HA to get entity_id list
            def resp = httpGetExec(genParamsMain("states"))
            logDebug("states response = ${resp?.data}")

            if(resp?.data)
            {
                resp.data.each
                {
                    domain = it.entity_id?.tokenize(".")?.getAt(0)
                    if(["fan", "switch", "light", "binary_sensor", "sensor", "device_tracker", "cover", "lock", "climate", "input_boolean", "button"].contains(domain))
                    {
                        state.entityList.put(it.entity_id, "${it.attributes?.friendly_name} (${it.entity_id})")
                    }
                }

                state.entityList = state.entityList.sort { it.value }
            }
        }
        
        section
        {
            input name: "includeList", type: "enum", title: "Select any devices to <b>include</b> from Home Assistant Device Bridge", options: state.entityList, required: false, multiple: true, offerAll: true
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
