package CommunicateApp.Services.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static CommunicateApp.Services.SharedHelpers.MessageBuilder.*;
import static CommunicateApp.Services.SharedHelpers.MessageBuilder.ParseMessage;

public class Server implements Runnable{

    private final int port;
    private final List<User> users;
    private ServerSocket server;
    protected PrintStream output;

    public Server(int port, PrintStream output) {
        this.port = port;
        this.users = new ArrayList<User>();
        this.output = output;
    }

    /**
     * This method handles all messages received by the server per the UCTCOMMUNICATE
     * protocol specification.
     *
     * @param   message         Received Message Object To Hqndle
     * @throws  IOException
     */
    public void HandleMessage(String message, Socket client) throws IOException {

        var parsedMessage = ParseMessage(message);
        var messageIdentifier = parsedMessage[1];
        var clientStream = client.getOutputStream();
        Writer clientOut = new BufferedWriter(new OutputStreamWriter(clientStream, StandardCharsets.UTF_8));

        switch (messageIdentifier) {
            case "AUTHREQ":
                handleAuthRequest(client, parsedMessage, clientOut);
                break;
            case "SENDMESSAGE":
                handleSendMessage(parsedMessage, clientOut);
                break;
            case "SENDMESSAGERES":
                handleSendMessageAck(message, parsedMessage);
                break;
            case "GETUSERREQ":
                handleGetUserRequest(parsedMessage, clientOut);
                break;
        }
    }

    private void handleGetUserRequest(String[] parsedMessage, Writer clientOut) throws IOException {
        var body = parsedMessage[3];
        var recipientCalculatedDigest = GenerateDigest(body);
        var validate = recipientCalculatedDigest.equals(parsedMessage[2]);
        if(validate) {
            clientOut.append(RequestUserListResponse(users)).append("\n");
            clientOut.flush();
            return;
        } else {
            output.println("A Validation Error Occurred While Parsing A GETUSERREQ Message From A Client.");
        }
    }

    private boolean handleSendMessageAck(String message, String[] parsedMessage) throws IOException {
        var body = parsedMessage[3];
        var recipientCalculatedDigest = GenerateDigest(body);
        var validate = recipientCalculatedDigest.equals(parsedMessage[2]);
        if(validate) {
            for (User user : this.users) {
                var userOut = new BufferedWriter(new OutputStreamWriter(user.client.getOutputStream(), StandardCharsets.UTF_8));
                userOut.append(message).append("\n");
                userOut.flush();
            }
            return true;
        } else {
            output.println("A Validation Error Occurred While Parsing A SENDMESSAGERES Message From A Client.");
        }
        return false;
    }

    private boolean handleSendMessage(String[] parsedMessage, Writer clientOut) throws IOException {
        var body = parsedMessage[3];
        var recipientCalculatedDigest = GenerateDigest(body);
        var validate = recipientCalculatedDigest.equals(parsedMessage[2]);
        if(validate) {
            var parsedBody = ParseBody(body);
            var source = parsedBody[0];
            var destination = parsedBody[1];
            var messageToTransmit = parsedBody[2];
            var messageId = parsedBody[3];
            var timeStamp = parsedBody[4];

            for (User user : this.users) {
                if (destination.equals("GROUP") || user.getUsername().equals(destination)) {
                    var userOut = new BufferedWriter(new OutputStreamWriter(user.client.getOutputStream(), StandardCharsets.UTF_8));
                    userOut.append(SendMessageRequest(source, destination, messageToTransmit, messageId, timeStamp)).append("\n");
                    userOut.flush();
                }
            }

            clientOut.append(SendMessageResponse(true, messageId, "SERVER")).append("\n");
            clientOut.flush();
            if(destination.equals("GROUP")){
                clientOut.append(SendMessageResponse(true, messageId, "CLIENT")).append("\n");
                clientOut.flush();
            }
            return true;
        } else {
            output.println("A Validation Error Occurred While Parsing A SENDMESSAGE Message From A Client.");
        }
        return false;
    }

    private void handleAuthRequest(Socket client, String[] parsedMessage, Writer clientOut) throws IOException {
        var body = parsedMessage[3];
        var recipientCalculatedDigest = GenerateDigest(body);
        var validate = recipientCalculatedDigest.equals(parsedMessage[2]);
        if(validate) {
            var parsedBody = ParseBody(body);
            var username = parsedBody[0];
            var nickname = parsedBody[1];
            var password = parsedBody[2];

            for (User user : this.users) {
                if (user.getUsername().equals(username)) {
                    if (user.getPassword().equals(password)) {
                        clientOut.append(RespondAuthentication(true, "User Logged In Successfully.")).append("\n");
                        clientOut.flush();
                        return;
                    } else {
                        clientOut.append(RespondAuthentication(false, "Password Invalid.")).append("\n");
                        clientOut.flush();
                        return;
                    }
                }
            }
            // Username attempting to login not found, create new user
            var user = new User(users.size() + 1, username, password, nickname, client);
            users.add(user);
            clientOut.append(RespondAuthentication(true, "User Registered Successfully.")).append("\n");
            clientOut.flush();
        } else {
            clientOut.append(RespondAuthentication(false, "Message Was Corrupted During Transfer. Please Try Again.")).append("\n");
            clientOut.flush();
            output.println("A Validation Error Occurred While Parsing A AUTHREQ Message From A Client.");
        }
    }

    public void run() {
        try {
            server = new ServerSocket(port);
            output.println("Port " + port + " is now open on " + server.getInetAddress().getHostAddress() + ".");
            while (true) {
                Socket client = server.accept();
                new Thread(new UserMessageHandler(this, client)).start();
            }
        } catch (Exception e){
            output.println("A Generic Server Error Has Occurred. Please Ensure That The Port Is Available.");
        }
    }
}
