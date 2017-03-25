package client;
import java.net.*;
import java.util.ArrayList;

import static core.MainHandler.DEFAULT_PORT;
import static core.MainHandler.graphics;

/**
 * Created by nadir on 18.03.2017.
 */
public class Client {
    public String nickname = "";
    public Socket socket;
    public String serverIP; // TODO change it to InetAddress (Maybe not)

    public Client(){
        automaticallyConnectToServer();
    }

    public void automaticallyConnectToServer() {
        try {
            connectToServer(findServers(1, DEFAULT_PORT).get(0), DEFAULT_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectToServer(String ip, int port){
        try {
            serverIP = ip;
            socket = new Socket(serverIP, port);
            graphics.log("Подключен к серверу " + ip + ":" + port, false, false);
        } catch (Exception e) {
            e.printStackTrace();
            graphics.log("Невозможно поключиться к серверу", false, false);
        }
    }

    public ArrayList<String> findServers(int howMany, int port){
        ArrayList<String> servers = new ArrayList<>();
        graphics.log("Поиск серверов...", false, false);
        for (int i = 2; i < 100; i++) {
            if(servers.size() >= howMany)
                break;
            try {
                Socket newSocket = new Socket();
                InetSocketAddress isa = new InetSocketAddress("192.168.1." + i, port);
                graphics.msgInputTF.setEnabled(false);
                newSocket.connect(isa,50);
                servers.add(newSocket.getInetAddress().getHostAddress());
            } catch (Exception e){}
        }
        if(servers.size() == 0)
            graphics.log("Нет доступных серверов", false, false);
        graphics.msgInputTF.setEnabled(true);
        return servers;
    }
}
