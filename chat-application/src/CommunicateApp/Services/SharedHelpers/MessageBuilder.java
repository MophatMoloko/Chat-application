package CommunicateApp.Services.SharedHelpers;

import CommunicateApp.Services.Server.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MessageBuilder {
    public static final String INITFIELD = "UCTCOMMUNICATE";
    public static final String TERMINATIONFIELD = "ENDCOMMUNICATE";
    public static final String FIELDSEPERATOR = "|";
    public static final String BODYSEPERATOR = ";";

    private static String ConstructGenericMessage(String identifier, String body){
        var hash = GenerateDigest(body);

        body = RandomlyMutateBody(body);

        return INITFIELD + FIELDSEPERATOR + identifier + FIELDSEPERATOR + hash + FIELDSEPERATOR + body + FIELDSEPERATOR + TERMINATIONFIELD;
    }

    private static String RandomlyMutateBody(String body) {
        var rand = new Random();
        var randomInt = rand.nextInt(10);
        if(randomInt == 4){
            body = "THISISGARBAGE";
        }
        return body;
    }

    public static String RequestAuthentication(String username, String nickname, String password) {
        var messageIdentifier = "AUTHREQ";
        var body = username + BODYSEPERATOR + nickname + BODYSEPERATOR + password;
        return ConstructGenericMessage(messageIdentifier, body);
    }

    public static String RespondAuthentication(boolean success, String userReply) {
        var messageIdentifier = "AUTHRES";
        var body = (success ? "PASS" : "FAIL") + BODYSEPERATOR + userReply;
        return ConstructGenericMessage(messageIdentifier, body);
    }

    public static String SendMessageRequest(String sourceUsername, String destinationUsername, String message, String messageId, String timeStamp) {
        var messageIdentifier = "SENDMESSAGE";
        var body = sourceUsername + BODYSEPERATOR + destinationUsername + BODYSEPERATOR + message + BODYSEPERATOR + messageId + BODYSEPERATOR + timeStamp;
        return ConstructGenericMessage(messageIdentifier, body);
    }

    public static String SendMessageResponse(boolean success, String messageId, String entity) {
        var messageIdentifier = "SENDMESSAGERES";
        var body = (success ? "PASS" : "FAIL") + BODYSEPERATOR + messageId + BODYSEPERATOR + entity;
        return ConstructGenericMessage(messageIdentifier, body);
    }

    public static String RequestUserList() {
        var messageIdentifier = "GETUSERREQ";
        return ConstructGenericMessage(messageIdentifier, "");
    }

    public static String RequestUserListResponse(List<User> users) {
        var messageIdentifier = "GETUSERRES";
        var body = "";
        for(User user : users){
            body += user.getUsername() + BODYSEPERATOR;
        }
        return ConstructGenericMessage(messageIdentifier, body);
    }

    public static String[] ParseMessage(String message){
        return message.split("\\|");
    }

    public static String[] ParseBody(String body){
        return body.split(BODYSEPERATOR);
    }

    public static String GenerateDigest(String hashProperty) {
        try {
            byte[] bytesOfMessage = hashProperty.getBytes(StandardCharsets.UTF_8);

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] digest = md.digest(bytesOfMessage);

            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            System.out.println("Unable To Encode Message.");
            return "";
        }
    }
}
