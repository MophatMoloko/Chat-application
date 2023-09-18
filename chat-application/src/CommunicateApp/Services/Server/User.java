package CommunicateApp.Services.Server;

import CommunicateApp.Services.SharedHelpers.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private int userId;
    private final String username;
    private String nickname;
    private String password;
    protected transient Socket client;
    private List<Message> messages;

    public User(int userId, String username, String password, String nickname, Socket client) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.client = client;
        this.messages = new ArrayList<Message>();
    }

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
