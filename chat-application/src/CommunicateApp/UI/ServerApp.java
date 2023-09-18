package CommunicateApp.UI;

import CommunicateApp.Services.Client.Client;
import CommunicateApp.Services.Server.Server;
import CommunicateApp.Services.SharedHelpers.CustomOutputStream;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintStream;

public class ServerApp {
    private JButton startServerButton;
    private JTextField portNumberInput;
    private JPanel Panel;
    private JTextArea serverOutput;
    private final PrintStream stream;

    public static void setupUI(){
        JFrame frame = new JFrame("ServerScreen");
        frame.setContentPane(new ServerApp().Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        setupUI();
    }

    public ServerApp() {
        stream = new PrintStream(new CustomOutputStream(serverOutput));
        startServerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                var port = Integer.parseInt(portNumberInput.getText());
                new Thread(new Server(port, stream)).start();
            }
        });
    }
}
