/**
 * Created by nadir on 02.04.2017.
 */
class ChatCommands {
    static boolean isCommand(String msg){
        if(msg.startsWith("/") && !msg.startsWith("//")){
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
            } else if (Utils.containsOnly(command, "help")) {
                Main.chatGraphics.log("<font face='verdana' color='yellow'>***<br>You're in program NexTex made by <font face='verdana' color='green'>rAre (NexusGen)</font> (v1.2.17)</font><br>" +
                        "<font face='verdana' color='white'>Your nickname: " + Main.selfClient.getNickname() + "<br>" +
                        "Your IP: <font face='arial' color='yellow'>" + Utils.getIP() + "</font><br>" +
                        "Connected to server: " + (Main.selfClient.connected ? Main.selfClient.serverName : "<font face='verdana' color='red'>NO CONNECTION</font>") + "<br>" +
                        "<font face='verdana' color='yellow'>Main commands: </font><br><font face='verdana' color='white'>" +
                        "<font face='arial' color='green'>/set nickname &lt;<font face='arial' color='white'>nick</font>&gt;</font> - change your nickname<br>" +
                        "<font face='arial' color='green'>/clear</font> - clear chat<br>" +
                        "<font face='arial' color='green'>/server</font> - help on servers<br>" +
                        "<font face='arial' color='green'>/help</font> \uD83E\uDC50 you're here<br>" +
                        "<font face='arial' color='green'>/sendip</font> - send your local IP<br>" +
                        "Commands which have <font face='arial' color='green'>&lt;</font>INSERT<font face='verdana' color='green'>&gt;</font> write w/o <font face='verdana' color='green'>&lt;&gt;</font>" +
                        " with a corresponding insert.<br>" +
                        "<font face='arial' color='#c0c0c0'><a href='https://nexusgen.wordpress.com/'>https://nexusgen.wordpress.com/</a></font><br>" +
                        "<font face='verdana' color='yellow'>***</font></font>");
                return;
            } else if (Utils.containsOnly(command, "sendip")) {
                Main.selfClient.sendMessage(Utils.getIP());
                return;
            } else if (command[0].equals("set")) {
                if (command.length >= 3) {
                    if (command[1].equals("nickname")) {
                        try {
                            if (command[2] != null && command[2].length() <= 24 && !Utils.containsChars(command[2], new String[]{"<", ">", ":", ";"})) {
                                Main.selfClient.setNickname(msg.substring(msg.indexOf("nickname") + 9));
                                if (Main.selfClient.connected) {
                                    Main.selfClient.output.println("NEWNICK/::/" + Main.selfClient.getNickname());
                                } else {
                                    Main.chatGraphics.log("<font face='arial' color='green'>You've changed your nickname to " + Main.selfClient.getNickname() + "</font>");
                                }
                            } else {
                                Main.chatGraphics.log("<font face='arial' color='red'>Nickname shouldn't contain any special symbols such as: &#60; > : ;" +
                                        "<br>Maximal length of nickname is 24, please try again.</font>");
                            }
                        } catch (Exception e) {
                            Main.chatGraphics.log("<font face='verdana' color='red'>Incorrect nickname</font>");
                        }
                    } else if (command[1].equals("server")) {
                        if (Main.ownServer && Main.server.working) {
                            Main.server.changeServerName("<font face='arial' color='" + Utils.getRandomRGBColorString() + "'>" + msg.substring(msg.indexOf("server") + 7) + "</font>");
                        } else {
                            Main.chatGraphics.log("<font face='arial' color='red'>Invalid command</font>");
                        }
                    }
                }
                return;
            } else if (command[0].equals("server")) {
                if (command.length == 1) { // means only /server
                    Main.chatGraphics.log("<font face='verdana' color='yellow'>***<br>Server commands:<br><font face='verdana' color='white'>" +
                            "<font face='arial' color='green'>/server start</font> - create a server and connect to it.<br>" +
                            "<font face='arial' color='green'>/server connect &lt;<font face='arial' color='white'>IP</font>&gt;</font> - connect to the server if you know its IP. " +
                            "If you write instead of IP <font face='arial' color='green'>any</font> then a server will be automatically found and you will automatically connect to it.<br>" +
                            "<font face='arial' color='green'>/server disconnect</font> - disconnect from the current server. If you are the admin then the server will shut down.<br>" +
                            "<font face='arial' color='green'>/server find</font> - search for available servers.<br>" +
                            "<font face='arial' color='green'>/server kick &lt;</font>NICKNAME<font face='arial' color='green'>&gt;</font> - kick a user. If you don't want to " +
                            "write the full nickname of the user then you can write a part of the nickname and add >> which will autocomplete the nickname or add &lt;Part of the nickname> " +
                            "to search for nicknames containing those symbols (Note that autocompleter starts to search for the nickname from the top of the list, so if another " +
                            "nickname had those symbols in the beginning then that user will be kicked) (ONLY FOR ADMIN/MODERATORS)<br>" +
                            "<font face='arial' color='green'>/server ban/unban &lt;</font>NICKNAME<font face='arial' color='green'>&gt;</font>- ban a user. When you unban you can also" +
                            " use an index from blacklist or write the IP of the user you want to unban (Take into account the same note) (ONLY FOR ADMIN/MODERATORS)<br>" +
                            "<font face='arial' color='green'>/server mod/unmod &lt;</font>NICKNAME<font face='arial' color='green'>&gt;</font> - make a user moderator " +
                            "(Take into account the same note) (ONLY FOR ADMIN)<br>" +
                            "<font face='arial' color='green'>/server blacklist</font> - show the blacklist of banned users (ONLY FOR ADMIN/MODERATORS)<br>" +
                            "<font face='arial' color='green'>/set server &lt;</font>name<font face='arial' color='green'>&gt;</font> - change the name of the server.</font><br>" +
                            "<font face='verdana' color='yellow'>***</font>");
                    return;
                } else if (command.length == 2) {
                    if (command[1].equals("find")) {
                        Main.selfClient.findServers(128, false); // will find and show servers in the chat
                        return;
                    } else if (command[1].equals("start")) {
                        if (Main.server == null) {
                            Main.server = new Server(Main.selfClient.getNickname());
                            if (Main.server.working) {
                                Main.chatGraphics.log("<font face='arial' color='yellow'>Server <font face='arial' color='white'>" + Utils.getIP() + "</font> has been opened</font>");
                                Main.selfClient.connect(Utils.getIP());
                                Main.ownServer = true;
                            }
                        } else if (!Main.server.working) {
                            Main.server.reset(Main.selfClient.getNickname());
                            Main.selfClient.connect(Utils.getIP());
                            Main.ownServer = true;
                        } else {
                            Main.chatGraphics.log("<font face='arial' color='red'>The server is already open</font>");
                        }
                        return;
                    } else if (command[1].equals("disconnect")) {
                        if (Main.selfClient.connected) {
                            Main.selfClient.disconnect();
                            Main.chatGraphics.log("<font face='arial' color='yellow'>You've been disconnected from </font>" + Main.selfClient.serverName);
                        } else {
                            Main.chatGraphics.log("<font face='arial' color='red'>You're not connected to any server</font>");
                        }
                        return;
                    } else if (command[1].equals("blacklist") && Main.selfClient.connected){
                        if (Main.ownServer || Main.selfClient.moderator) {
                            Main.selfClient.output.println("BLACKLIST");
                        }
                        return;
                    }
                } else if (command[1].equals("mod") && Main.ownServer && Main.selfClient.connected) { // ONLY FOR ADMIN
                    modUnmodKickBan("mod", msg);
                    return;
                } else if (command[1].equals("unmod") && Main.ownServer && Main.selfClient.connected) { // ONLY FOR ADMIN
                    modUnmodKickBan("unmod", msg);
                    return;
                } else if (command[1].equals("kick") && Main.selfClient.connected) {
                    modUnmodKickBan("kick", msg);
                    return;
                } else if (command[1].equals("ban") && Main.selfClient.connected) {
                    modUnmodKickBan("ban", msg);
                    return;
                } else if (command[1].equals("unban") && Main.selfClient.connected) {
                    if (Main.ownServer || Main.selfClient.moderator) {
                        Main.selfClient.output.println("UNBAN/::/" + msg.substring(msg.indexOf("unban") + 6));
                    } else Main.chatGraphics.log("<font face='arial' color='red'>To be able to unban someone you must be the admin or a moderator</font>");
                    return;
                } else if (command[1].equals("connect")) {
                    Main.selfClient.disconnect();
                    if (command.length == 3) {
                        if(command[2].equals("any"))
                            Main.selfClient.automaticallyConnect();
                        else {
                            try {
                                String ip = command[2];
                                Main.selfClient.connect(ip);
                            } catch (Exception e) {
                                Main.chatGraphics.log("<font face='arial' color='red'>Incorrect IP</font>");
                            }
                        }
                        return;
                    } else if (command[2].equals("i") && command.length == 4) {
                        try {
                            int i = Integer.parseInt(command[3]) - 1;
                            String serverIP = Main.selfClient.lastFoundServers.get(i).substring(0, Main.selfClient.lastFoundServers.get(i).indexOf(':'));
                            Main.selfClient.connect(serverIP);
                        } catch (Exception e) {
                            Main.chatGraphics.log("<font face='arial' color='red'>No available servers with this index.</font>");
                        }
                        return;
                    }
                    Main.chatGraphics.log("<font face='arial' color='red'>Invalid command</font>");
                    return;
                }
            }
        } catch(Exception e){
            Main.chatGraphics.log("<font face='arial' color='red'>Invalid command</font>");
            return;
        }

        Main.chatGraphics.log("<font face='arial' color='red'>Invalid command</font>");
    }

    private static void modUnmodKickBan(String modUnmodKickBan, String msg){
        int i = modUnmodKickBan.length() + 1;
        String fullNickname = Utils.getNickname(msg.substring(msg.indexOf(modUnmodKickBan) + i));
        if (Main.ownServer || Main.selfClient.moderator) {
            if (fullNickname.equals(""))
                Main.chatGraphics.log("<font face='arial' color='red'>No one with that nickname</font>");
            else
                Main.selfClient.output.println(modUnmodKickBan.toUpperCase() + "/::/" + fullNickname);
        } else
            Main.chatGraphics.log("<font face='arial' color='red'>To be able to " + modUnmodKickBan + " someone you must be the admin or a moderator</font>");
    }
}
