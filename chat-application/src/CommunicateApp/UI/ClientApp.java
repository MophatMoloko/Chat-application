package CommunicateApp.UI;

import CommunicateApp.Services.Client.Client;
import CommunicateApp.Services.Server.User;
import CommunicateApp.Services.SharedHelpers.CustomOutputStream;
import CommunicateApp.Services.SharedHelpers.Message;
import CommunicateApp.Services.SharedHelpers.MessageType;
import CommunicateApp.Services.SharedHelpers.TextMessage;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClientApp {
    private JButton sendButton;
    private JButton connectButton;
    private JTextField portNumberInput;
    private JTextField messageField;
    private JPanel Panel;
    private JTabbedPane tabbedPane1;
    private JPanel groupChatPanel;
    private JPanel authPanel;
    private JTextField username;
    private JTextField nickname;
    private JPasswordField password;
    private JButton registerButton;
    private JTextField hostname;
    private JTextArea groupChatOutput;
    private JPanel privateChatPanel;
    private JList usersList;
    private JButton refreshButton;
    private JList privateMessageList;
    private JList groupMessageList;
    private JScrollPane scrollPaneGroup;
    private JTextField privateMessageText;
    private JButton sendPrivateMessageButton;
    private Client client;
    private final PrintStream groupChatStream;
    private String privateChatUser;

    public static void main(String[] args) {
        JFrame frame = new JFrame("UCTCommunicate Client");
        frame.setContentPane(new ClientApp().Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public ClientApp() {
        groupChatStream = new PrintStream(new CustomOutputStream(groupChatOutput));

        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                var message = messageField.getText();
                try {
                    client.SendMessage("GROUP", message, MessageType.GroupMessage);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        var context = this;
        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                var host = hostname.getText();
                var port = Integer.parseInt(portNumberInput.getText());
                var user = username.getText();
                var pass = password.getText();

                client = new Client(host, port, context);
                new Thread(client).start();

                try {
                    client.RegisterUser(user, "DEPRECATED", pass);

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        refreshButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                RequestUserList();
            }
        });
        usersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Object sel = null;

                int[] selected = usersList.getSelectedIndices();

                for (int i = 0; i < selected.length; i++) {
                    privateChatUser = usersList.getModel().getElementAt(selected[i]).toString();
                    RefreshChatPane();
                }
            }
        });
        sendPrivateMessageButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                var message = privateMessageText.getText();
                try {
                    client.SendMessage(privateChatUser, message, MessageType.PrivateMessage);
                    RefreshChatPane();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    private void RequestUserList(){
        try {
            client.RequestUsers();
        } catch (Exception e){

        }
    }

    public void RefreshUserList(){
        usersList.setListData(client.users.toArray());
    }

    public void ShowMessage(String message){
        JOptionPane.showMessageDialog(new JFrame(), message, "Response", JOptionPane.INFORMATION_MESSAGE);
    }

    public void RefreshChatPane(){
        var groupMessages = new ArrayList<Message>();
        var personalMessages = new ArrayList<Message>();
        if(client.messages != null) {
            for (Message message : client.messages) {
                var username = message.sourceUser;
                if (message.messageType == MessageType.GroupMessage) {
                    groupMessages.add(message);
                } else {
                    // Shows messages for the conversation with the selected user
                    // Include where source or destination is selected user
                    if(message.destinationUser.equals(privateChatUser) || message.sourceUser.equals(privateChatUser)){
                        personalMessages.add(message);
                    }
                }
            }
        }
        privateMessageList.setCellRenderer(new MessageRenderer());
        privateMessageList.setListData(personalMessages.toArray());
        groupMessageList.setCellRenderer(new MessageRenderer());
        groupMessageList.setListData(groupMessages.toArray());
    }

}
