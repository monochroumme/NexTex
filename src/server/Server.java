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
            MainHandler.graphics.log("<html><font face='verdana' color='green'>Сервер с портом </font><font face='arial' color='white'>"  + port + "</font><font face='verdana' color='green'> запущен</font></html>", false, false, false);
            new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        clients.add(socket);
                        //TODO
                    }
                } catch (Exception e){
                    System.out.println("Own server is closed");
                }
            }).start();
        } catch (Exception e) {
            MainHandler.graphics.log("<html><font face='verdana' color='red'>Невозможно запустить сервер. Возможно, сервер с текущим портом уже открыт</font></html>", false, false, false);
        }
    }

    public void stopServer(){
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateClientsList(){

    }
}
