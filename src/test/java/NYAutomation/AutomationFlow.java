package NYAutomation;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.testng.annotations.Test;

import base.BaseClass;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

public class AutomationFlow extends BaseClass {

    private String RideOtp = "";

    @Test
    /* Creating a method for overall flow of the applications */
    private void flow() throws InterruptedException {
        for (String[] actionParameter : sanityData) {
            String testCase = actionParameter[0]; /* Variable handles the test cases (eg., user login, hamburger, ride flow and so on) */
            String state = actionParameter[2]; /* Varaible handles the state of the screen */
            String xpath = actionParameter[3]; /* variable handles the xpath of the element */
            String sendKeysValue = actionParameter[4]; /* variable handles whether the action is sendKeys or click */
            boolean isUser = "user".equals(actionParameter[5]); /* variable handles whether the actions are related to user or driver */
            checkCase(testCase, state, xpath, sendKeysValue, isUser);
            System.out.println("XPath: " + xpath + " | SendKeys Value: " + sendKeysValue);
        }

        System.out.println("User Successfully Logged In");
    }

    /* method used to code all the types of functions to be handled */
    public void checkCase(String testCase, String state, String xpath, String sendKeysValue, boolean isUser) {
        /* Creating a wait object to wait for the user or driver */
        Wait<AndroidDriver> wait = new FluentWait<>(isUser ? user : driver)
                .withTimeout(Duration.ofSeconds(50)) /* Set the timeout duration to 50 seconds */
                .pollingEvery(Duration.ofMillis(1000)) /* Set the polling interval to 1000 milliseconds */
                .ignoring(Exception.class); /* Ignore exceptions during the wait */

        if (state.equals("Fetch Otp")) {
            /* Fetching OTP digits */
        	for (int i = 1; i <= 4; i++) {
                RideOtp = RideOtp + wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath + "/../android.widget.LinearLayout/android.widget.LinearLayout[" + i + "]/android.widget.TextView"))).getText();
            }
            System.out.println("Ride Otp = " + RideOtp);

            char[] otp = RideOtp.toCharArray();
            /* Entering OTP digits */
            for (int i = 0; i < 4; i++) {
                char digit = otp[i];
                System.out.println("Otp Digit = " + digit);
                String xpath2 = "//android.widget.TextView[@text='Please ask the customer for the OTP']/../../../android.widget.LinearLayout[2]/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView[@text='" + digit + "']";
                driver.findElement(AppiumBy.xpath(xpath2)).click();
            }
            System.out.println("Ride Otp = " + RideOtp);
            
        	return;
        }
        
        /* if any specific testcases has to be performed */
    //    if (testCase != "UserLogin")
    //    {
    //        return;

    //    }

        /* Button layout locator */
        By buttonLayoutLocator = By.xpath(xpath);

        /* Performing action based on input */
        performAction(wait, buttonLayoutLocator, sendKeysValue);
    }

    /**
     * Performs the action based on the provided parameters.
     *
     * @param wait           Wait object for waiting conditions
     * @param buttonLayoutLocator Locator for the button layout
     * @param sendKeysValue  Value to be sent as keys (if not empty)
    **/
    public void performAction(Wait<AndroidDriver> wait, By buttonLayoutLocator, String sendKeysValue) {
        if (!sendKeysValue.isEmpty()) {
            /* perform send keys action to the element */
            wait.until(ExpectedConditions.visibilityOfElementLocated(buttonLayoutLocator)).sendKeys(sendKeysValue);
        } else {
            /* perform click action on the element */
            wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).click();
        }
    }
}