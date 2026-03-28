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

        String method = parts[0]; // ex: "GET", "POST", "DELETE"

        // Se o caminho for "/", serve o index.html por convenção
        //Caso contrário remove o "inicial" com substring(1)
        String path = parts[1].equals("/") ? "index.html" : parts[1].substring(1);

        // 2. Verificar se o método é GET (Requisito: 501 se não for GET)
        if (!method.equals("GET")) {
            sendResponse(out, "501 Not Implemented", "text/plain", "Método não suportado".getBytes());
            return;
        }

        // 3. Tentar ler o arquivo localmente, o caminho é relativo ao diretório onde o servidor foi executado
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            //Ficheiro encontrado - lê todos os bytes de uma vez
            byte[] fileContent = Files.readAllBytes(file.toPath());

            //Determina o Content-Type com base na extensão do ficheiro
            String contentType = getContentType(path);

            //envia resposta 200 OK com o conteúdo do ficheiro
            sendResponse(out, "200 OK", contentType, fileContent);
        } else {
            //Ficheiro não encontrado
            byte[] errorContent = "<h1>404 Not Found</h1>".getBytes();
            sendResponse(out, "404 Not Found", "text/html", errorContent);
        }
    }

    private static void sendResponse(OutputStream out, String status, String contentType, byte[] content) throws IOException {
        //PrintWriter premite escrever texto formatado no OutputStream
        PrintWriter writer = new PrintWriter(out);
        writer.println("HTTP/1.1 " + status); // //Linha de Status HTTP
        writer.println("Content-Type: " + contentType); //header, indica ao browser o tipo de conteúdo
        writer.println("Content-Length: " + content.length); //header, tamanho do corpo em bytes, permite ao browser
        //saber quando a resposta termina
        writer.println("Connection: close"); // informa que a conexão será encerrada
        writer.println(); // Linha em branco obrigatória entre headers e body
        writer.flush(); // envia os headers imediatamente

        // Escreve o corpo da reposta em bytes brutos, separado do PrintWriter para suportar conteudo binário
        out.write(content);
        out.flush();
    }

    private static String getContentType(String fileName) {
        //Verifica a extensão do ficheiro e retorna Media type correspondente
        if (fileName.endsWith(".html")) return "text/html"; // se o ficheiro terminar em .html devolve text/html
        //diz ao navegador que é uma página ‘web’ para renderizar
        if (fileName.endsWith(".css"))  return "text/css";//se terminar em css devolve text/css, diz que é uma folha de estilos
        if (fileName.endsWith(".js"))   return "application/javascript"; // se terminar em js, devolve text/js
        //diz que é codigo JavaScript
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg"; // se terminar em jpg ou jpeg
        //devolve imagem/jpeg, e extensão pode ser das duas formas ma so tipo é o mesmo por isso o || trata dos dois casos
        if (fileName.endsWith(".png"))  return "image/png";// Se terminar em png devolve imagem/png, diz ao browser que é uma imagem png
        //O browser fará download do ficheiro em vez de o tentar abrir
        return "application/octet-stream"; // Default conforme requisito
    }
}