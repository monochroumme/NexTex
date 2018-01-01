/**
 * Created by nadir on 02.04.2017.
 */
public class Main {
    static ChatGraphics chatGraphics;
    static final int DEFAULT_PORT = 22869;
    static final String DEFAULT_SEARCH_IP = Utils.getSearchIP();
    static Client selfClient;
    static Server server;
    static boolean ownServer = false;

    public static void main(String[] args) {
        chatGraphics = new ChatGraphics();
        selfClient = new Client();
    }
}
