import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server{
    private static int uniqueID;
    private ArrayList<ClientThread> clientsTs;
    private int port;
    boolean working;
    private String name;

    public Server(int port, String name){
        this.port = port;
        setName(name);
        working = true;
        clientsTs = new ArrayList<>();
        start();
    }

    private void setName(String name){
        this.name = name;
    }

    void start(){
        Thread ServerThread = new Thread(() -> {
            ServerSocket serverSocket = null;
            try{
                serverSocket = new ServerSocket(port);
                    while (working) {
                        Socket socket = serverSocket.accept();
                        if(!working)
                            break;
                        clientsTs.add(new ClientThread(socket));
                    }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stop();
                try {
                    if(serverSocket != null)
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ServerThread.setName("Server");
        ServerThread.start();
    }

    void stop(){
        broadcast("<html><font face='arial' color='red'>Сервер выключен.</font></html>");
        for (ClientThread ct : clientsTs) {
            try {
                ct.writeMsg("STOP");
            } catch (Exception e) {
                System.out.println("Some problem with stopping server");
            }
        }
        try {
            new Socket("localhost", Main.DEFAULT_PORT);
        } catch (Exception e){
            e.printStackTrace();
        }
        working = false;
        uniqueID = 0;
        name = "";
        clientsTs.clear();
    }

    private synchronized void broadcast(String message){
        for (ClientThread ct : clientsTs) {
            if(ct.listen)
                ct.writeMsg(message);
        }
    }

    private synchronized void remove(int id) {
        try {
            clientsTs.get(id).listen = false;
            clientsTs.remove(id);
        } catch (Exception e){
            System.out.println("No client like this(remove)");
        }
    }

    private void handleMessage(String msg, int id){
        String[] message = msg.split("/::/");
        switch (message[0]){
            case "LOGIN":
                broadcast("<html><font face='arial' color='yellow'>" + message[1] + " подключился на сервер.</font></html>");
                clientsTs.get(id).username = message[1];
                break;
            case "MESSAGE":
                broadcast(message[1]);
                break;
            case "LOGOUT":
                remove(id);
                broadcast("<html><font face='arial' color='yellow'>" + clientsTs.get(id).username + " отключился</font></html>");
                break;
        }
    }

    class ClientThread extends Thread {
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        int id;
        String msg;
        String username;
        boolean listen = true;

        ClientThread(Socket socket){
            id = uniqueID++;
            this.socket = socket;
            setName("ClientThread-" + id);
            start();
        }

        public void run(){
            try{
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output.println("<html><font face='arial' color='green'>Вы подключены к серверу " + name + "</font></html>");
            } catch (Exception e){
                System.out.println("Some problem with ClientThread");
            }

            while (listen){
                if(working) {
                    try {
                        msg = input.readLine();
                        handleMessage(msg, id);
                    } catch (Exception e) {

                    }
                }
                else {
                    listen = false;
                    remove(id);
                    close();
                }
            }
        }

        private void close(){
            try {
                if(output != null) output.close();
            }
            catch(Exception e) {}
            try {
                if(input != null) input.close();
            }
            catch(Exception e) {}
            try {
                if(socket != null && !socket.isClosed()) socket.close();
            }
            catch (Exception e) {}
            finally {
                broadcast("<html><font face='arial' color='yellow'>" + username + " отключился.</font></html>");
            }
        }

        void writeMsg(String msg){
            try{
                output.println(msg);
            } catch (Exception e){
                close();
                System.out.println("Error writing to " + id);
            }
        }
    }
}