import java.io.*;
import java.net.*;
import java.nio.file.*;

public class WebServer {
    public static void main(String[] args) {

        //define a porta onde o servidor vai realizar conexões
        int port = 8080;

        //Cria um ServerSocket que fica à espera na porta 8080, o try garante que o socket é
        //fechado automaticamente mesmo que ocorra erro
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado na porta " + port);

            // Loop infinito para lidar com conexões ilimitadas, o servidor nunca para por conta prórpia
            while (true) {
                //serverSocket.accept(), bloqueia aqui até um cliente se conectar, quando isso acontece
                //retorna um socket dedicado a essa conexão
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket);
                } catch (IOException e) {
                    // se houver erro numa conexão especifica, não mata o servidor, apenas imprime o erro
                    // e volta ao inicio
                    System.err.println("Erro ao processar conexão: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // erro quando se inica o servidor, ou seja, o servidor não conseguiu arrancar
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private static void handleRequest(Socket socket) throws IOException {
        //cria um leitor de texto para ler o que o cliente enviou
        //InputStreamReader, converte os bytes do socket em caracteres
        //Buffer-leitura eficiente linha a linha
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //OutputStream raw para enviar bytes ao cliente, usado para o corpo da resposta
        OutputStream out = socket.getOutputStream();

        // 1. Ler a primeira linha (Request Line)
        String requestLine = in.readLine();
        //se a linha for nula, o cliente fechou a conexão sem enviar nada
        if (requestLine == null) return;

        System.out.println("Requisição: " + requestLine);
        //divide a linha em partes separadas por espaço
        String[] parts = requestLine.split(" ");
        //se a linha não tiver pelos menos um método e caminho ignora
        if (parts.length < 2) return;

        String method = parts[0];
        
        String path = parts[1].equals("/") ? "index.html" : parts[1].substring(1);

        // 2. Verificar se o método é GET (Requisito: 501 se não for GET)
        if (!method.equals("GET")) {
            sendResponse(out, "501 Not Implemented", "text/plain", "Método não suportado".getBytes());
            return;
        }

        // 3. Tentar ler o arquivo localmente
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String contentType = getContentType(path);
            sendResponse(out, "200 OK", contentType, fileContent);
        } else {
            byte[] errorContent = "<h1>404 Not Found</h1>".getBytes();
            sendResponse(out, "404 Not Found", "text/html", errorContent);
        }
    }

    private static void sendResponse(OutputStream out, String status, String contentType, byte[] content) throws IOException {
        PrintWriter writer = new PrintWriter(out);
        writer.println("HTTP/1.1 " + status);
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + content.length);
        writer.println("Connection: close");
        writer.println(); // Linha em branco obrigatória entre headers e body
        writer.flush();

        out.write(content);
        out.flush();
    }

    private static String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css"))  return "text/css";
        if (fileName.endsWith(".js"))   return "application/javascript";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png"))  return "image/png";
        return "application/octet-stream"; // Default conforme requisito
    }
}