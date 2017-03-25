package core;

import client.Client;
import server.Server;

/**
 * Created by nadir on 11.03.2017.
 */
public class MainHandler {
    public static Graphics graphics;
    public static Client selfClient;
    public static final int DEFAULT_PORT = 22869;
    public static Server server;
    public static boolean ownServer = false;

    public static void main(String[] args) {
        MainHandler ch =  new MainHandler();
        try {
            ch.antistatic();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void antistatic() {
        start();
    }

    void start() {
        graphics = new Graphics();
        selfClient = new Client();
    }
}
