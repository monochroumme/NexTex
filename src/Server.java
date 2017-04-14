import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server{
    private static int uniqueID;
    private ArrayList<ClientListener> clients;
    boolean working;
    String serverName;
    ServerSocket serverSocket;

    Server(String serverName){
        clients = new ArrayList<>();
        this.serverName = serverName;
        working = true;
        uniqueID = 0;
        start();
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

                    clients.add(new ClientListener(client));
                }
                stop();
            } catch (IOException e) {}
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
                clients.get(id).name = message[1];
                break;
            case "MESSAGE":
                broadcast(message[1]);
                break;
            case "LOGOUT":
                clients.get(id).close();
                broadcast("<html><font face='arial' color='yellow'>" + clients.get(id).name + " отключился</font></html>");
                break;
        }
    }

    synchronized void broadcast(String msg){
        for (ClientListener ct : clients) {
            try {
                if (ct.listen)
                    ct.output.println(msg);
            } catch (Exception e){
                ct.close();
            }
        }
    }

     synchronized void stop(){
        broadcast("<html><font face='arial' color='red'>Сервер " + serverName + " выключен</font></html>");
        working = false;
        try{
        for (ClientListener cl : clients) {
            cl.close();
        }} catch (Exception e) { e.printStackTrace();}
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try{
            clients.clear();
        } catch (Exception e) {}
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
            } catch (IOException e) {}
            finally {
                close();
            }
        }

         void close(){
            listen = false;
            try {
                output.println("<html><font face='arial' color='red'>Вы отключены от сервера</font></html>");
                try {
                    if (socket != null) socket.close();
                } catch (Exception e) {}
            } catch (Exception e){}
        }
    }
}