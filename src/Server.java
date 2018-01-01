import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server{
    private static int uniqueID;
    private ArrayList<ClientListener> clients;
    private ServerSocket serverSocket;
    private String serverName;
    private ArrayList<String> blacklist;
    boolean working;

    Server(String serverName){
        clients = new ArrayList<>();
        blacklist = new ArrayList<>();
        reset(serverName);
    }

    void reset(String serverName){
        this.serverName = serverName;
        working = true;
        uniqueID = 0;
        clients.clear();
        blacklist.clear();
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

                    for (String ip : blacklist){
                        if (ip.substring(0, ip.indexOf(" (")).equals(client.getLocalAddress().toString().substring(1))){
                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()),true);
                            pw.println("<font face='arial' color='red'>Banned from this server</font>");
                            pw.println("DISCONNECT");
                            client.close();
                            pw.close();
                        }
                    }

                    if(!client.isClosed() && client.isConnected())
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
                cl.output.println("<font face='arial' color='gray'>Welcome to chat!</font>");
                updateClientsList();
                break;
            case "MESSAGE":
                broadcast(message[1]);
                break;
            case "LOGOUT":
                clients.get(clients.indexOf(cl)).listen = false;
                clients.get(clients.indexOf(cl)).close();
                if(cl.name != null)
                    broadcast(cl.name + "<font face='arial' color='yellow'> has left the server</font>");
                clients.remove(cl);
                break;
            case "NEWNICK":
                broadcast("<font face='arial' color='yellow'>" + clients.get(clients.indexOf(cl)).name + " has changed his nickname to " + message[1] + "</font>");
                clients.get(clients.indexOf(cl)).name = message[1];
                if(clients.get(clients.indexOf(cl)).name.equals(Main.selfClient.getNickname()))
                    clients.get(clients.indexOf(cl)).listName = "<font face='Verdana Bold' color='" + message[1].substring(message[1].indexOf("color='") + 7, message[1].indexOf("'>")) +
                            "' size=5>" + message[1].substring(message[1].lastIndexOf("'>") + 2, message[1].indexOf("</font")) + "</font>";
                else
                    clients.get(clients.indexOf(cl)).listName = "<font face='Verdana Bold' color='" + message[1].substring(message[1].indexOf("color='") + 7, message[1].indexOf("'>")) +
                            "' size=4>" + message[1].substring(message[1].lastIndexOf("'>") + 2, message[1].indexOf("</font")) + "</font>";
                updateClientsList();
                break;
            case "MOD":
                for (ClientListener client : clients) {
                    if(client.name.substring(client.name.indexOf("'>") + 2, client.name.indexOf("</")).equals(msg.substring(msg.indexOf("/::/") + 4))){
                        if(!client.moderator) {
                            client.moderator = true;
                            client.output.println("MOD");
                            broadcast("<font face='arial' color='yellow'>" + client.name + " is now moderator</font>");
                        } else
                            cl.output.println("<font face='arial' color='red'>That user is a moderator already</font>");
                    }
                }
                break;
            case "UNMOD":
                for (ClientListener client : clients){
                    if(client.name.substring(client.name.indexOf("'>") + 2, client.name.indexOf("</")).equals(msg.substring(msg.indexOf("/::/") + 4))){
                        if(client.moderator) {
                            client.moderator = false;
                            client.output.println("UNMOD");
                            broadcast("<font face='arial' color='yellow'>" + client.name + " is not moderator anymore</font>");
                        } else
                            cl.output.println("<font face='arial' color='red'>That user is not a moderator already</font>");
                    }
                }
                break;
            case "KICK":
                kickOrBan(cl, msg, "KICK");
                break;
            case "BAN":
                kickOrBan(cl, msg, "BANN");
                break;
            case "UNBAN":
                if (blacklist.size() != 0){
                    if (message[1].matches("[0-9]+$") && blacklist.size() >= Integer.parseInt(message[1]) - 1) {
                        broadcast("<font face='arial' color='yellow'>IP <font color='white'>" + blacklist.get(Integer.parseInt(message[1]) - 1) + "</font> has been unbanned</font>");
                        blacklist.remove(Integer.parseInt(message[1]) - 1);
                    } else {
                        for (String ipNickname : blacklist) {
                            if (ipNickname.substring(ipNickname.indexOf("'>") + 2, ipNickname.indexOf("</")).equals(msg.substring(msg.indexOf("/::/") + 4))) {
                                broadcast("<font face='arial' color='yellow'>User " + ipNickname.substring(ipNickname.indexOf("(<") + 1, ipNickname.indexOf(">)") + 1) + " has been unbanned</font>");
                                blacklist.remove(ipNickname);
                                return;
                            } else if (ipNickname.substring(0, ipNickname.indexOf(" (")).equals(message[1])){
                                broadcast("<font face='arial' color='yellow'>IP <font color='white'>" + message[1] + "</font> has been unbanned</font>");
                                blacklist.remove(ipNickname);
                                return;
                            }
                        }
                    }
                }
                cl.output.println("<font face='arial' color='red'>That IP isn't banned</font>");
                break;
            case "BLACKLIST":
                String blacklist = "BLACKLIST:";
                for (String ip : this.blacklist){
                    blacklist = blacklist.concat(ip + ":");
                }
                cl.output.println(blacklist);
                System.out.println(blacklist);
                break;
        }
    }

    private synchronized void broadcast(String msg){
        for (ClientListener client : clients) {
            try {
                if (client.listen)
                    client.output.println(msg);
            } catch (Exception e) {
                if(client.name != null)
                broadcast(client.name + "<font face='arial' color='yellow'> has left the server</font>");
                client.listen = false;
                client.close();
            }
        }
    }

    void stop(){
        working = false;
        try {
            for (ClientListener cl : clients) {
                cl.listen = false;
                cl.close();
            }
        } catch (Exception e){}
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

    private void kickOrBan(ClientListener cl, String msg, String outCommand){
        for (int i = 1; i < clients.size(); i++){
            if(clients.get(i).name.substring(clients.get(i).name.indexOf("'>") + 2, clients.get(i).name.indexOf("</")).equals(msg.substring(msg.indexOf("/::/") + 4))) {
                if(!clients.get(i).moderator){
                    String nick = clients.get(i).name;
                    if (outCommand.equals("BANN"))
                        blacklist.add(clients.get(i).socket.getLocalAddress().toString().substring(1) + " (" + clients.get(i).name + ")");
                    clients.get(i).output.println(outCommand);
                    clients.get(i).listen = false;
                    clients.get(i).close();
                    broadcast(nick + "<font face='arial' color='yellow'> has been " + outCommand.toLowerCase() + "ed from the server</font>");
                    return;
                } else
                    cl.output.println("<font face='arial' color='red'>You cannot " + outCommand.toLowerCase() + " the admin nor a moderator</font>");
            }
        }
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
        boolean moderator = false;

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
                    updateClientsList();
                } else
                    output.println("STOP");
                uniqueID--;

                try {
                    if (socket != null) socket.close();
                    output.close();
                    input.close();
                } catch (Exception e) {}
            }
        }
    }
}