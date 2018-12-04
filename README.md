You can use this plugin to get data from bluetooth device when it is set to SPP mode

Please note this is not an Android application.

# How to use

You can pull this repository and then in app folder, find build.gradle to run delete (if you have exported before) and export into aar file. 
Modify exportJar if you need to change file type.

# What to use
- Get connected device: getPairDevice()
- Set target device: findBT() // with device name
- Create socket, inputstream and outputstream to build connection
- Get read in data: loopThread()
- Stop connection: closeConnection() 

# Issues
Please note that this project is only for Android. And your BT device needs to be set in SPP mode(there are many awesome plugins to utilise HID mode for iOS and Android).
Report issues and I will try to figure it out.
