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
                                String lastNickname = selfClient.nickname;
                                boolean nickBeforeWasNull = lastNickname.equals("");
                                if (!selfClient.nickname.equals(newNickname)) {
                                    selfClient.nickname = newNickname;
                                    if (nickBeforeWasNull)
                                        graphics.log("UNKNOWN изменил свой ник на " + selfClient.nickname, true, false); // TODO Make colorful
                                    else
                                        graphics.log(lastNickname + " изменил свой ник на " + selfClient.nickname, true, false); // TODO Make colorful
                                }
                            } else {
                                graphics.log("Слишком длинный/короткий ник (от 3 до 20)", false, false); // TODO Make colorful
                            }
                        } catch (Exception e) {
                            graphics.log("Некорректный ник", false, false); // TODO Make colorful
                        }
                    }
                } catch (Exception e) {
                    graphics.log("Неизветсная команда", false, false); // TODO Make colorful
                }
            return;
            } else if (containsOnly(command, "clear")) {
                graphics.clearChat();
                return;
            } else if(command[0].equals("server")) {
                if(containsOnly(command, "server")){
                    String help = "\nДоступные команды:" +
                            "\n/server start <port> - создать новый сервер с указанным портом и подключиться к нему" +
                            " (возможно и без порта, тогда откроется сервер с дефолтным портом, к которому все клиенты будут подключаться автоматически при запуске этого приложения на их компьютере)." +
                            "\n/server connect <IP> <port> - поключиться к серверу зная IP и порт. Если написать вместо IP и порта <any>, то приложение автоматически найдет сервер " +
                            "по дефолтному порту и подключиться к нему. Также можно не писать порт, тогда приложение зайдет на сервер на соответствующем IP с дефолтным портом." +
                            "\n/server disconnect - отключиться от текущего сервера" +
                            "\n/server find <port> - найти сервера по порту. Если не написать порт, то пойдет поиск по дефолтному порту.\n";
                    graphics.log(help, false, false);
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
                            if(servers.size() != 0)
                                graphics.log("\nДоступные сервера:\n" + showServers(servers) + "Напишите /server connect <index> для подлючения к одному из серверов", false, false);
                        } catch (Exception e){
                            graphics.log("Некорректный ввод порта", false, false);
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
                            MainHandler.selfClient.connectToServer(InetAddress.getLocalHost().getHostAddress(), MainHandler.DEFAULT_PORT);
                            MainHandler.ownServer = true;
                            return;
                        }

                        try {
                            int port = Integer.parseInt(command[2]);
                            if (port > 1023 && port < 65536) {
                                MainHandler.server = new Server(port);
                                MainHandler.selfClient.connectToServer(InetAddress.getLocalHost().getHostAddress(), port);
                                MainHandler.ownServer = true;
                            }
                            else
                                graphics.log("Некорректый порт, используйте числа от 1024 до 65535", false, false); // TODO Make colorful
                        } catch (Exception e) {
                            graphics.log("Некорректый порт, используйте числа от 1024 до 65535", false, false); // TODO Make colorful
                        }
                        return;
                    }
                    else if(command[1].equals("connect")){
                        if(command[2].equals("any")){
                            MainHandler.selfClient.automaticallyConnectToServer();
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
                                MainHandler.selfClient.connectToServer(ip, port);
                            } catch (Exception e){
                                // TODO CONNECT TO SERVER BY INDEX
                                graphics.log("Некорректный IP или порт", false, false);
                            }
                        }
                        return;
                    }
                    else if (command[1].equals("disconnect") && command.length == 2){
                        MainHandler.selfClient.socket.close();
                        graphics.log("Отключен от сервера", false, false);
                        return;
                    }

                    graphics.log("Неизветсная команда", false, false); // TODO Make colorful
                } catch (Exception e){
                    graphics.log("Неизветсная команда", false, false); // TODO Make colorful
                }
                return;
            }
            else if(containsOnly(command, "help")){
                String helpMsg = "\n***\n" +
                            "Вы на программе NexTex by NexusGen." +
                            "\nВаш ник: " + (selfClient.nickname.equals("") ? "НЕ УКАЗАН" : selfClient.nickname) +
                            "\nВаш IP: " + InetAddress.getLocalHost().getHostAddress();
                try {
                    helpMsg = helpMsg.concat("\nПоключен к серверу: " + (!MainHandler.selfClient.socket.isClosed() ? MainHandler.selfClient.serverIP : "НЕТ ПОДКЛЮЧЕНИЯ")); // TODO потом сделать имя сервера
                } catch (Exception e){
                    helpMsg = helpMsg.concat("\nПоключен к серверу: НЕТ ПОДКЛЮЧЕНИЯ");
                }
                helpMsg = helpMsg.concat("\nКоманды:\n/set nickname <nick> - поставить ваш ник." +
                        "\n/clear - очистить чат." +
                        "\n/server - помощь по серверам" +
                        "\n/help \uD83E\uDC50 вы тут." +
                        "\nКоманды, где есть <ВСТАВКА> писать без <> с соответстующей вставкой." +
                        "\nhttps://nexusgen.wordpress.com/" +
                        "\n***\n");
                graphics.log(helpMsg, false,false);
                return;
            }

            graphics.log("Неизвестная команда", false, false); // TODO Make colorful
        }
        catch (Exception e){
            graphics.log("Неизвестная команда", false, false); // TODO Make colorful
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
        String serversList = "";
        for(String server : servers){
            index++;
            serversList = serversList.concat(index + ") " + server + "\n");
        }
        return serversList;
    }
}