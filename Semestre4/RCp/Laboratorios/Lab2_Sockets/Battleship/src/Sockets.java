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
            inFromOpponent = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // GENERIC SEND/RECEIVE
    // =========================

    public static void send_msg(String msg) {

        try {
            // send the message followed by a newline so the receiver can use readLine
            outToOpponent.writeBytes(msg + "\n");
            outToOpponent.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String recv_msg() {

        try {
            String line = inFromOpponent.readLine();
            if (line == null) return null;
            return line.trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================
    // GAME PROTOCOL
    // =========================

    public static void send_ready() {

        send_msg("Ready");

    }

    public static void wait_ready() {
        while(true){
            String msg = recv_msg();
            if (msg == null) continue;
            if(msg.equals("Ready")){
                return;
            }
        }

    }

    public static void send_shot(int x, int y) {
        send_msg("Shot" + x + " " + y);

    }

    public static int[] wait_shot() {
        while (true) {
            String msg = recv_msg();
            if (msg == null) continue;
            String[] parts = msg.split("\\s+");

            // Expecting "SHOT", "x", "y"
            if (parts.length >= 3 && parts[0].equals("SHOT")) {
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    return new int[]{x, y};
                } catch (NumberFormatException e) {
                    // Log error or ignore malformed coordinates
                }
            }
        }

    }

    public static void send_gameover(boolean status) {

        send_msg("GAMEOVER " + (status ? "1" : "0"));
    }

    public static boolean wait_gameover() {

        while (true) {
            String msg = recv_msg();
            if (msg == null) continue;
            String[] parts = msg.split("\\s+");
            if (parts.length >= 2 && parts[0].equals("GAMEOVER")) {
                return parts[1].equals("1") || parts[1].equalsIgnoreCase("true");
            }
            // ignore other messages
        }
    }

    public static void send_result(char res) {

        send_msg("RESULT " + res);
    }

    public static char wait_result() {

        while (true) {
            String msg = recv_msg();
            if (msg == null) continue;
            String[] parts = msg.split("\\s+");
            if (parts.length >= 2 && parts[0].equals("RESULT")) {
                return parts[1].charAt(0);
            }
            // ignore other messages
        }
    }
}