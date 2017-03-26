package core;

import client.Client;
import server.Server;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by nadir on 19.03.2017.
 */
public class ChatCommands {
    private static Graphics graphics = MainHandler.graphics;
    private static Client selfClient = MainHandler.selfClient;

    public static boolean isCommand(String msg){
        if(msg.startsWith("/") && !msg.startsWith("//")){
            handleCommand(msg);
            return true;
        }
        else return false;
    }

    private static void handleCommand(String msg) {
        String[] command = msg.substring(1).split("\\s+");

        try {
            if (command[0].equals("set")) {
                try {
                    if (command[1].equals("nickname")) {
                        try {
                            String newNickname = command[2];
                            if (newNickname != null && newNickname.length() >= 3 && newNickname.length() <= 20) {
                                String lastNickname = selfClient.getNickname();
                                boolean nickBeforeWasNull = lastNickname.equals("");
                                if (!selfClient.getNickname().equals(newNickname)) {
                                    selfClient.setNickname(newNickname);
                                    if (nickBeforeWasNull)
                                        graphics.log("<html><font face='arial' color='yellow'>UNKNOWN изменил свой ник на </font>"
                                                + selfClient.getNickname() + "</html>", true, false, false);
                                    else
                                        graphics.log("<html>" + lastNickname + "<font face='arial' color='yellow'> изменил свой ник на </font>"
                                                + selfClient.getNickname() + "</html>", true, false, false);
                                }
                            } else {
                                graphics.log("<html><font face='verdana' color='red'>Слишком длинный/короткий ник (от 3 до 20)</font></html>", false, false, false);
                            }
                        } catch (Exception e) {
                            graphics.log("<html><font face='verdana' color='red'>Некорректный ник</font></html>", false, false, false);
                        }
                    }
                } catch (Exception e) {
                    graphics.log("<html><font face='verdana' color='red'>Неизветсная команда</font></html>", false, false, false);
                }
            return;
            } else if (containsOnly(command, "clear")) {
                graphics.clearChat();
                return;
            } else if(command[0].equals("server")) {
                if(containsOnly(command, "server")){
                    String help = "<html><font face='verdana' color='yellow'><br>Доступные команды:" +
                            "<br></font><font face='verdana' color='green'>/server start &lt;</font><font face='arial' color='white'>port</font><font face='verdana' color='green'>><font face='verdana' color='white'>" +
                            " - создать новый сервер с указанным портом и подключиться к нему" +
                            " Если написать без порта, то откроется сервер с дефолтным портом, к которому все клиенты будут подключаться автоматически при запуске этого приложения на их компьютере).</font>" +
                            "<br><font face='verdana' color='green'>/server connect &lt;</font><font face='arial' color='white'>IP</font><font face='verdana' color='green'>></font> &lt;" +
                            "<font face='arial' color='white'>port</font>>" +
                            "<font face='verdana' color='white'> - поключиться к серверу зная IP и порт. " +
                            "Если написать вместо IP и порта <any>, то приложение автоматически найдет сервер " +
                            "по дефолтному порту и подключиться к нему. Также можно не писать порт, тогда приложение зайдет на сервер на соответствующем IP с дефолтным портом.</font>" +
                            "<br><font face='verdana' color='green'>/server disconnect</font><font face='verdana' color='white'> - отключиться от текущего сервера</font>" +
                            "<br><font face='verdana' color='green'>/server find &lt;</font><font face='arial' color='white'>port</font><font face='verdana' color='green'>></font><font face='verdana' color='white'> - " +
                            "найти сервера по порту. Если не написать порт, то пойдет поиск по дефолтному порту.<br></font></html>";
                    graphics.log(help, false, false, false);
                    return;
                }

                try {
                    if(command[1].equals("find")){
                        int port;
                        try {
                            port = Integer.parseInt(command[2]);
                        } catch (Exception e){
                            port = MainHandler.DEFAULT_PORT;
                        }

                        try {
                            ArrayList<String> servers = MainHandler.selfClient.findServers(50, port);
                            if(servers.size() != 0) {
                                MainHandler.lastFoundServers.clear();
                                MainHandler.lastFoundServers.addAll(servers);
                                graphics.log("<html><font face='verdana' color='yellow'><br>Доступные сервера:<br></font><font face='arial' color='cyan'>" + showServers(servers) +
                                        "</font><font face='verdana' color='white'>Напишите </font><font face='arial' color='green'>/server connect i &lt;</font>" +
                                        "<font face='arial' color='white'>index</font><font face='arial' color='green'>></font><font face='verdana' color='white'> для подлючения к одному из " +
                                        "серверов</font></html>", false, false, false);
                            }
                        } catch (Exception e){
                            graphics.log("<html><font face='verdana' color='red'>Некорректный ввод порта</font></html>", false, false, false);
                        }
                        return;
                    }

                    if(MainHandler.ownServer) { // All next commands need to shut down own server for starting a new one, connecting to new one or disconnecting.
                        MainHandler.server.stopServer();
                        MainHandler.ownServer = false;
                    }

                    if(command[1].equals("start")){
                        if(command.length == 2){
                            MainHandler.server = new Server();
                            MainHandler.selfClient.connectToServer(InetAddress.getLocalHost().getHostAddress() + ":" + MainHandler.DEFAULT_PORT);
                            MainHandler.ownServer = true;
                            return;
                        }

                        try {
                            int port = Integer.parseInt(command[2]);
                            if (port > 1023 && port < 65536) {
                                MainHandler.server = new Server(port);
                                MainHandler.selfClient.connectToServer(InetAddress.getLocalHost().getHostAddress() + ":" + port);
                                MainHandler.ownServer = true;
                            }
                            else
                                graphics.log("<html><font face='verdana' color='red'>Некорректый порт, используйте числа от 1024 до 65535</font></html>", false, false, false);
                        } catch (Exception e) {
                            graphics.log("<html><font face='verdana' color='red'>Некорректый порт, используйте числа от 1024 до 65535</font></html>", false, false, false);
                        }
                        return;
                    }
                    else if(command[1].equals("connect")){
                        if(command[2].equals("any")){
                            MainHandler.selfClient.automaticallyConnectToServer();
                        } else if(command[2].equals("i")){
                            try {
                                int i = Integer.parseInt(command[3]) - 1;
                                String serverIP = MainHandler.lastFoundServers.get(i).substring(0, MainHandler.lastFoundServers.get(i).indexOf(":"));
                                int port = Integer.parseInt(MainHandler.lastFoundServers.get(i).substring(MainHandler.lastFoundServers.get(i).indexOf(":") + 1, MainHandler.lastFoundServers.get(i).length()));
                                MainHandler.selfClient.connectToServer(serverIP + ":" + port);
                            } catch (Exception e){
                                graphics.log("<html><font face='verdana' color='red'>Нет доступных серверов на данном индексе.</font></html>", false, false, false);
                            }
                        }
                        else {
                            try {
                                String ip = command[2];
                                int port;
                                try {
                                    port = Integer.parseInt(command[3]);
                                } catch (Exception e){
                                    port = MainHandler.DEFAULT_PORT;
                                }
                                MainHandler.selfClient.connectToServer(ip+ ":" + port);
                            } catch (Exception e){
                                graphics.log("<html><font face='verdana' color='red'>Некорректный IP или порт</font></html>", false, false, false);
                            }
                        }
                        return;
                    }
                    else if (command[1].equals("disconnect") && command.length == 2){
                        MainHandler.selfClient.socket.close();
                        graphics.log("<html><font face='verdana' color='yellow'>Отключен от сервера</font></html>", false, false, false);
                        return;
                    }

                    graphics.log("<html><font face='verdana' color='red'>Неизветсная команда</font></html>", false, false, false);
                } catch (Exception e){
                    graphics.log("<html><font face='verdana' color='red'>Неизветсная команда</font></html>", false, false, false);
                }
                return;
            }
            else if(containsOnly(command, "help")){
                String helpMsg = "<html><font face='verdana' color='yellow'>***<br>" +
                            "Вы на программе NexTex by </font><font face='verdana' color='#33cc33'>NexusGen</font><font face='verdana' color='white'>(v0.0.2)</font><br><font face='verdana' color='white'>" +
                            "Ваш ник: </font>" + (selfClient.getNickname().equals("") ? "<html><font face='verdana' color='red'>НЕ УКАЗАН</font>" : "<font face='arial' color='yellow'>" + selfClient.getNickname()) +
                            "</font><br><font face='verdana' color='white'>Ваш IP: </font><font face='arial' color='yellow'>" + InetAddress.getLocalHost().getHostAddress() + "</font><br>";
                try {
                    helpMsg = helpMsg.concat("<font face='verdana' color='white'>Подключение к серверу: </font>" + (!MainHandler.selfClient.socket.isClosed() ? "<font face='arial' color='yellow'>"
                            + MainHandler.selfClient.serverIP + "</font>" : "<font face='verdana' color='red'>НЕТ ПОДКЛЮЧЕНИЯ</font>")); // TODO потом сделать имя сервера
                } catch (Exception e){
                    helpMsg = helpMsg.concat("<font face='verdana' color='white'>Подключение к серверу: </font><font face='verdana' color='red'>НЕТ ПОДКЛЮЧЕНИЯ</font>");
                }
                helpMsg = helpMsg.concat("<font face='verdana' color='yellow'><br>Команды:<br></font><font face='verdana' color='green'>/set nickname &lt;</font><font face='arial' color='white'>nick</font>" +
                        "<font face='verdana' color='green'>&gt;</font>" +
                        "<font face='verdana' color='white'> - поставить ваш ник.</font>" +
                        "<br><font face='verdana' color='green'>/clear</font><font face='verdana' color='white'> - очистить чат.</font>" +
                        "<br><font face='verdana' color='green'>/server</font><font face='verdana' color='white'> - помощь по серверам</font>" +
                        "<br><font face='verdana' color='green'>/help</font><font face='verdana' color='white'> \uD83E\uDC50 вы тут.</font>" +
                        "<br><font face='verdana' color='white'>Команды, где есть </font><font face='arial' color='green'>&lt;</font><font face='arial' color='white'>ВСТАВКА</font><font face='verdana' color='green'>&gt;</font><font face='verdana' color='white'> писать без &lt;&gt; " +
                        "с соответстующей вставкой.</font>" +
                        "<br><font face='arial' color='#c0c0c0'><a href='https://nexusgen.wordpress.com/'>https://nexusgen.wordpress.com/</a></font>" +
                        "<br><font face='verdana' color='yellow'>***<br></font></html>");
                graphics.log(helpMsg, false,false, false);
                return;
            }

            graphics.log("<html><font face='verdana' color='red'>Неизвестная команда</font></html>", false, false, false);
        }
        catch (Exception e){
            graphics.log("<html><font face='verdana' color='red'>Неизвестная команда</font></html>", false, false, false);
        }
    }

    private static boolean containsOnly(String[] container, String only) {
        if(container.length > 1)
            return false;
        else if(container[0].equals(only))
            return true;
        return false;
    }

    private static String showServers(ArrayList<String> servers){
        int index = 0;
        String serversList = "<html>";
        for(String server : servers){
            index++;
            serversList = "<font face='arial' color='white'>" + serversList.concat(index + ") " + server + "<br>") + "</font>";
        }
        return serversList + "</html>";
    }
}