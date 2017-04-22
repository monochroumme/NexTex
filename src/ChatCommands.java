import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by nadir on 02.04.2017.
 */
class ChatCommands {
    static boolean isCommand(String msg){
        if(msg.startsWith("/")){
            handleCommand(msg);
            return true;
        }
        else return false;
    }

    private static void handleCommand(String msg) {
        String[] command = msg.substring(1).split("\\s+");

        try {
            if (Utils.containsOnly(command, "clear")) {
                Main.chatGraphics.clearChat();
                return;
            } else if(Utils.containsOnly(command, "help")){
                Main.chatGraphics.log("<html><font face='verdana' color='yellow'>***<br>Вы на программе NexTex by <font face='verdana' color='green'>NexusGen</font>(v1.0.13)</font><br>" +
                        "<font face='verdana' color='white'>Ваш ник: " + Main.selfClient.getNickname() + "<br>" +
                        "Ваш IP: <font face='arial' color='yellow'>" + InetAddress.getLocalHost().getHostAddress() + "</font><br>" +
                        "Подключен к серверу: " + (Main.selfClient.connected ? Main.selfClient.serverName : "<font face='verdana' color='red'>НЕТ ПОДКЛЮЧЕНИЯ</font>") + "<br>" +
                        "<font face='verdana' color='yellow'>Общие команды: </font><br><font face='verdana' color='white'>" +
                        "<font face='arial' color='green'>/set nickname &lt;<font face='arial' color='white'>nick</font>&gt;</font> - поставить ваш ник<br>" +
                        "<font face='arial' color='green'>/clear</font> - очистить чат<br>" +
                        "<font face='arial' color='green'>/server</font> - помощь по серверам<br>" +
                        "<font face='arial' color='green'>/help</font> \uD83E\uDC50 вы тут<br>" +
                        "Команды, где есть <font face='arial' color='green'>&lt;</font>ВСТАВКА<font face='verdana' color='green'>&gt;</font> писать без <font face='verdana' color='green'>&lt;&gt;</font> с соответстующей вставкой.<br>" +
                        "<font face='arial' color='#c0c0c0'><a href='https://nexusgen.wordpress.com/'>https://nexusgen.wordpress.com/</a></font><br>" +
                        "<font face='verdana' color='yellow'>***</font></font></html>");
                return;
            } else if(Utils.containsOnly(command, "sendip")){
                Main.selfClient.sendMessage(InetAddress.getLocalHost().getHostAddress());
                return;
            }
            else if (command[0].equalsIgnoreCase("set")) {
                if (command[1].equalsIgnoreCase("nickname")) {
                    try {
                        if(command.length == 3){
                            String newNickname = command[2];
                            if (newNickname != null && newNickname.length() >= 1 && newNickname.length() <= 16) {
                                if (!Main.selfClient.getNickname().equals(newNickname)) {
                                    Main.selfClient.setNickname(newNickname);
                                    if(Main.selfClient.connected){
                                        if(Main.ownServer)
                                            Main.chatGraphics.log("<html><font face='arial' color='red'>Админ не может менять свой ник внутри сервера.</font></html>");
                                        else {
                                            Main.selfClient.output.println("NEWNICK/::/" + Main.selfClient.getNickname());
                                        }
                                    }
                                    else {
                                        Main.chatGraphics.log("<html><font face='arial' color='green'>Вы изменили свой ник на " + Main.selfClient.getNickname() + "</font></html>");
                                    }
                                }
                            } else {
                                Main.chatGraphics.log("<html><font face='arial' color='red'>Слишком длинный/короткий ник (от 1 до 20)</font></html>");
                            }
                        } else if(command.length == 4 && Main.ownServer && command[2].equalsIgnoreCase("server")){
                            String newServerName = "<font face='WildWest' color='" + Utils.getRandomRGBColorString() + "'>" + command[3] + "</font>";
                            System.out.println(newServerName);
                            Main.server.changeServerName(newServerName);
                        }
                    } catch (Exception e) {
                        Main.chatGraphics.log("<html><font face='verdana' color='red'>Некорректный ник</font></html>");
                    }
                }
                return;
            } else if (command[0].equalsIgnoreCase("server")) {
                if (Utils.containsOnly(command, "server")) {
                    Main.chatGraphics.log("<html><font face='verdana' color='yellow'>***<br>Доступные команды:<br><font face='verdana' color='white'>" +
                            "<font face='arial' color='green'>/server start</font> - создать новый сервер и подключиться к нему.<br>" +
                            "<font face='arial' color='green'>/server connect &lt;<font face='arial' color='white'>IP</font>&gt;</font> - поключиться к серверу зная IP. " +
                            "Если написать вместо IP <font face='arial' color='green'>any</font>, то автоматически найдется сервер и вы подключитесь к нему.<br>" +
                            "<font face='arial' color='green'>/server disconnect</font> - отключиться от текущего сервера. Если вы админ сервера, то сервер выключится.<br>" +
                            "<font face='arial' color='green'>/server find</font> - поиск доступных серверов.</font><br>" +
                            "<font face='verdana' color='yellow'>***</font></html>");
                    return;
                }

                if (command[1].equalsIgnoreCase("find")) {
                    try {
                        ArrayList<String> servers = Main.selfClient.findServers(50);
                        if (servers.size() != 0) {
                            Main.lastFoundServers.clear();
                            Main.lastFoundServers.addAll(servers);
                            Main.chatGraphics.log("<html><font face='arial' color='yellow'>Доступные сервера:<br></font><font face='arial' color='cyan'>" + showServers(servers) +
                                    "</font><font face='arial' color='white'>Напишите </font><font face='arial' color='green'>/server connect i &lt;</font>" +
                                    "<font face='arial' color='white'>index</font><font face='arial' color='green'>&gt;</font><font face='arial' color='white'> для подлючения к одному из " +
                                    "серверов</font></html>");
                        }
                    } catch (Exception e) {
                        System.out.println("Some problem with finding servers");
                    }
                    return;
                }

                if (Main.ownServer) { // All next commands need to shut down own server for starting a new one, connecting to new one or disconnecting.
                    Main.server.stop();
                    Main.ownServer = false;
                    Main.server = null;
                }

                if (command[1].equalsIgnoreCase("start")) {
                    if (command.length == 2) {
                        if (Main.server == null)
                            Main.server = new Server(Main.selfClient.getNickname());
                        else {
                            Main.server.reset(Main.selfClient.getNickname());
                        }

                        if (Main.server.working) {
                            Main.chatGraphics.log("<html><font face='arial' color='yellow'>Сервер с IP <font face='arial' color='white'>" + InetAddress.getLocalHost().getHostAddress() + "</font> открыт</font></html>");
                            Main.selfClient.connect(InetAddress.getLocalHost().getHostAddress());
                            Main.ownServer = true;
                        }
                    }
                    return;
                } else if (command[1].equalsIgnoreCase("connect")) {
                    if (command[2].equalsIgnoreCase("any")) {
                        Main.selfClient.automaticallyConnect();
                    } else if (command[2].equalsIgnoreCase("i")) {
                        try {
                            int i = Integer.parseInt(command[3]) - 1;
                            String serverIP = Main.lastFoundServers.get(i);
                            Main.selfClient.connect(serverIP);
                            return;
                        } catch (Exception e) {
                            Main.chatGraphics.log("<html><font face='arial' color='red'>Нет доступных серверов на данном индексе.</font></html>");
                            return;
                        }
                    } else {
                        try {
                            String ip = command[2];
                            Main.selfClient.connect(ip);
                        } catch (Exception e) {
                            Main.chatGraphics.log("<html><font face='arial' color='red'>Некорректный IP или порт</font></html>");
                        }
                    }
                    return;
                } else if (command[1].equals("disconnect") && command.length == 2) {
                    Main.selfClient.disconnect();
                    return;
                }
            }
        } catch (Exception e){
            Main.chatGraphics.log("<html><font face='arial' color='red'>Неизветсная команда</font></html>");
        }

        Main.chatGraphics.log("<html><font face='arial' color='red'>Неизветсная команда</font></html>");
    }

    private static String showServers(ArrayList<String> servers){
        int index = 0;
        String serversList = "";
        for(String server : servers){
            index++;
            serversList = "<font face='arial' color='white'>" + serversList.concat(index + ") " + server + "<br>") + "</font>";
        }
        return serversList;
    }
}
