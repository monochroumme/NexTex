import java.util.ArrayList;

/**
 * Created by nadir on 02.04.2017.
 */
public class Main {
    static Graphics graphics;
    static final int DEFAULT_PORT = 22869;
    static Client selfClient;
    static Server server;
    static boolean ownServer = false;
    static ArrayList<String> lastFoundServers = new ArrayList<>();

    public static void main(String[] args) {
        Main ch =  new Main();
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

        // debugging
    }
}
