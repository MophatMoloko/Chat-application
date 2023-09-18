package CommunicateApp.Services.SharedHelpers;

import java.io.Serializable;

public class Message implements Serializable {
    public String destinationUser;
    public String sourceUser;
    public MessageType messageType;

    public Message(String destinationUser, String sourceUser, MessageType type) {
        this.destinationUser = destinationUser;
        this.sourceUser = sourceUser;
        messageType = type;
    }
}