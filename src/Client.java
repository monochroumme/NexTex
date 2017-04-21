import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by nadir on 02.04.2017.
 */
public class Client {
    private String nickname;
    private Socket socket;
    private BufferedReader input;
    PrintWriter output;
    boolean connected = false;
    private ServerListener listener;
    String serverName;

    void connect(String serverIP) {
        try {
            socket = new Socket(serverIP, Main.DEFAULT_PORT);
            connected = true;
        } catch (Exception e) {
            System.out.println("Error connecting to server " + serverIP);
            Main.chatGraphics.log("<html><font face='arial' color='red'>Сервера на данном IP нет.</font></html>");
            return;
        }

        System.out.println("Connected to server " + serverIP);

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println("Error creating input/output things");
            return;
        }

        listener = new ServerListener();
        listener.start();

        // Logging in
        try {
            output.println("LOGIN/::/" + nickname);
        } catch (Exception e) {
            System.out.println("Error doing login");
            disconnect();
        }
    }

    void sendMessage(String msg) {
        try {
            if(!connected) {
                Main.chatGraphics.log("<html><font face='arial' color='red'>Сначала подключитесь к серверу. Если вы не знаете как, то напишите <font face='arial' color='green'>/help</font></font></html>");
            } else output.println("MESSAGE/::/<html>" + nickname + "<font face='verdana' color='white'>: " + msg + "</font></html>");
        } catch (Exception e) {
            System.out.println("Error writing to server");
        }
    }

    void automaticallyConnect(){
        try {
            connect(findServers(1).get(0));
        } catch (Exception e) {
            System.out.println("No servers available");
        }
    }

    ArrayList<String> findServers(int howMany){
        ArrayList<String> servers = new ArrayList<>();
        Main.chatGraphics.log("<html><font face='arial' color='yellow'>Поиск серверов...</font></html>");
        Main.chatGraphics.msgInputTF.setEnabled(false);
        Socket newSocket;
        for (int i = 2; i < 36; i++) {
            if (servers.size() >= howMany)
                break;
            try {
                newSocket = new Socket();
                InetSocketAddress isa = new InetSocketAddress("192.168.1." + i, Main.DEFAULT_PORT);
                if(isa.isUnresolved())
                    continue;
                newSocket.connect(isa, 25);
                servers.add(newSocket.getInetAddress().getHostAddress());
            } catch (Exception e) {}
        }
        if (servers.size() == 0)
            Main.chatGraphics.log("<html><font face='arial' color='red'>Нет доступных серверов</font></html>");
        Main.chatGraphics.msgInputTF.setEnabled(true);
        return servers;
    }

    void disconnect() {
        if(connected) {
            try {
                Main.selfClient.output.println("LOGOUT");
            } catch (Exception e) {e.printStackTrace();}
            listener.listen = false;
            try {
                if (input != null) input.close();
            } catch (Exception e) {}
            try {
                if (output != null) output.close();
            } catch (Exception e) {}
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception e) {}
            connected = false;
            Main.chatGraphics.clearList();
        }
    }

    void setNickname(String name) {
        String color = Utils.getRandomRGBColorString();
        nickname = "<font face='WildWest' color='" + color + "'>" + name + "</font>";
    }

    String getNickname() {
        return nickname;
    }

    class ServerListener extends Thread {
        String msg;
        boolean listen = true;
        public void run(){
            while(listen){
                try {
                    msg = input.readLine();
                    if(msg == null)
                        break;
                    if (msg.equals("STOP")) {
                        disconnect();
                    } else if (msg.startsWith("NAME:")){
                        serverName = msg.substring(msg.indexOf(":") + 1);
                        Main.chatGraphics.log("<html><font face='arial' color='green'> Вы подключились к серверу " + serverName + "</font></html>");
                    }
                    else if(msg.startsWith("LIST:")){
                        Main.chatGraphics.changeList(msg);
                    }
                    else {
                        Main.chatGraphics.log(msg);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
