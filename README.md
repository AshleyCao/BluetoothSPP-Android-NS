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

# Update
Java future get method will block until the result is available. It couldn't be mannully completed. Future.get() menthod can set timeout in it. Feel free to add timeout parameter in future if it is suitable for your case. 

CompletableFuture is introduced. It could be manually completed. For example, you post new customers info to api and need to close connection.

# Issues
Please note that this project is only for Android. And your BT device needs to be set in SPP mode(there are many awesome plugins to utilise HID mode for iOS and Android).
Report issues and I will try to figure it out.
