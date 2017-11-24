import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server{
    private static int uniqueID;
    private ArrayList<ClientListener> clients;
    private ServerSocket serverSocket;
    private String serverName;
    boolean working;

    Server(String serverName){
        clients = new ArrayList<>();
        reset(serverName);
    }

    void reset(String serverName){
        this.serverName = serverName;
        working = true;
        uniqueID = 0;
        clients.clear();
        updateClientsList();
        start();
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
            } catch (IOException e) {}
            finally {
                stop();
            }
        }).start();
    }

    private void handleMsg(String msg, ClientListener cl){
        String[] message = msg.split("/::/");
        switch (message[0]){
            case "LOGIN":
                broadcast("<font face='arial' color='yellow'>" + message[1] + " has connected to the server</font>");
                String color = message[1].substring(message[1].indexOf("color='") + 7, message[1].indexOf("'>"));
                String name = message[1].substring(message[1].lastIndexOf("'>") + 2, message[1].indexOf("</font"));
                clients.get(clients.indexOf(cl)).name = message[1];
                if(message[1].equals(Main.selfClient.getNickname())){ // if admin
                    message[1] = "<font face='Verdana Bold' color='" + color + "' size=5>" + name + "</font>";
                }
                else { // if not admin
                    message[1] = "<font face='Verdana Bold' color='" + color + "' size=4>" + name + "</font>";
                }
                clients.get(clients.indexOf(cl)).listName = message[1];
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
                broadcast("<font face='arial' color='yellow'>" + clients.get(clients.indexOf(cl)).name + " has changed his nickname to " + message[1] + "</font>");
                clients.get(clients.indexOf(cl)).name = message[1];
                clients.get(clients.indexOf(cl)).listName = "<font face='Verdana Bold' color='" + message[1].substring(message[1].indexOf("color='") + 7, message[1].indexOf("'>")) +
                        "' size=4>" + message[1].substring(message[1].lastIndexOf("'>") + 2, message[1].indexOf("</font")) + "</font>";
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
            cl.listen = false;
            cl.close();
        }
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void changeServerName(String name){
        serverName = name;
        broadcast("<font face='arial' color='yellow'>The name of the server has been changed to " + serverName + "</font>"); // sending visual message to clients
        broadcast("NEWNAME:" + serverName); // sending the new name to clients
    }

    private void updateClientsList(){
        String clientsNames = "LIST";
        for (ClientListener client : clients) {
            if (client.name != null)
                clientsNames = clientsNames.concat(":<html>" + client.listName + "</html>");
        }
        broadcast(clientsNames);
    }

    class ClientListener extends Thread {
        int id;
        String name;
        String listName;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        boolean listen;

        ClientListener(Socket socket){
            id = uniqueID++;
            this.socket = socket;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("NAME:" + serverName);
            } catch (Exception e){
                System.out.println("Some problem with creation input or output of client:" + name);
            }
            listen = true;
            start(); // starts the thread which calls run() method
        }

        public void run(){ // runs once
            listen();
        }

        void listen(){
            String msg;
            try {
                while (listen) {
                    msg = input.readLine();
                    if (msg != null || listen)
                        handleMsg(msg, this);
                }
            } catch (IOException e) {}
            finally {
                close();
            }
        }

         void close(){
            if(!listen && socket.isConnected() && !socket.isClosed()) {
                if(working) {
                    clients.remove(this);
                    if(name != null)
                        broadcast(name + "<font face='arial' color='yellow'> has left the server</font>");
                    updateClientsList();
                } else
                    output.println("STOP");
                uniqueID--;

                try {
                    if (socket != null) socket.close();
                } catch (Exception e) {}
            }
        }
    }
}