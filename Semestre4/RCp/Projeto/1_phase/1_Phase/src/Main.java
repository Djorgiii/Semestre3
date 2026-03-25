import java.io.IOException;

/**
 * Entry point of the Web Server application.
 * Instantiates and starts the server.
 */
public class Main {

    public static void main(String[] args) {
        int port = 8080;
        WebServer server = new WebServer(port);

        server.start();
    }
}