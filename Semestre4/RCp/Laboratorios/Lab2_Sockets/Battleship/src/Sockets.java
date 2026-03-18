import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public final class Sockets {

    private Sockets() {}

    private static ServerSocket welcomeSocket;// Usado apenas pelo Servidor
    private static Socket connectionSocket;// A conexão real
    private static DataOutputStream outToOpponent;// Para enviar mensagens (Strings)
    private static BufferedReader inFromOpponent;// Para receber mensagens (Strings)
    private static final int PORT = 25565;// Escolha uma porta acima de 1024

    public static void create_server() {
        
        try{
            welcomeSocket  = new ServerSocket(PORT);
            System.out.println("Servidor a aguardar conexão na porta " + PORT + "...");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void wait_client() {

        try{
            connectionSocket = welcomeSocket.accept();
            outToOpponent = new DataOutputStream(connectionSocket.getOutputStream());
            inFromOpponent = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            System.out.println("Cliente conectado: "+ connectionSocket.getInetAddress());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void connect_server(String serverAddr) {

        try{
            connectionSocket = new Socket(serverAddr, PORT);
            outToOpponent = new DataOutputStream(connectionSocket.getOutputStream());
            inFromOpponent = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()))
        } catch (Exception e) {
            e.printStackTrace();
        }
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
