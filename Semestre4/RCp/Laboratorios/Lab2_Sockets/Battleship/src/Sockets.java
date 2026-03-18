import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public final class Sockets {

    private Sockets() {}

    // =========================
    // CONNECTION SETUP
    // =========================

    public static void create_server() {
        
        // TODO
    }

    public static void wait_client() {

        // TODO
    }

    public static void connect_server(String serverAddr) {

        // TODO
    }

    // =========================
    // GENERIC SEND/RECEIVE
    // =========================

    public static void send_msg(String msg) {

        // TODO
    }

    public static String recv_msg() {

        // TODO
    }

    // =========================
    // GAME PROTOCOL
    // =========================

    public static void send_ready() {

        // TODO
    }

    public static void wait_ready() {

        // TODO
    }

    public static void send_shot(int x, int y) {

        // TODO
    }

    public static int[] wait_shot() {

        // TODO
    }

    public static void send_gameover(boolean status) {

        // TODO
    }

    public static boolean wait_gameover() {

        // TODO
    }

    public static void send_result(char res) {

        // TODO
    }

    public static char wait_result() {

        // TODO
    }
}
