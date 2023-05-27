package base;

import io.appium.java_client.android.AndroidDriver;

public class Utilities {

	public static AndroidDriver user, driver;

	/* Declaring xpaths */
	public String[][] sanityData = {
			/* User Login flow */
			{"UserLogin", "Mobile Number", "Enter Mobile Number", "//android.widget.EditText[@text='Enter Mobile number']", "7777777777", "user"},
            {"UserLogin", "Mobile Number", "Continue", "//android.widget.TextView[@text='Continue']", "", "user"},
            {"UserLogin", "Otp", "OTP", "//android.widget.EditText[@text='Enter 4 digit OTP']", "7891", "user"},
            {"UserLogin", "Permission", "Grant Access", "//android.widget.TextView[@text='Grant Access']", "", "user"},
            {"UserLogin", "Permission", "Location Permission", "//android.widget.Button[@text='While using the app']", "", "user"},
            {"UserLogin", "Home", "Recenter", "//android.widget.TextView[@text='Where to?']/../../../preceding-sibling::android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.ImageView[1]", "", "user"},
			/* Driver login flow */
			{"DriverLogin", "Choose Language", "Kannada", "//android.widget.TextView[@text='Kannada']", "", "driver"},
			{"DriverLogin", "Choose Language", "Hindi", "//android.widget.TextView[@text='Hindi']", "", "driver"},
			{"DriverLogin", "Choose Language", "Tamil", "//android.widget.TextView[@text='Tamil']", "", "driver"},
			{"DriverLogin", "Choose Language", "English", "//android.widget.TextView[contains(@text,'English')]", "", "driver"},
			{"DriverLogin", "Choose Language", "Next", "//android.widget.TextView[@text='Next']", "", "driver"},
			{"DriverLogin", "Mobile Number", "Enter Mobile Number", "//android.widget.EditText[@text='Enter Mobile Number']", "9999999920", "driver"},
			{"DriverLogin", "Mobile Number", "Next", "//android.widget.TextView[@text='Next']", "", "driver"},
			{"DriverLogin", "Otp", "OTP", "//android.widget.EditText[@text='Auto Reading OTP...']", "7891", "driver"},
			{"DriverLogin", "Permission", "Location Access", "//android.widget.TextView[@text='Location Access']", "", "driver"},
			{"DriverLogin", "Permission", "Location Permisssion", "//android.widget.Button[@text='While using the app']", "", "driver"},
			{"DriverLogin", "Permission", "Draw over applications", "//android.widget.TextView[@text='Draw over applications']", "", "driver"},
			{"DriverLogin", "Permission", "Select Namma Yatri Partner", "//android.widget.TextView[@text='Namma Yatri Partner']", "", "driver"},
			{"DriverLogin", "Permission", "Enable Toggle", "//android.widget.TextView[@text='Allow display over other apps']", "", "driver"},
			{"DriverLogin", "Permission", "Overlay screen Back Icon", "//android.widget.ImageButton[@content-desc='Back']", "", "driver"},
			{"DriverLogin", "Permission", "Battery Optimization", "//android.widget.TextView[@text='Battery Optimization']", "", "driver"},
			{"DriverLogin", "Permission", "Allow Battery Optimization", "//android.widget.Button[@text='Allow']", "", "driver"},
			{"DriverLogin", "Permission", "AutoStart", "//android.widget.TextView[@text='Autostart app in background']", "", "driver"},
			{"DriverLogin", "Permission", "Allow AutoStart", "//android.widget.TextView[@text='Allow Access']", "", "driver"},
			{"DriverLogin", "Home", "Profile Icon", "//android.widget.TextView[@text='Click here to access your account']/../../android.widget.ImageView[1]", "", "driver"},
			{"DriverLogin", "Home", "Navbar Home", "//android.widget.TextView[@text='Home']", "", "driver"},
            /* Ridesearch */
			{"RideFlow", "Home", "Fav tag Home", "//android.widget.TextView[@text='Home']", "", "user"},
			/* Confirm pickup */
			{"RideFlow", "Location Confirmation", "Confirm Location", "//android.widget.TextView[@text='Confirm Location']", "", "user"},
			/* Estimate */
			{"RideFlow", "Estimate", "Estimates", "//android.widget.TextView[@text='Request Ride']", "", "user"},
			/* Ride Request Popup */
			{"RideFlow", "Ride Request Popup", "Accept Offer", "//android.widget.Button[@text='Accept Offer']", "", "driver"},
			/* Start ride */
			{"RideFlow", "Ride Action", "Start Ride", "//android.widget.TextView[@text='Start Ride']", "", "driver"},
			/* Fetch OTP on start ride */
			{"RideFlow", "Start Ride", "Fetch Otp", "//android.widget.TextView[@text='OTP:']", "", "user"},
			{"RideFlow", "Start Ride", "Otp Enter", "//android.widget.TextView[@text='0']/../../following-sibling::android.widget.LinearLayout/android.widget.LinearLayout/android.widget.ImageView", "", "driver"},
			/* End ride */
			{"RideFlow", "End Ride", "End ride", "//android.widget.TextView[@text='End Ride']", "", "driver"},
			{"RideFlow", "End Ride", "End ride", "//android.widget.TextView[@text='Go Back']/../following-sibling::android.widget.LinearLayout/android.widget.TextView", "", "driver"},
			{"RideFlow", "Ride Completed", "Cash Collected", "//android.widget.TextView[@text='Cash Collected']", "", "driver"},
			/* Rate your driver */
			{"RideFlow", "Ride Completed", "Rate Driver", "//android.widget.TextView[@text='Rate Your Driver']", "", "user"},
			{"RideFlow", "Ride Completed", "Write Feedback", "//android.widget.TextView[@text='Ride Completed']/../following-sibling::android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[5]/android.widget.ImageView", "", "user"},
			{"RideFlow", "Ride Completed", "Submit Feedback", "//android.widget.TextView[@text='Submit Feedback']", "", "user"},
            /* User Hamburger */
            {"HamburgerFlow", "Home", "Hamburger Icon", "//android.widget.TextView[@text='Pick Up Location']/../../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            /* My Profile */
            {"HamburgerFlow", "Hamburger Section", "My Profile", "//android.widget.TextView[@text='My Rides']/../../../preceding-sibling::android.widget.LinearLayout", "", "user"},
            {"HamburgerFlow", "MyProfile", "Back Icon", "//android.widget.TextView[@text='Personal Details']/../android.widget.LinearLayout", "", "user"},
            /* My rides */
            {"HamburgerFlow", "Hamburger Section", "My Rides", "//android.widget.TextView[@text='My Rides']", "", "user"},
            {"HamburgerFlow", "My Rides", "View Details", "//android.widget.TextView[@text='View Details']", "", "user"},
            {"HamburgerFlow", "Ride Details", "View Invoice", "//android.widget.TextView[@text='View Invoice']", "", "user"},
            {"HamburgerFlow", "Invoice", "Back Icon", "//android.widget.TextView[@text='Invoice']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            {"HamburgerFlow", "Ride Details", "Back Icon", "//android.widget.TextView[@text='Ride Details']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            {"HamburgerFlow", "My Rides", "repeat ride", "//android.widget.TextView[@text='Repeat Ride']", "", "user"},
            {"HamburgerFlow", "Estimates", "Back Icon", "//android.widget.TextView[@text='Request Auto Ride']/../../../preceding-sibling::android.widget.LinearLayout[3]/android.widget.LinearLayout", "", "user"},
            {"HamburgerFlow", "Search Location Modal", "Back Icon", "//android.widget.TextView[@text='Home']/../../../../../../preceding-sibling::android.widget.LinearLayout[1]", "", "user"},
            /* Favourites */
			{"HamburgerFlow", "Home", "Hamburger Icon", "//android.widget.TextView[@text='Pick Up Location']/../../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            /* Emergency contacts */
            {"HamburgerFlow", "Hamburger Section", "Emergency Contacts", "//android.widget.TextView[@text='Emergency Contacts']", "", "user"},
            {"HamburgerFlow", "Emergency Contacts", "Back Icon", "//android.widget.TextView[@text='Emergency Contacts']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            /* Help and support */
            {"HamburgerFlow", "Hamburger Section", "Help Support", "//android.widget.TextView[@text='Help and Support']", "", "user"},
            {"HamburgerFlow", "Help and Support", "View All Rides", "//android.widget.TextView[@text='View All Rides']", "", "user"},
            {"HamburgerFlow", "My Rides", "Back Icon", "//android.widget.TextView[@text='My Rides']/../android.widget.LinearLayout", "", "user"},
            {"HamburgerFlow", "Help and Support", "Report an Issue", "//android.widget.TextView[@text='Report an issue with this Trip']", "", "user"},
            {"HamburgerFlow", "Ride Details", "Back Icon", "//android.widget.TextView[@text='Ride Details']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            {"HamburgerFlow", "Help and Support", "Write Other Issues", "//android.widget.TextView[@text='For other issues, write to us']", "", "user"},
            {"HamburgerFlow", "Write To Us", "Back Icon", "//android.widget.TextView[@text='Write to Us']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            {"HamburgerFlow", "Help and Support", "Contact Support", "//android.widget.TextView[@text='Contact Support']", "", "user"},
            {"HamburgerFlow", "Contact Support", "Go Back", "//android.widget.TextView[@text='Go Back']", "", "user"},
            {"HamburgerFlow", "Help and Support", "Back Icon", "//android.widget.TextView[@text='Help and Support']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            /* Language update */
            {"HamburgerFlow", "Update Language", "Language", "//android.widget.TextView[@text='Language']", "", "user"},
            {"HamburgerFlow", "Update Language", "Back Icon", "//android.widget.TextView[@text='Language']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            /* Share app */
            {"HamburgerFlow", "Share App", "Share App", "//android.widget.TextView[@text='Share App']", "", "user"},
            {"HamburgerFlow", "Share App", "Copy Link", "//android.widget.Button[@text='Copy']", "", "user"},
            /* Stats dashboard */
            {"HamburgerFlow", "Stats", "stats dashboard", "//android.widget.TextView[@text='Live Stats Dashboard']", "", "user"},
            {"HamburgerFlow", "Stats Dashboard", "Back Press", "", "", "user"},
            /* About */
            {"HamburgerFlow", "About", "About", "//android.widget.TextView[@text='About']", "", "user"},
            {"HamburgerFlow", "About", "Back Icon", "//android.widget.TextView[@text='About']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            /* Logout */
            {"HamburgerFlow", "Logout", "Logout", "//android.widget.TextView[@text='Logout']", "", "user"},
            {"HamburgerFlow", "Logout Section", "Back Press", "", "", "user"},
            /* Driver Hamburger */
            {"DriverHamburger", "Home", "Navbar Home", "//android.widget.TextView[@text='Home']/../../android.widget.LinearLayout[1]", "", "driver"},
            /* Add alternate number*/
            {"Add Alternate Number", "Home", "Add Alternate Number", "//android.widget.TextView[@text='Add Alternate Number']/../../android.widget.LinearLayout", "", "driver"},
            {"Add Alternate Number", "Personal Details", "Alternate Number Popup", "//android.widget.TextView[@text='Enter Alternate Mobile Number']/../../android.widget.LinearLayout/android.widget.ImageView", "", "driver"},
            {"Add Alternate Number", "Personal Details", "Back Icon", "//android.widget.TextView[@text='Personal Details']/../../android.widget.LinearLayout/android.widget.ImageView", "", "driver"},
            /* Driver status */
            {"Driver status", "Home", "Offline", "//android.widget.TextView[@text='Offline']", "", "driver"},
            {"Driver status", "Home", "Offline Popup", "//android.widget.TextView[@text='Go Offline']", "", "driver"},
            {"Driver status", "Home", "Offline to Online", "//android.widget.TextView[@text='GO!']", "", "driver"},
            {"Driver status", "Home", "Silent", "//android.widget.TextView[@text='Silent']", "", "driver"},
            {"Driver status", "Home", "Silent to Online", "//android.widget.TextView[@text='Online']/../../android.widget.LinearLayout", "", "driver"},
            /* Driver Profile */
            {"DriverProfile", "Home", "Profile Icon", "//android.widget.TextView[@text='Offline']/../../../../android.widget.LinearLayout[1]", "", "driver"},
            {"DriverProfile", "Driver Profile", "Personal Details", "//android.widget.TextView[@text='Personal Details']/../../../android.widget.LinearLayout[1]", "", "driver"},
            {"DriverProfile", "Personal Details", "Back Icon", "//android.widget.TextView[@text='Personal Details']/../../android.widget.LinearLayout/android.widget.ImageView", "", "driver"},
            /* Vehicle Details */
            {"DriverProfile", "Driver Profile", "Vehicle Details", "//android.widget.TextView[@text='Personal Details']/../../../android.widget.LinearLayout[2]", "", "driver"},
            {"DriverProfile", "Vehicle Details", "Back Icon", "//android.widget.TextView[@text='Vehicle Details']/../../android.widget.LinearLayout/android.widget.ImageView", "", "driver"},
            /* App Info */
            {"DriverProfile", "Driver Profile", "App Info", "//android.widget.TextView[@text='Personal Details']/../../../android.widget.LinearLayout[3]", "", "driver"},
            {"DriverProfile", "Vehicle Details", "Back Icon", "//android.widget.TextView[@text='App info']/../../android.view.ViewGroup/android.widget.ImageButton", "", "driver"},
            /* Languages */
            {"DriverProfile", "Driver Profile", "Languages", "//android.widget.TextView[@text='Personal Details']/../../../android.widget.LinearLayout[4]", "", "driver"},
            {"DriverProfile", "Languages", "Back Icon", "//android.widget.TextView[@text='Select Language']/../../android.widget.LinearLayout/android.widget.ImageView", "", "driver"},
            /* Help and FAQs */
            {"DriverProfile", "Driver Profile", "Help and FAQs", "//android.widget.TextView[@text='Personal Details']/../../../android.widget.LinearLayout[5]", "", "driver"},
            {"DriverProfile", "Help and Support", "Lost and Found", "//android.widget.TextView[@text='Lost And Found']/../../android.widget.LinearLayout[1]", "", "driver"},
            {"DriverProfile", "Lost and Found", "Back Icon", "//android.widget.TextView[@text='Lost And Found']/../../android.widget.LinearLayout[1]/android.widget.ImageView", "", "driver"},
            
            {"DriverProfile", "Help and Support", "Ride Related", "//android.widget.TextView[@text='Lost And Found']/../../android.widget.LinearLayout[2]", "", "driver"},
            {"DriverProfile", "Ride Related", "Back Icon", "//android.widget.TextView[@text='Ride Related']/../../android.widget.LinearLayout[1]/android.widget.ImageView", "", "driver"},
            
            {"DriverProfile", "Help and Support", "App Related", "//android.widget.TextView[@text='App Related']/../../android.widget.LinearLayout[1]", "", "driver"},
            {"DriverProfile", "Help and FAQs", "Back Icon", "//android.widget.TextView[@text='App Related Issue']/../../android.widget.LinearLayout[1]/android.widget.ImageView", "", "driver"},
            
            {"DriverProfile", "Help and Support", "Fare Related", "//android.widget.TextView[@text='App Related']/../../android.widget.LinearLayout[2]", "", "driver"},
            {"DriverProfile", "Help and FAQs", "Back Icon", "//android.widget.TextView[@text='Fare Related Issue']/../../android.widget.LinearLayout[1]/android.widget.ImageView", "", "driver"},
            
            {"DriverProfile", "Help and Support", "Back Icon", "//android.widget.TextView[@text='HELP_AND_SUPPORT']/../../android.widget.LinearLayout[1]/android.widget.ImageView", "", "driver"},
            /* Live Stats Dashboard */
            {"DriverProfile", "Driver Profile", "Live Stats Dashboard", "//android.widget.TextView[@text='Personal Details']/../../../android.widget.LinearLayout[6]", "", "driver"},
            {"DriverProfile", "Stats Dashboards", "Back Press", "", "", "driver"},
            /* About */
            {"DriverProfile", "Driver Profile", "About", "//android.widget.TextView[@text='Personal Details']/../../../android.widget.LinearLayout[7]", "", "driver"},
            {"DriverProfile", "About", "Back Icon", "//android.widget.TextView[@text='About']/../../android.widget.LinearLayout[1]/android.widget.ImageView", "", "driver"},
            /* Logout */
            {"DriverProfile", "Driver Profile", "Logout", "//android.widget.TextView[@text='Personal Details']/../../../android.widget.LinearLayout[8]", "", "driver"},
            {"DriverProfile", "Driver Logout", "Go Back", "//android.widget.TextView[@text='Go Back']", "", "driver"},
            /* Trips */
            {"DriverHamburger", "Driver Profile", "Navbar Trips", "//android.widget.TextView[@text='Home']/../../android.widget.LinearLayout[2]", "", "driver"},
            {"DriverHamburger", "Trips", "Cancelled", "//android.widget.TextView[@text='Cancelled']", "", "driver"},
            {"DriverHamburger", "Trips", "Completed", "//android.widget.TextView[@text='Completed']", "", "driver"},
            /* Contest */
            {"DriverHamburger", "Trips", "Navbar Contest", "//android.widget.TextView[@text='Home']/../../android.widget.LinearLayout[3]", "", "driver"},
            /* Alerts */
            {"DriverHamburger", "Contest", "Navbar Alerts", "//android.widget.TextView[@text='Home']/../../android.widget.LinearLayout[4]", "", "driver"},
            {"DriverHamburger", "Alerts", "Back Icon", "//android.widget.TextView[@text='All Alerts']/../../android.widget.LinearLayout/android.widget.ImageView", "", "driver"},
            {"DriverHamburger", "Referral", "Navbar Home", "//android.widget.TextView[@text='Home']/../../android.widget.LinearLayout[1]", "", "driver"},

	};
	
	public String[][] regressionData = {
			/* User Login flow */
			{"UserLogin", "Mobile Number", "Enter Mobile Number", "//android.widget.EditText[@text='Enter Mobile number']", "7777777777", "user"},
            {"UserLogin", "Mobile Number", "Continue", "//android.widget.TextView[@text='Continue']", "", "user"},
            {"UserLogin", "Otp", "OTP", "//android.widget.EditText[@text='Enter 4 digit OTP']", "7891", "user"},
            {"UserLogin", "Permission", "Grant Access", "//android.widget.TextView[@text='Grant Access']", "", "user"},
            {"UserLogin", "Permission", "Location Permission", "//android.widget.Button[@text='While using the app']", "", "user"},
            {"UserLogin", "Home", "Recenter", "//android.widget.TextView[@text='Where to?']/../../../preceding-sibling::android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.ImageView[1]", "", "user"},
			/* Driver login flow */
			{"DriverLogin", "Choose Language", "Kannada", "//android.widget.TextView[@text='Kannada']", "", "driver"},
			{"DriverLogin", "Choose Language", "Hindi", "//android.widget.TextView[@text='Hindi']", "", "driver"},
			{"DriverLogin", "Choose Language", "Tamil", "//android.widget.TextView[@text='Tamil']", "", "driver"},
			{"DriverLogin", "Choose Language", "English", "//android.widget.TextView[contains(@text,'English')]", "", "driver"},
			{"DriverLogin", "Choose Language", "Next", "//android.widget.TextView[@text='Next']", "", "driver"},
			{"DriverLogin", "Mobile Number", "Enter Mobile Number", "//android.widget.EditText[@text='Enter Mobile Number']", "9999999920", "driver"},
			{"DriverLogin", "Mobile Number", "Next", "//android.widget.TextView[@text='Next']", "", "driver"},
			{"DriverLogin", "Otp", "OTP", "//android.widget.EditText[@text='Auto Reading OTP...']", "7891", "driver"},
			{"DriverLogin", "Permission", "Location Access", "//android.widget.TextView[@text='Location Access']", "", "driver"},
			{"DriverLogin", "Permission", "Location Permisssion", "//android.widget.Button[@text='While using the app']", "", "driver"},
			{"DriverLogin", "Permission", "Draw over applications", "//android.widget.TextView[@text='Draw over applications']", "", "driver"},
			{"DriverLogin", "Permission", "Select Namma Yatri Partner", "//android.widget.TextView[@text='Namma Yatri Partner']", "", "driver"},
			{"DriverLogin", "Permission", "Enable Toggle", "//android.widget.TextView[@text='Allow display over other apps']", "", "driver"},
			{"DriverLogin", "Permission", "Overlay screen Back Icon", "//android.widget.ImageButton[@content-desc='Back']", "", "driver"},
			{"DriverLogin", "Permission", "Battery Optimization", "//android.widget.TextView[@text='Battery Optimization']", "", "driver"},
			{"DriverLogin", "Permission", "Allow Battery Optimization", "//android.widget.Button[@text='Allow']", "", "driver"},
			{"DriverLogin", "Permission", "AutoStart", "//android.widget.TextView[@text='Autostart app in background']", "", "driver"},
			{"DriverLogin", "Permission", "Allow AutoStart", "//android.widget.TextView[@text='Allow Access']", "", "driver"},
			{"DriverLogin", "Home", "Profile Icon", "//android.widget.TextView[@text='Click here to access your account']/../../android.widget.ImageView[1]", "", "driver"},
			{"DriverLogin", "Home", "Navbar Home", "//android.widget.TextView[@text='Home']", "", "driver"},
            /* User Hamburger */
            {"Hamburger", "Home", "Hamburger Icon", "//android.widget.TextView[@text='Pick Up Location']/../../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            /* Help and support */
            {"HamburgerFlow", "Hamburger Section", "Help Support", "//android.widget.TextView[@text='Help and Support']", "", "user"},
            {"HamburgerFlow", "Help and Support", "View All Rides", "//android.widget.TextView[@text='View All Rides']", "", "user"},
            {"HamburgerFlow", "My Rides", "Back Icon", "//android.widget.TextView[@text='My Rides']/../android.widget.LinearLayout", "", "user"},
            {"HamburgerFlow", "Help and Support", "Report an Issue", "//android.widget.TextView[@text='Report an issue with this Trip']", "", "user"},
			// few lines are needed to be added
            {"HamburgerFlow", "Help and Support", "View Invoice", "//android.widget.TextView[@text='View Invoice']", "", "user"},
            {"HamburgerFlow", "Invoice", "Download Pdf", "//android.widget.TextView[@text='Download PDF']", "", "user"},
            {"HamburgerFlow", "Download permission", "Allow Permission", "//android.widget.Button[@text='Allow']", "", "user"},
            {"HamburgerFlow", "Invoice", "Back Icon", "//android.widget.TextView[@text='Invoice']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            {"HamburgerFlow", "Ride Details", "Lost Something", "//android.widget.TextView[@text='Lost Something?']", "", "user"},
            {"HamburgerFlow", "Lost Something", "Cancel", "//android.widget.TextView[@text='Cancel']", "", "user"},
            // {"HamburgerFlow", "Ride Details", "Report issue", "//android.widget.TextView[@text='Report an issue']", "", "user"},
            // {"HamburgerFlow", "Ride Details", "Report issue", "//android.widget.TextView[@text='Report an issue']", "", "user"},
            {"HamburgerFlow", "Ride Details", "Describe Issue", "//android.widget.EditText[@text='You can describe the issue you faced here']", "", "user"},
            {"HamburgerFlow", "Ride Details", "Enter Issue", "//android.widget.EditText[@text='You can describe the issue you faced here']", "Good", "user"},
            {"HamburgerFlow", "Ride Details", "Submit Issue", "//android.widget.TextView[@text='Submit']", "", "user"},
            {"HamburgerFlow", "Issue Responded", "Home", "//android.widget.TextView[@text='Go Home']", "", "user"},
            {"HamburgerFlow", "Home", "Hamburger", "//android.widget.TextView[@text='Pick Up Location']/../../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            {"HamburgerFlow", "Hamburger Section", "Help and Support", "//android.widget.TextView[@text='Help and Support']", "", "user"},
            {"HamburgerFlow", "Help and Support", "Write Issue", "//android.widget.TextView[@text='For other issues, write to us']", "", "user"},
            {"HamburgerFlow", "Write to Us", "Click Subject", "//android.widget.EditText[@text='I am not receiving any rides']", "", "user"},
            {"HamburgerFlow", "Write to Us", "Enter Subject", "//android.widget.EditText[@text='I am not receiving any rides']", "Checking", "user"},
            {"HamburgerFlow", "Write to Us", "Click Email", "//android.widget.EditText[@text='example@xyz.com']", "", "user"},
            {"HamburgerFlow", "Write to Us", "Enter Email", "//android.widget.EditText[@text='example@xyz.com']", "hi@gmail.com", "user"},
            {"HamburgerFlow", "Write to Us", "Click issue", "//android.widget.EditText[@text='You can describe the issue you faced here']", "", "user"},
            {"HamburgerFlow", "Write to Us", "Enter Issue", "//android.widget.EditText[@text='You can describe the issue you faced here']", "Good", "user"},
            {"HamburgerFlow", "Write to Us", "Submit Issue", "//android.widget.TextView[@text='Submit']", "", "user"},
            {"HamburgerFlow", "Issue Responded", "Home", "//android.widget.TextView[@text='Go To Home']", "", "user"},
            {"HamburgerFlow", "Home", "Hamburger", "//android.widget.TextView[@text='Pick Up Location']/../../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            {"HamburgerFlow", "Hamburger Section", "Help and Support", "//android.widget.TextView[@text='Help and Support']", "", "user"},
            {"HamburgerFlow", "Help and Support", "Contact Support", "//android.widget.TextView[@text='Contact Support']", "", "user"},
            {"HamburgerFlow", "Help and Support", "Go Back", "//android.widget.TextView[@text='Go Back']", "", "user"},
            {"HamburgerFlow", "Help and Support", "Back Icon", "//android.widget.TextView[@text='Help and Support']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
			/* Language update */
//			{"HamburgerFlow", "Home", "Hamburger", "//android.widget.TextView[@text='Pick Up Location']/../../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
			{"HamburgerFlow", "Hamburger Section", "Language", "//android.widget.TextView[@text='Language']", "", "user"},
            {"HamburgerFlow", "Language", "Select Kannada", "//android.widget.TextView[@text='Kannada']", "", "user"},
            {"HamburgerFlow", "Language", "Update to Kannada", "//android.widget.TextView[@text='Update']", "", "user"},
            {"HamburgerFlow", "Hamburger Section", "Language in Kannada", "//android.widget.TextView[@text='ಭಾಷೆಗಳು']", "", "user"},
            {"HamburgerFlow", "Language", "Select Hindi", "//android.widget.TextView[@text='Hindi']", "", "user"},
            {"HamburgerFlow", "Language", "Update to Hindi", "//android.widget.TextView[@text='ನವೀಕರಿಸು']", "", "user"},
            {"HamburgerFlow", "Hamburger Section", "Language in Hindi", "//android.widget.TextView[@text='भाषा']", "", "user"},
            {"HamburgerFlow", "Language", "Select English", "//android.widget.TextView[@text='English']", "", "user"},
            {"HamburgerFlow", "Language", "Update to English", "//android.widget.TextView[@text='अपडेट']", "", "user"},
            /* About */
            // {"HamburgerFlow", "Hamburger Section", "About", "//android.widget.TextView[@text='About']", "", "user"},
			// {"HamburgerFlow", "About", "T&C", "//android.widget.TextView[@text='T&C']", "", "user"},
			// {"HamburgerFlow", "About Popup", "Choose Account", "//android.widget.TextView[@text='Select an account']/../../../android.widget.LinearLayout/../android.widget.FrameLayout/android.widget.ListView/android.widget.CheckedTextView[2]", "", "user"},
			// {"HamburgerFlow", "About Popup", "Click Ok", "//android.widget.Button[@text='OK']", "", "user"},
			// {"HamburgerFlow", "T&C Document", "Cross Icon", "//android.widget.TextView[@text='NAMMA_YATRI_T&C.docx']/../../android.widget.ImageButton", "", "user"},
			// {"HamburgerFlow", "About", "Privacy Policy", "//android.widget.TextView[@text='Privacy Policy']", "", "user"},
			// {"HamburgerFlow", "About Popup", "Choose Account", "//android.widget.TextView[@text='Select an account']/../../../android.widget.LinearLayout/../android.widget.FrameLayout/android.widget.ListView/android.widget.CheckedTextView[2]", "", "user"},
			// {"HamburgerFlow", "About Popup", "Click Ok", "//android.widget.Button[@text='OK']", "", "user"},
			// {"HamburgerFlow", "Privacy Policy Document", "Cross Icon", "//android.widget.TextView[@text='PRIVACY_POLICY_NAMMA_YATRI.docx']/../../android.widget.ImageButton", "", "user"},
			// {"HamburgerFlow", "About", "Back Icon", "//android.widget.TextView[@text='About']/../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            /* Logout */
            {"HamburgerFlow", "Hamburger Section", "Logout", "//android.widget.TextView[@text='Logout']", "", "user"},
            {"HamburgerFlow", "Logout Popup", "Go Back", "//android.widget.TextView[@text='Go Back']", "", "user"},
            {"HamburgerFlow", "Search Location Modal", "Back Icon", "//android.widget.TextView[@text='Set location on map']/../../../preceding-sibling::android.widget.LinearLayout[1]/android.widget.LinearLayout[1]", "", "user"},
            {"HamburgerFlow", "Home", "Hamburger", "//android.widget.TextView[@text='Pick Up Location']/../../android.widget.LinearLayout/android.widget.ImageView", "", "user"},
            {"HamburgerFlow", "Hamburger Section", "Logout", "//android.widget.TextView[@text='Logout']", "", "user"},
            {"HamburgerFlow", "Logout Popup", "Click Logout", "//android.widget.TextView[@text='Go Back']/../../android.widget.LinearLayout[2]/android.widget.TextView", "", "user"}

	};

}