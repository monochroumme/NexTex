import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server{
    private static int uniqueID;
    private ArrayList<ClientListener> clients;
    boolean working;
    private String serverName;
    private ServerSocket serverSocket;

    Server(String serverName){
        clients = new ArrayList<>();
        reset(serverName);
        start();
    }

    void reset(String serverName){
        this.serverName = serverName;
        working = true;
        uniqueID = 0;
        clients.clear();
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

                    clients.add(new ClientListener(client));
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

    private void handleMsg(String msg, ClientListener cl){
        String[] message = msg.split("/::/");
        switch (message[0]){
            case "LOGIN":
                broadcast("<html><font face='arial' color='yellow'>" + message[1] + " подключился на сервер.</font></html>");
                if(message[1].equals(Main.selfClient.getNickname())){ // if admin
                    String color = message[1].substring(message[1].indexOf("color='") + 7, message[1].indexOf("'>"));
                    String name = message[1].substring(message[1].lastIndexOf("'>") + 2, message[1].indexOf("</font"));
                    message[1] = "<font face='Verdana Bold' color='" + color + "' size=5>" + name + "</font>";
                }
                else{
                    String color = message[1].substring(message[1].indexOf("color='") + 7, message[1].indexOf("'>"));
                    String name = message[1].substring(message[1].lastIndexOf("'>") + 2, message[1].indexOf("</font"));
                    message[1] = "<font face='Verdana Bold' color='" + color + "' size=4>" + name + "</font>";
                }
                clients.get(clients.indexOf(cl)).name = message[1];
                updateClientsList();
                break;
            case "MESSAGE":
                broadcast(message[1]);
                break;
            case "LOGOUT":
                clients.get(clients.indexOf(cl)).listen = false;
                clients.get(clients.indexOf(cl)).close();
                clients.remove(cl);
                break;
            case "NEWNICK":
                broadcast("<html><font face='arial' color='yellow'>" + clients.get(clients.indexOf(cl)).name + " изменил свой ник на " + message[1] + "</font></html>");
                clients.get(clients.indexOf(cl)).name = message[1];
                updateClientsList();
                break;
        }
    }

    private synchronized void broadcast(String msg){
        for (ClientListener client : clients) {
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
            if(cl.name != null) {
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
        clients.clear();
    }

    void changeServerName(String name){
        serverName = name;
        broadcast("<html><font face='arial' color='yellow'>Название сервера изменено на " + serverName + "</font></html>");
    }

    private void updateClientsList(){
        String clientsNames = "LIST";
        for (ClientListener client : clients) {
            if (client.name != null)
                clientsNames = clientsNames.concat(":<html>" + client.name + "</html>");
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
                    handleMsg(msg, this);
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
                    output.println("LIST:");
                    output.println("<html><font face='arial' color='red'>Сервер " + serverName + " выключен</font></html>");
                    output.println("<html><font face='arial' color='red'>Вы отключены от сервера</font></html>");
                    try {
                        if (socket != null) socket.close();
                    } catch (Exception e) {}
                } catch (Exception e) {}

                broadcast("<html><font face='arial' color='yellow'>" + name + " вышел из сервера.</font></html>");
                if(working)
                    clients.remove(this);
                uniqueID--;

                if(working){
                    updateClientsList();
                }
            }
        }
    }
}