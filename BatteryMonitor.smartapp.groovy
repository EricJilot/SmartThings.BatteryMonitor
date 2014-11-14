/**
 *  BatteryMonitor SmartApp for SmartThings
 *
 *  Copyright (c) 2014 Brandon Gordon (https://github.com/notoriousbdg)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Overview
 *  ----------------
 *  This SmartApp helps you monitor the status of your SmartThings devices with batteries.
 *
 *  Install Steps
 *  ----------------
 *  1. Create new SmartApps at https://graph.api.smartthings.com/ide/apps using the SmartApps at https://github.com/notoriousbdg/SmartThings.BatteryMonitor.
 *  2. Install the newly created SmartApp in the SmartThings mobile application.
 *  3. Follow the prompts to configure.
 *  4. Tap Status to view battery level for all devices.
 *
 *  Revision History
 *  ----------------
 *  2014-11-14  v0.0.1  Initial release
 *
 *  The latest version of this file can be found at:
 *    https://github.com/notoriousbdg/SmartThings.BatteryMonitor
 *
 */

definition(
    name: "BatteryMonitor",
    namespace: "notoriousbdg",
    author: "Brandon Gordon",
    description: "SmartApp to monitor battery levels.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page name:"pageMain"
    page name:"pageConfigure"
    page name:"pageStatus"
}

// Show Main page
def pageMain() {
    def pageProperties = [
        name:       "pageMain",
        title:      "BatteryMonitor",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    if (settings.devices == null) {
        return pageConfigure()
    }
    
    return dynamicPage(pageProperties) {
        section("Main Menu") {
            href "pageConfigure", title:"Configure", description:"Tap to open"
            href "pageStatus", title:"Status", description:"Tap to open"
        }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

// Show Configure Page
def pageConfigure() {
    def helpPage =
        "Select devices with batteries that you wish to monitor."

    def inputBattery = [
        name:       "devices",
        type:       "capability.battery",
        title:      "Which devices with batteries?",
        multiple:   true,
        required:   true
    ]

    def pageProperties = [
        name:       "pageConfigure",
        title:      "Configure",
        nextPage:   "pageMain",
        uninstall:  true
    ]

    return dynamicPage(pageProperties) {
        section("About") {
            paragraph helpPage
        }
        section("Devices") {
            input inputBattery
        }
    }
}

// Show Battery Status Page
def pageStatus() {
    def pageProperties = [
        name:       "pageStatus",
        title:      "Battery Status",
        nextPage:   "pageMain",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        settings.devices.each() {
            def status = "$it.currentBattery"

            section(it.displayName) {
                paragraph status
            }
        }
        section("Options") {
            href "pageStatus", title:"Refresh", description:"Tap to refresh"
            href "pageConfigure", title:"Configure", description:"Tap to open"
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(devices, "battery", batteryHandler)
}

def batteryHandler(evt) {
    for (device in settings.devices) {
        log.debug "$device.name battery level is $device.currentBattery"
    }
}
