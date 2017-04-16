import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server{
    private static int uniqueID;
    private ClientListener[] clients;
    boolean working;
    String serverName;
    ServerSocket serverSocket;

    Server(String serverName){
        clients = new ClientListener[50];
        reset(serverName);
        start();
    }

    void reset(String serverName){
        this.serverName = serverName;
        working = true;
        uniqueID = 0;
        clearListOfClients();
    }

    void start(){
        System.out.println("Starting server");
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(Main.DEFAULT_PORT);
                while (working){
                    Socket client = serverSocket.accept();

                    if(!working)
                        break;

                    clients[uniqueID] = new ClientListener(client);
                }
                stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                stop();
            }
        }).start();
    }

    void handleMsg(String msg, int id){
        String[] message = msg.split("/::/");
        switch (message[0]){
            case "LOGIN":
                broadcast("<html><font face='arial' color='yellow'>" + message[1] + " подключился на сервер.</font></html>");
                clients[id].name = message[1];
                break;
            case "MESSAGE":
                broadcast(message[1]);
                break;
            case "LOGOUT":
                clients[id].listen = false;
                clients[id].close();
                break;
        }
    }

    synchronized void broadcast(String msg){
        for (ClientListener client : clients) {
            if (client != null) {
                try {
                    if (client.listen)
                        client.output.println(msg);
                } catch (Exception e) {
                    client.close();
                }
            }
        }
    }

    void stop(){
        broadcast("<html><font face='arial' color='red'>Сервер " + serverName + " выключен</font></html>");
        working = false;
        for (ClientListener cl : clients) {
            if(cl != null && cl.name != null) {
                cl.listen = false;
                cl.close();
            }
        }
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void clearListOfClients(){
        for (int i = 0; i < clients.length; i++) {
            clients[i] = null;
        }
    }

    class ClientListener extends Thread {
        int id;
        String name;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        boolean listen;

        ClientListener(Socket socket){
            id = uniqueID++;
            this.socket = socket;
            listen = true;
            start();
        }

        public void run(){
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("<html><font face='arial' color='green'> Вы подключились к серверу " + serverName + "</font></html>");
                listen();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        void listen(){
            String msg;
            try {
                while (listen) {
                    msg = input.readLine();
                    if (msg == null)
                        return;
                    handleMsg(msg, id);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                close();
            }
        }

         void close(){
            if(!listen && socket.isConnected() && !socket.isClosed()) {
                try {
                    output.println("<html><font face='arial' color='red'>Вы отключены от сервера</font></html>");
                    try {
                        if (socket != null) socket.close();
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }

                broadcast("<html><font face='arial' color='yellow'>" + name + " вышел из сервера.</font></html>");
                clients[id] = null;
            }
        }
    }
}