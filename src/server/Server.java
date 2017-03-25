package server;

import client.Client;
import core.MainHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static core.MainHandler.DEFAULT_PORT;

/**
 * Created by nadir on 14.03.2017.
 */
public class Server {
    public ServerSocket serverSocket;
    private int port;
    private List<Socket> clients = new ArrayList<>();

    public Server(){
        this(DEFAULT_PORT);
    }

    public Server(int port){
        this.port = port;
        startServer();
    }

    private void startServer(){
        try {
            MainHandler.graphics.log("Сервер с портом "  + port + " запущен", false, false);
            new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        clients.add(socket);
                        //TODO
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            MainHandler.graphics.log("Невозможно запустить сервер. Возможно, сервер с текущим портом уже открыт", false, false);
        }
    }

    public void stopServer(){
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
