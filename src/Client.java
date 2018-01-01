import javax.swing.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadir on 02.04.2017.
 */
public class Client {
    private String nickname;
    private ServerListener listener;
    boolean connected = false;
    boolean moderator = false;

    Socket socket;
    String serverName;
    BufferedReader input;
    PrintWriter output;
    ArrayList<String> lastFoundServers = new ArrayList<>();

    void connect(String serverIP) {
        if(connected)
            disconnect();
        try {
            socket = new Socket(serverIP, Main.DEFAULT_PORT);
            connected = true;
        } catch (Exception e) {
            System.out.println("Error connecting to server " + serverIP);
            Main.chatGraphics.log("<font face='arial' color='red'>No servers on that IP</font>");
            return;
        }

        System.out.println("Connected to server " + serverIP);

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println("Error creating input/output things");
            disconnect();
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
                Main.chatGraphics.log("<font face='arial' color='red'>Firstly connect to a server. If you don't know how then write <font face='arial' color='green'>/help</font></font>");
            } else output.println("MESSAGE/::/" + nickname + "<font face='verdana' color='white'>: " + msg + "</font>");
        } catch (Exception e) {
            System.out.println("Error writing to server");
        }
    }

    void automaticallyConnect(){
        try {
            findServers(1, true);
        } catch (Exception e) {
            System.out.println("No servers available");
        }
    }

    void findServers(int howMany, boolean autoConnect){
        Main.chatGraphics.log("<font face='arial' color='yellow'>Searching for servers...</font>");
        Main.chatGraphics.msgInputTF.setHorizontalAlignment(JTextField.CENTER);
        Main.chatGraphics.msgInputTF.setEnabled(false);
        new SwingWorker<List<String>, Void>() {
            @Override
            public List<String> doInBackground() {
                Main.chatGraphics.msgInputTF.setText("Wait..."); // SHOULDN'T BE HERE, BUT IT DOESN'T WORK THE OTHER WAY
                List<String> servers = new ArrayList<>();
                Socket newSocket;
                for (int i = 2; i < 254; i++) {
                    if (servers.size() >= howMany) {
                        break;
                    }

                    try {
                        newSocket = new Socket();
                        InetSocketAddress isa = new InetSocketAddress(Main.DEFAULT_SEARCH_IP + i, Main.DEFAULT_PORT);
                        if (isa.isUnresolved())
                            continue;
                        newSocket.connect(isa, 5);
                        BufferedReader br = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
                        String name = br.readLine();
                        servers.add(newSocket.getInetAddress().getHostAddress() + ":" + (name.substring(name.indexOf(':') + 1)));
                        PrintWriter pw = new PrintWriter(newSocket.getOutputStream(), true);
                        pw.println("LOGOUT");
                        newSocket.close();
                        br.close();
                        pw.close();
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }
                return servers;
            }

            @Override
            public void done() {
                try {
                    List<String> servers = get();

                    Main.chatGraphics.msgInputTF.setEnabled(true);
                    Main.chatGraphics.msgInputTF.setText("");
                    Main.chatGraphics.msgInputTF.setHorizontalAlignment(JTextField.LEFT);
                    Main.chatGraphics.msgInputTF.grabFocus();

                    if (servers.size() > 0) {
                        if (autoConnect) {
                            connect(servers.get(0).substring(0, servers.get(0).indexOf(':')));
                        } else {
                            Main.selfClient.lastFoundServers.clear();
                            Main.selfClient.lastFoundServers.addAll(servers);
                            String serversList = "";
                            for (int i = 0; i < servers.size(); i++) {
                                serversList = "<font face='arial' color='white'>" + serversList.concat((i + 1) + ") " + servers.get(i).substring(servers.get(i).indexOf(':') + 1) + "<br>") + "</font>";
                            }
                            Main.chatGraphics.log("<font face='arial' color='yellow'>Available servers:<br></font><font face='arial' color='cyan'>" + serversList +
                                    "</font><font face='arial' color='white'>Write </font><font face='arial' color='green'>/server connect i &lt;</font>" +
                                    "<font face='arial' color='white'>INDEX</font><font face='arial' color='green'>&gt;</font><font face='arial' color='white'> to connect to a server from the list </font>");
                        }
                    } else {
                        Main.chatGraphics.log("<font face='arial' color='red'>No available servers</font>");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    void disconnect() {
        if (Main.ownServer) {
            if (Main.server.working)
                Main.server.stop();
            Main.ownServer = false;
        }
        if(connected) {
            connected = false;
            System.out.println("Disconnected from " + socket.getInetAddress().toString().substring(1));
            try {
                Main.selfClient.output.println("LOGOUT");
            } catch (Exception e) {}
            listener.listen = false;
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception e) {}
            Main.chatGraphics.clearList();
            Main.chatGraphics.connectionL.setText("<html><font face='arial' color='red'>NO CONNECTION</font></html>");
        }
    }

    void setNickname(String name) {
        nickname = "<font face='arial' color='" + Utils.getRandomRGBColorString() + "'>" + name + "</font>";
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
                    if(msg == null || !listen)
                        break;
                    if (msg.equals("STOP")) {
                        disconnect();
                        if(!Main.ownServer)
                            Main.chatGraphics.log("<font face='arial' color='yellow'>You've been disconnected from </font>" + Main.selfClient.serverName);
                        Main.chatGraphics.log("<font face='arial' color='red'>Server " + serverName + " has been shut down</font>");
                    } else if (msg.startsWith("NAME:")){
                        serverName = msg.substring(msg.indexOf(":") + 1);
                        Main.chatGraphics.log("<font face='arial' color='green'> You've been connected to " + serverName + "</font>");
                        Main.chatGraphics.connectionL.setText("<html><font face='arial' color='green'>Connected to </font>" + serverName + "</html>");
                    }
                    else if(msg.startsWith("LIST:")){
                        Main.chatGraphics.changeList(msg);
                    }
                    else if(msg.startsWith("NEWNAME:")){
                        serverName = msg.substring(msg.indexOf(":") + 1);
                        Main.chatGraphics.connectionL.setText("<html><font face='arial' color='green'>Connected to </font>" + serverName + "</html>");
                    }
                    else if (msg.startsWith("BLACKLIST:")){
                        String[] blacklist = msg.split(":");
                        Main.chatGraphics.log("<font face='arial' color='yellow'>Banned users' IPs:</font>");
                        if(blacklist.length == 1)
                            Main.chatGraphics.log("<font face='arial' color='red'>No banned users</font>");
                        else {
                            for (int i = 1; i < blacklist.length; i++)
                                Main.chatGraphics.log("<font face='arial' color='white'>" + i + ") </font><font face='arial' color='red'>" + blacklist[i] + "</font>");
                            Main.chatGraphics.log("<font face='arial' color='white'>If you're admin/moderator you can unban users using " +
                                    "<font face='arial' color='green'>/server unban &lt;<font face='arial' color='white'>INDEX</font>></font>");
                        }
                    }
                    else if (msg.equals("MOD")){
                        moderator = true;
                    }
                    else if (msg.equals("UNMOD")) {
                        moderator = false;
                    }
                    else if (msg.equals("KICK")) {
                        disconnect();
                        Main.chatGraphics.log("<font face='arial' color='red'>You've been kicked from </font>" + serverName);
                    }
                    else if (msg.equals("BANN")) {
                        disconnect();
                        Main.chatGraphics.log("<font face='arial' color='red'>You've been banned from </font>" + serverName);
                    }
                    else if (msg.equals("DISCONNECT")){
                        disconnect();
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
