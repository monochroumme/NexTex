package client;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

import static core.MainHandler.DEFAULT_PORT;
import static core.MainHandler.graphics;

/**
 * Created by nadir on 18.03.2017.
 */
public class Client {
    private String nickname = "";
    public Socket socket;
    public String serverIP;

    public Client(){
        automaticallyConnectToServer();
    }

    public void automaticallyConnectToServer() {
        try {
            connectToServer(findServers(1, DEFAULT_PORT).get(0));
        } catch (Exception e) {
            System.out.println("No servers available");
        }
    }

    public void connectToServer(String ip){
        try {
            serverIP = ip.substring(0, ip.indexOf(":"));
            int port = Integer.parseInt(ip.substring(ip.indexOf(":") + 1, ip.length()));
            socket = new Socket(serverIP, port);
            graphics.log("<html><font face='verdana' color='green'>Подключен к серверу </font><font face='arial' color='white'>" + ip + "</font></html>", false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
            graphics.log("<html><font face='verdana' color='red'>Невозможно поключиться к серверу</font></html>", false, false, false);
        }
    }

    public ArrayList<String> findServers(int howMany, int port){
        ArrayList<String> servers = new ArrayList<>();
        graphics.log("<html><font face='verdana' color='yellow'>Поиск серверов...</font></html>", false, false, false);
        graphics.msgInputTF.setEnabled(false);
        for (int i = 2; i < 100; i++) {
            if(servers.size() >= howMany)
                break;
            try {
                Socket newSocket = new Socket();
                InetSocketAddress isa = new InetSocketAddress("192.168.1." + i, port);
                newSocket.connect(isa,50);
                servers.add(newSocket.getInetAddress().getHostAddress() + ":" + newSocket.getPort());
            } catch (Exception e){}
        }
        if(servers.size() == 0)
            graphics.log("<html><font face='verdana' color='red'>Нет доступных серверов</font></html>", false, false, false);
        graphics.msgInputTF.setEnabled(true);
        return servers;
    }

    public void setNickname(String name){
        String[] colors = {"white", "#33cc33", "#33cccc", "#cc3333", "#cc33a6", "#804000", "yellow", "#cc8033", "#66b3ff", "#6666ff", "#8c66ff", "#ff66ff", "#66ff66", "#66ffb3", "#ff8000", "#00ccff", "#6633ff"};
        String color = colors[new Random().nextInt(colors.length)];
        nickname = "<font face='georgia' color='" + color + "'>" + name + "</font>";
    }

    public String getNickname(){
        return nickname;
    }
}
