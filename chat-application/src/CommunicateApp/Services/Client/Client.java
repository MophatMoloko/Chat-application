package CommunicateApp.Services.Client;

import CommunicateApp.Services.Server.User;
import CommunicateApp.Services.SharedHelpers.*;
import CommunicateApp.UI.ClientApp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static CommunicateApp.Services.SharedHelpers.MessageBuilder.*;

public class Client implements Runnable {

    private final String hostAddress;
    private final int port;
    private Socket client;
    private PrintStream output;
    public List<User> users;
    public List<Message> messages;
    public User currentUser;
    public ClientApp context;

    public Client(String hostAddress, int port, ClientApp context) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.context = context;
        this.messages = new ArrayList<>();
    }

    public void RegisterUser(String username, String nickname, String password) throws IOException {
        client = new Socket(hostAddress, port);
        var message = RequestAuthentication(username, nickname, password);
        currentUser = new User(0, username, password, nickname, client);
        output = new PrintStream(client.getOutputStream());
        output.println(message);
    }

    public void SendMessage(String destination, String message, MessageType type) throws IOException {
        var messageId = UUID.randomUUID().toString();
        var messageString = SendMessageRequest(currentUser.getUsername(), destination, message, messageId, new Date().toString());
        var textMessage = new TextMessage(destination, currentUser.getUsername(), type, message, messageId, new Date().toString());
        messages.add(textMessage);
        output = new PrintStream(client.getOutputStream());
        output.println(messageString);
    }

    public void ReceiveMessage(String message) throws UnsupportedEncodingException {
        var parsedMessage = ParseMessage(message);
        var messageIdentifier = parsedMessage[1];
        Writer out = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));

        switch (messageIdentifier) {
            case "AUTHRES":
                handleAuthResponseMessage(parsedMessage);
                break;
            case "SENDMESSAGE":
                handleReceivedMessage(parsedMessage);
                break;
            case "SENDMESSAGERES":
                handleMessageAck(parsedMessage);
                break;
            case "GETUSERRES":
                handleUserListResponse(parsedMessage);
                break;
        }
    }

    public void RequestUsers() throws IOException {
        var message = RequestUserList();
        output = new PrintStream(client.getOutputStream());
        output.println(message);
    }
    private void handleMessageAck(String[] parsedMessage) {
        var body = parsedMessage[3];
        var recipientCalculatedDigest = GenerateDigest(body);
        var validate = recipientCalculatedDigest.equals(parsedMessage[2]);
        if (validate) {
            var parsedBody = ParseBody(body);
            for (Message message : this.messages) {
                var textMessage = (TextMessage) message;
                if (textMessage.messageId.equals(parsedBody[1])) {
                    if (parsedBody[2].equals("SERVER")) {
                        ((TextMessage) message).receivedByServer = true;
                    } else {
                        ((TextMessage) message).receivedByClient = true;
                    }
                }
            }
            context.RefreshChatPane();
        } else {
            return;
        }
    }

    private void handleReceivedMessage(String[] parsedMessage) {
        var body = parsedMessage[3];
        var recipientCalculatedDigest = GenerateDigest(body);
        var validate = recipientCalculatedDigest.equals(parsedMessage[2]);
        if (validate) {
            var parsedBody = ParseBody(body);
            if (parsedBody[1].equals("GROUP") && !parsedBody[0].equals(currentUser.getUsername())) {
                var textMessage = new TextMessage("GROUP", parsedBody[0], MessageType.GroupMessage, parsedBody[2], parsedBody[3], parsedBody[4]);
                textMessage.receivedByServer = true;
                textMessage.receivedByClient = true;
                messages.add(textMessage);
                acknowledgeReceivedMessage(textMessage.messageId, true);
            } else {
                var textMessage = new TextMessage(parsedBody[1], parsedBody[0], MessageType.PrivateMessage, parsedBody[2], parsedBody[3], parsedBody[4]);
                textMessage.receivedByServer = true;
                textMessage.receivedByClient = true;
                messages.add(textMessage);
                acknowledgeReceivedMessage(textMessage.messageId, true);
            }
            context.RefreshChatPane();
        } else {
            return;
        }
    }

    private void handleUserListResponse(String[] parsedMessage) {
        var body = parsedMessage[3];
        var recipientCalculatedDigest = GenerateDigest(body);
        var validate = recipientCalculatedDigest.equals(parsedMessage[2]);
        if (validate) {
            var parsedBody = ParseBody(body);
            users = new ArrayList<>();
            for(String user : parsedBody){
                if(!user.equals("")){
                    var newUser = new User(user);
                    users.add(newUser);
                }
            }
            context.RefreshUserList();
        } else {
            context.ShowMessage("A data transfer error occurred.");
        }
    }

    private void handleAuthResponseMessage(String[] parsedMessage) {
        var body = parsedMessage[3];
        var recipientCalculatedDigest = GenerateDigest(body);
        var validate = recipientCalculatedDigest.equals(parsedMessage[2]);
        if (validate) {
            var parsedBody = ParseBody(body);
            if (parsedBody[0].equals("PASS")) {
                context.ShowMessage("Successfully logged in.");
                context.RefreshUserList();
            } else {
                context.ShowMessage("Unable to login user.");
            }
        } else {
            context.ShowMessage("Unable to login user. A data transfer error occurred.");
        }
    }

    private void acknowledgeReceivedMessage(String messageId, boolean success) {
        try {
            output = new PrintStream(client.getOutputStream());
            output.println(SendMessageResponse(success, messageId, "CLIENT"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            if (client == null) {
                client = new Socket(hostAddress, port);
            }
            new Thread(new HandleIncomingMessage(client)).start();
        } catch (Exception e) {
            System.out.println("Error!");
        }
    }

    class HandleIncomingMessage implements Runnable {

        private final Socket socket;
        private final DataInputStream inStream;
        private final DataOutputStream outStream;

        public HandleIncomingMessage(Socket socket) throws IOException {
            this.socket = socket;
            this.inStream = new DataInputStream(socket.getInputStream());
            this.outStream = new DataOutputStream(socket.getOutputStream());
        }

        public void run() {
            while (true) {
                try {
                    String msg = inStream.readLine();
                    ReceiveMessage(msg);
                } catch (Exception e) {
                    System.out.println("An unexpected error occurred while communicating with the server.");
                }
            }
        }
    }
}
