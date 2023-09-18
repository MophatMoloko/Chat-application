package CommunicateApp.Services.SharedHelpers;

import java.io.Serializable;

public class TextMessage extends Message implements Serializable {

    public String message;
    public String messageId;
    public String timeStamp;
    public boolean receivedByServer = false;
    public boolean receivedByClient = false;
    public TextMessage(String destinationUser, String sourceUser, MessageType type, String mess, String messageId, String timeStamp) {
        super(destinationUser, sourceUser, type);
        this.message = mess;
        this.messageId = messageId;
        this.timeStamp = timeStamp;
    }
}