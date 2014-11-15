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
 *  2014-11-15  v0.0.2  Moved status to main page
 *                      Removed status page
 *                      Improved formatting of status page
 *                      Added low, medium, high thresholds
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
    page name:"pageStatus"
    page name:"pageConfigure"
}

// Show Status page
def pageStatus() {
    def pageProperties = [
        name:       "pageStatus",
        title:      "BatteryMonitor Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    if (settings.devices == null) {
        return pageConfigure()
    }
    
	def helpLevel0 = "Batteries with errors or no status."
    def helpLevel1 = "Batteries with low charge (less than $settings.level1)."
    def helpLevel2 = "Batteries with medium charge (between $settings.level1 and $settings.level3)."
    def helpLevel3 = "Batteries with high charge (more than $settings.level3)."
	def listLevel0 = ""
    def listLevel1 = ""
    def listLevel2 = ""
    def listLevel3 = ""

	if (settings.level1 == null) { settings.level1 = 33 }
	if (settings.level3 == null) { settings.level3 = 67 }
    
	return dynamicPage(pageProperties) {
		settings.devices.each() {
            def status = "$it.currentBattery"


			if (it.currentBattery == null) {
            	listLevel0 += "$it.displayName\n"
            } else if (it.currentBattery <  settings.level1.toInteger()) {
            	listLevel1 += "$it.currentBattery  $it.displayName\n"
            } else if (it.currentBattery >= settings.level1.toInteger() && it.currentBattery <= settings.level3.toInteger()) {
            	listLevel2 += "$it.currentBattery  $it.displayName\n"
            } else if (it.currentBattery >  settings.level3.toInteger() && it.currentBattery <= 100) {
            	listLevel3 += "$it.currentBattery  $it.displayName\n"
            } else if (it.currentBattery == "OK") {
            	listLevel3 += "$it.currentBattery  $it.displayName\n"
            } else {
				listLevel0 += "$it.currentBattery  $it.displayName\n"
			}
        }

        if (listLevel0) {
            section("Battery Error") {
                paragraph helpLevel0
                paragraph listLevel0
            }
		}
        
        if (listLevel1) {
        	section("Battery Low") {
            	paragraph helpLevel1
            	paragraph listLevel1
            }
        }

        if (listLevel2) {
            section("Battery Medium") {
                paragraph helpLevel2
                paragraph listLevel2
            }
        }

        if (listLevel3) {
            section("Battery High") {
                paragraph helpLevel3
                paragraph listLevel3
            }
        }

        section("Menu") {
            href "pageStatus", title:"Refresh", description:"Tap to refresh"
            href "pageConfigure", title:"Configure", description:"Tap to open"
        }
    }
}

// Show Configure Page
def pageConfigure() {
    def helpPage =
        "Select devices with batteries that you wish to monitor."

    def inputBattery = [
        name:           "devices",
        type:           "capability.battery",
        title:          "Which devices with batteries?",
        multiple:       true,
        required:       true
    ]

    def inputLevel1 = [
        name:           "level1",
        type:           "number",
        title:          "Low battery threshold?",
        defaultValue:   "33",
        required:       true
    ]

	def inputLevel3 = [
        name:           "level3",
        type:           "number",
        title:          "Low battery threshold?",
        defaultValue:   "67",
        required:       true
    ]

    def pageProperties = [
        name:           "pageConfigure",
        title:          "BatteryMonitor Configuration",
        nextPage:       "pageStatus",
        uninstall:      true
    ]

    return dynamicPage(pageProperties) {
        section("About") {
            paragraph helpPage
        }
        section("Devices") {
            input inputBattery
            input inputLevel1
            input inputLevel3
        }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
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
