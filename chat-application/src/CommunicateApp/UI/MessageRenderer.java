package CommunicateApp.UI;

import CommunicateApp.Services.SharedHelpers.Message;
import CommunicateApp.Services.SharedHelpers.TextMessage;

import javax.swing.*;
import java.awt.*;

public class MessageRenderer extends JLabel implements ListCellRenderer<Message> {

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message messageObj, int index, boolean isSelected, boolean cellHasFocus) {

        var textMessage = (TextMessage)messageObj;

        setOpaque(true);

        if(textMessage.receivedByServer){
            if(textMessage.receivedByClient) {
                setBackground(Color.GREEN);
            } else {
                setBackground(Color.CYAN);
            }
        } else {
            setBackground(Color.RED);
        }
        setText(textMessage.sourceUser + ": " + textMessage.message + " (" + textMessage.timeStamp + ")");

        return this;
    }

}