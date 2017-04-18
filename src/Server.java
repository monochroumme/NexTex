import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{
    private static int uniqueID;
    private ClientListener[] clients;
    boolean working;
    private String serverName;
    private ServerSocket serverSocket;

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

    private void start(){
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

    private void handleMsg(String msg, int id){
        String[] message = msg.split("/::/");
        switch (message[0]){
            case "LOGIN":
                broadcast("<html><font face='arial' color='yellow'>" + message[1] + " подключился на сервер.</font></html>");
                if(message[1].equals(Main.selfClient.getNickname())){
                    String color = message[1].substring(message[1].indexOf("color='") + 7, message[1].indexOf("'>"));
                    String name = message[1].substring(message[1].lastIndexOf("'>") + 2, message[1].indexOf("</font"));
                    message[1] = "<font face='Verdana Bold' color='" + color + "' size=4>" + name + "</font>";
                }
                clients[id].name = message[1];
                updateClientsList();
                break;
            case "MESSAGE":
                broadcast(message[1]);
                break;
            case "LOGOUT":
                clients[id].listen = false;
                clients[id].close();
                break;
            case "NEWNICK":
                broadcast("<html><font face='arial' color='yellow'>" + clients[id].name + " изменил свой ник на " + message[1] + "</font></html>");
                clients[id].name = message[1];
                updateClientsList();
                break;
        }
    }

    private synchronized void broadcast(String msg){
        for (ClientListener client : clients) {
            if (client == null)
                break;
            try {
                if (client.listen)
                    client.output.println(msg);
            } catch (Exception e) {
                client.close();
            }
        }
    }

    void stop(){
        working = false;
        for (ClientListener cl : clients) {
            if(cl != null && cl.name != null) {
                cl.output.println("<html><font face='arial' color='red'>Сервер " + serverName + " выключен</font></html>");
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

    private void clearListOfClients(){
        for (int i = 0; i < clients.length; i++) {
            clients[i] = null;
        }
    }

    void changeServerName(String name){
        serverName = name;
        broadcast("<html><font face='arial' color='yellow'>Название сервера изменено на " + serverName + "</font></html>");
    }

    private void updateClientsList(){
        String clientsNames = "LIST";
        for (int i = 0; i < clients.length; i ++) {
            if(clients[i] == null)
                break;
            if(clients[i].name != null)
                clientsNames = clientsNames.concat(":" + clients[i].name);
        }
        broadcast(clientsNames);
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
                    } catch (Exception e) {}
                } catch (Exception e) {}

                broadcast("<html><font face='arial' color='yellow'>" + name + " вышел из сервера.</font></html>");
                clients[id] = null;
                uniqueID--;

                if(working){
                    updateClientsList();
                }
            }
        }
    }
}