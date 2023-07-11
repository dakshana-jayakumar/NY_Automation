package base;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.ReadableByteChannel;

import com.slack.api.Slack;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.jetty.SlackAppServer;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.model.event.FileSharedEvent;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.channels.Channels;

import com.slack.api.model.Message;



public class SlackBotIntegration {

    public static final String BOT_TOKEN = "";
    public static final String USER_TOKEN = "";
    public static String SLACK_SIGNING_SECRET = "";
    public static String CUSTOMER_FILE = "user.apk";
    public static String DRIVER_FILE = "driver.apk";
    public static String PATH = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
                                + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator + "BotApk"  + File.separator;
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static Boolean happen = true;
     private static String webHooksUrl = "";
	 private static String slackChannel = "";


    public static void main(String[] args) throws Exception {
	    //   boolean startExecution = false;
        // Create a new instance of the Slack Bolt App
        AppConfig appConfig = AppConfig.builder()
                .singleTeamBotToken((BOT_TOKEN))
                .userScope(USER_TOKEN)
                .signingSecret((SLACK_SIGNING_SECRET))
                .build();
        System.out.println("Build Completed");
        App app = new App(appConfig);
        sendMessageToSlack("Hey People 😃! You can upload the APK in the `Ny Automation` slack bot app, by adding it using add apps below. You will receive your reports here 🙌🥳");

    
        app.event(FileSharedEvent.class, (payload, ctx) -> {
              if(happen){
            	System.out.println("happen check 1 : " + happen);
                happen = false;
                System.out.println("⚡️Thankyou for uploading the apk!");
                String userId = payload.getEvent().getUserId();
                String fileId = payload.getEvent().getFileId();
                System.out.println("Payload :: " + payload);
                System.out.println("fileId :: " + fileId);
                String fileInfoUrl = "" + fileId;
                System.out.println("FileUrl :: " + fileInfoUrl);
                Boolean isValidApk;
                
                isValidApk = downloadFile(fileInfoUrl, PATH);

                int count = 0;
                String message;
                if(count < 1){
                    if(isValidApk) { 
                        message = "<@" + userId + ">, ️Thankyou 🙏 for uploading";
                        ctx.say(message);
                        String waitMsg = "<@" + userId + ">, ️Uploading the APK into the machine ⚙️🦾";
                        ctx.say(waitMsg);
                        String testStart = "<@" + userId + ">, Start automation using `/start_automation` command 🤖";
                        ctx.say(testStart);
                    }

                    else {
                        message = "Hey <@" + userId + ">, Only APK files are allowed for automation. Please upload an APK file to proceed. ⚙️🦾";
                        ctx.say(message);
                    }
                }
             }
             happen = true;
                 System.out.println("happen check 2 : " + happen);
            return ctx.ack();  
        });


        app.command("/start_automation", (req, ctx) -> {
            String instructionMessage = "To start automation, please choose one of the 👇 commands:\n"
                    + "`/yes` - Delete existing report files and start automation.\n"
                    + "`/no` - Keep existing report files and start automation.";
            ctx.say(instructionMessage);
            return ctx.ack();
        });
        
        app.command("/yes", (req, ctx) -> {
            System.out.println("/yes getting executed");
            
            String startMessage = String.format("Automation testing started 👍 for %s 's 😃 APK", req.getPayload().getUserName());
            ctx.say(startMessage);
            
            String alterMessage = String.format("Hey Users 😃, Don't 🚫 perform any actions now. Automation is on the progress 🚧 for %s 's APKs", req.getPayload().getUserName());
            ctx.say(alterMessage);

            SlackBotIntegration slackBotIntegration = new SlackBotIntegration();
            try {
                slackBotIntegration.executeYesMavenCommand();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            String thankingMsg = String.format("Hey %s 😃, Automation is completed 🙌🥳. Visit `automation-reports` channel for the reports", req.getPayload().getUserName());
            ctx.say(thankingMsg);
            String endMessage = String.format("Hey Users 😃, Upload APKs to perfom Automation 🤖⚙️🦾 ", req.getPayload());
            ctx.say(endMessage);

            return ctx.ack();
        });

        app.command("/no", (req, ctx) -> {
            System.out.println("/no getting executed");
            
            String startMessage = String.format("Automation testing started 👍 for %s 's 😃 APK", req.getPayload().getUserName());
            ctx.say(startMessage);
            
            String alterMessage = String.format("Hey Users 😃, Don't perform any actions now. Automation is on the progress for %s 's APKs", req.getPayload().getUserName());
            ctx.say(alterMessage);

            // Execute the Maven command asynchronously in a background thread
            SlackBotIntegration slackBotIntegration = new SlackBotIntegration();
            try {
                slackBotIntegration.executeNoMavenCommand();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            String thankingMsg = String.format("Hey %s 😃, Automation is completed 🙌🥳. Visit `automation-reports` channel for the reports", req.getPayload().getUserName());
            ctx.say(thankingMsg);
            String endMessage = String.format("Hey Users 😃, Upload APKs to perfom Automation 🤖⚙️🦾", req.getPayload().getUserName());
            ctx.say(endMessage);

            return ctx.ack();
        });

        app.command("/remove_messages", (req, ctx) -> {
            System.out.println("check1");
            String channelId = req.getPayload().getChannelId();
            boolean hasMore = true;
            final String[] latestTs = {null};

            while (hasMore) {
                System.out.println("check2");
                ConversationsHistoryResponse historyResponse = ctx.client().conversationsHistory(r -> r
                        .channel(channelId)
                        .latest(latestTs[0])
                        .limit(1000)
                );

                System.out.println("check3");
                List<Message> messages = historyResponse.getMessages();
                hasMore = historyResponse.isHasMore();
                System.out.println("check4");

                for (Message message : messages) {
                    System.out.println("check5");
                    if (message.getUser().equals(ctx.getBotId())) {
                        System.out.println("check6");
                        ctx.client().chatDelete(r -> r
                                .channel(channelId)
                                .ts(message.getTs())
                        );
                        System.out.println("check7");

                        // Introduce a delay between delete requests to comply with the rate limiting restriction
                        try {
                            System.out.println("check8");
                            Thread.sleep(1200); // Sleep for 1.2 seconds
                        } catch (InterruptedException e) {
                            System.out.println("check8");
                            e.printStackTrace();
                        }
                        System.out.println("check9");
                    }
                    System.out.println("check10");
                    latestTs[0] = message.getTs();
                }
                System.out.println("check11");
            }
            System.out.println("check12");
            return ctx.ack();
        });








        // Create the Slack App server and start it
        int port = 8000;
        SlackAppServer slackAppServer = new SlackAppServer(app, port);
        System.out.println("Slack App Server started on port :: " + port);
        slackAppServer.start();
    }
    
    private static Boolean downloadFile(String fileUrl, String destinationPath) throws IOException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(fileUrl);
            httpGet.setHeader("Authorization", "Bearer " + BOT_TOKEN);


            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            System.out.println("\nresponseBody: " + responseBody);
            // Parse the JSON response
            JSONObject responseJson = new JSONObject(responseBody);
            System.out.println("\nresponseJson: " + responseJson);

            // Access the file object
            JSONObject fileJson = responseJson.getJSONObject("file");
            System.out.println("\nfileJson: " + fileJson);


            // Get the value of url_private_download
            String urlPrivateDownload = fileJson.getString("url_private");

            System.out.println("\nurlPrivateDownload: " + urlPrivateDownload);
            String filetype = fileJson.getString("filetype");
            System.out.println("\nfiletype: " + filetype);
            String fileName = fileJson.getString("title");
            System.out.println("\fileName: " + fileName);

            // Print the value
            
            System.out.println("\nfiletype: " + filetype);
            if(filetype.contains("apk")){
                System.out.println("url_private: " + urlPrivateDownload);
                URL FileDownloadUrl = new URL(urlPrivateDownload);
                if(fileName.contains("nyp")){
                    destinationPath = destinationPath + DRIVER_FILE;
                    // DRIVER_FILE = fileName;
                    System.out.println("DRIVER_FILE :: " + DRIVER_FILE);
                }else{
                    destinationPath = destinationPath + CUSTOMER_FILE;
                    // CUSTOMER_FILE = fileName;
                    System.out.println("CUSTOMER_FILE :: " + CUSTOMER_FILE);
                }
                    URLConnection connection = FileDownloadUrl.openConnection();
                    connection.setRequestProperty("Authorization", "Bearer " + BOT_TOKEN);
        
                    try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(destinationPath)) {
        
                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }
                        System.out.println("File Downloaded succesfully");
                    }
                    return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
     public static void sendMessageToSlack(String message) throws IOException {
	 	StringBuilder msgBuilder = new StringBuilder();
	 	msgBuilder.append(message);
	 	Payload payload = Payload.builder().channel(slackChannel).text(msgBuilder.toString()).build();
	 	WebhookResponse webResp = Slack.getInstance().send(webHooksUrl, payload);
	 }
  
  
    private void executeMavenCommand(boolean confirmation) throws ExecutionException, TimeoutException {
	    try {
	        // Set the working directory
	        String workingDirectory = System.getProperty("user.dir");

	        // Determine the confirmation argument based on the boolean value
	        String confirmationArg = confirmation ? "yes" : "no";

	        // Execute the Maven command using ProcessBuilder
	        String mvnExecutable = "/opt/homebrew/bin/mvn"; // Replace '/path/to/mvn' with the actual path to the 'mvn' executable
	        ProcessBuilder processBuilder = new ProcessBuilder(mvnExecutable, "test", "-Dtest=NYAutomation.AutomationFlow#flow", "-Dconfirmation=" + confirmationArg);
	        processBuilder.directory(new File(workingDirectory));

	        Process process = processBuilder.start();

	        // Read the output of the process
	        try (InputStream inputStream = process.getInputStream();
	             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

	            String line;
	            while ((line = bufferedReader.readLine()) != null) {
	                System.out.println(line);
	            }
	        }

	        // Wait for the process to complete with timeout
	        int exitCode = waitForProcessWithTimeout(process, 30, TimeUnit.MINUTES); // Set the timeout value as needed

	        // Check if the process completed successfully
	        if (exitCode == 0) {
	            System.out.println("Maven command executed successfully");
	        } else {
	            System.out.println("Failed to execute Maven command. Exit code: " + exitCode);
	        }
	    } catch (IOException | InterruptedException e) {
	        System.out.println("An error occurred while executing the Maven command: " + e.getMessage());
	    }
	}

	private void executeYesMavenCommand() throws ExecutionException, TimeoutException {
	    executeMavenCommand(true);
	}

	private void executeNoMavenCommand() throws ExecutionException, TimeoutException {
	    executeMavenCommand(false);
    }
  
    private int waitForProcessWithTimeout(Process process, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        Future<Integer> future = executorService.submit(() -> process.waitFor());
        try {
              return future.get(timeout, timeUnit);
        } finally {
              future.cancel(true);
        }
    }

}