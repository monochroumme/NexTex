import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by nadir on 02.04.2017.
 */
public class Client {
    private String nickname;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected = false;
    private ServerListener listener;

    void connect(String serverIP) {
        try {
            socket = new Socket(serverIP, Main.DEFAULT_PORT);
            connected = true;
        } catch (Exception e) {
            System.out.println("Error connecting to server " + serverIP);
            Main.graphics.log("<html><font face='arial' color='red'>Сервера на данном IP нет.</font></html>");
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
                Main.graphics.log("<html><font face='arial' color='red'>Сначала подключитесь к серверу. Если вы не знаете как, то напишите <font face='arial' color='green'>/help</font></font></html>");
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
        Main.graphics.log("<html><font face='arial' color='yellow'>Поиск серверов...</font></html>");
        Main.graphics.msgInputTF.setEnabled(false);
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
            Main.graphics.log("<html><font face='arial' color='red'>Нет доступных серверов</font></html>");
        Main.graphics.msgInputTF.setEnabled(true);
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
            //Main.graphics.log("<html><font face='arial' color='red'>Вы отключены от сервера.</font></html>");
        }
    }

    void setNickname(String name, boolean randomColor) {
        if (randomColor) {
            String[] colors = {"#33cc33", "#33cccc", "#cc3333", "#cc33a6", "#804000", "yellow", "#cc8033", "#66b3ff", "#6666ff", "#8c66ff", "#ff66ff", "#66ff66", "#66ffb3", "#ff8000", "#00ccff", "#6633ff"};
            String color = colors[new Random().nextInt(colors.length)];
            nickname = "<font face='georgia' color='" + color + "'>" + name + "</font>";
        } else {
            nickname = name;
        }
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
                    }
                    else {
                        Main.graphics.log(msg);
                    }
                } catch (Exception e){
                    break;
                }
            }
        }
    }
}
