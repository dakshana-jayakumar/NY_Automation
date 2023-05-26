# NY_Automation

AppiumTestAutomation
====================

This project exhibits how to do automation testing for Namma Yatri mobile applications using TestNG framework, Appium, Selenium and Java

Prerequisite
=====================
1. Android SDK
2. Appium setup
3. Maven (For managing dependencies)
4. Eclipse (With TestNG plugin)

Installation
=====================
1. Install from git (using git clone)
2. Import the project to Eclipse IDE
3. Run `mvn clean install` to use maven as build tool
4. Check whether maven depencies and JRE system libaries are imported in the project

Run The Project
=====================
1. Run the appium server in terminal or appium application
2. Check whether the port running in server and in code (src/main/java/base/BaseClass) are same
3. Import APKs which has to be tested, in the apk location folder : src/main/java/NYAutomation.resources
4. Set the name of the APKs in the BaseClass file
5. Either open two emulators from Android studio or connect two mobile devices
6. Run `adb devices` on the terminal and fetch its UDID, and add it in the BaseClass file
7. Now can run the project by right click on the testng.xml file -> Run As -> click on 1 TestNG Suite
8. Then the project will get start, and run in the TestNG (testing framework) from Appium framework
9. After the automation is done, screenshots and reports are generated to test the pass and fail cases
10. Reports will be generated in a specified folder (src/main/java/NYAutomation.Reports)

