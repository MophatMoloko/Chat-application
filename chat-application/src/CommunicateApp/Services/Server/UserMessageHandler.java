package CommunicateApp.Services.Server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class UserMessageHandler implements Runnable {

    private final Server server;
    private final Socket client;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    public UserMessageHandler(Server server, Socket client) {
        this.server = server;
        this.client = client;
        try {
            this.inStream = new DataInputStream(client.getInputStream());
            this.outStream = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
                try {
                    String msg = inStream.readLine();
                    server.HandleMessage(msg, client);
                } catch(EOFException ex) {
                    server.output.println("EOF ERROR");
                } catch(SocketException e){
                    break;
                } catch (Exception e) {
                    server.output.println("Unexpected Error");
                }
        }
    }
}
