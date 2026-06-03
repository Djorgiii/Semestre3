package util;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


public class MyImage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path = null;
    private String mime = null; 
    private byte[] content = null;


    public static final String contexto = "src/main/webapp/";


    public MyImage() {}


    public MyImage(final String p) {
        this.path = p;
        this.mime = extrairExtensao(p);
        load();
    }


    public MyImage(final String p, final String m) {
        this.path = p;
        this.mime = m;
        load();
    }


    private String extrairExtensao(String p) {
        if (p != null && p.contains(".")) {
            return p.substring(p.lastIndexOf('.') + 1);
        }
        return "bin";
    }


    public boolean load() {
        if (path != null) {
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                try {
                    this.content = Files.readAllBytes(p);
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Erro ao ler ficheiro: " + path);
                }
            } else {
                System.err.println("🚫 Ficheiro não encontrado: " + path);
            }
        }
        return false;
    }


    public boolean isOk() {
        return content != null && content.length > 0;
    }

    public boolean save(String dataFile) {
        if (dataFile == null) dataFile = path;
        if (dataFile == null || content == null) return false;

        Path target = Paths.get(dataFile);

        if (Files.exists(target)) {
            String backupName = dataFile + "." + System.currentTimeMillis() + ".bak";
            try {
                Files.move(target, Paths.get(backupName));
                System.out.println("📦 Backup criado: " + backupName);
            } catch (IOException e) {
                System.err.println("⚠️ Não foi possível criar backup, a tentar gravar por cima...");
            }
        }
        try {
            Files.write(target, content);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Erro ao gravar imagem: " + e.getMessage());
            return false;
        }
    }


    public String getBase64() {
        return (content != null) ? Base64.getEncoder().encodeToString(content) : null;
    }


    public void setBase64(String base64) {
        if (base64 != null) {
            this.content = Base64.getDecoder().decode(base64);
        }
    }


    public void serializar(Socket socket) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject(this);
            out.flush();
        }
    }


    public void deserializar(Socket socket) throws ClassNotFoundException, IOException {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            MyImage temp = (MyImage) in.readObject();
            this.path = temp.path;
            this.mime = temp.mime;
            this.content = temp.content;
        }
    }


    public static String descarregar(String sourceUrl, String targetDirectory) throws IOException {
        URL url = URI.create(sourceUrl).toURL();
        
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        Path targetPath = Paths.get(targetDirectory, fileName);

        try (InputStream in = url.openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return targetPath.toString();
    }


    public void view() {
        if (content == null) {
            System.err.println("⚠️ Sem dados para visualizar!");
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Visualizador MyImage 🖼️");
            try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
                BufferedImage img = ImageIO.read(bais);
                if (img != null) {
                    JLabel label = new JLabel(new ImageIcon(img));
                    frame.add(label, BorderLayout.CENTER);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            } catch (IOException e) {
                System.err.println("❌ Erro ao renderizar imagem.");
            }
        });
    }


    public byte[] getContent() {
        return content != null ? content.clone() : null;
    }

    public void setContent(byte[] c) {
        this.content = c != null ? c.clone() : null;
    }

    public String getMime() { return mime; }
    public String getPath() { return path; }


    public static void menu() {
        Scanner sc = new Scanner(System.in);
        MyImage imgAtual = new MyImage();
        char op;

        do {
            System.out.println("\n" + "=".repeat(20));
            System.out.println("🌟 MENU MYIMAGE 🌟");
            System.out.println("1 – Carregar e Visualizar Imagem Local");
            System.out.println("2 – Testar Conversão Base64 (Texto)");
            System.out.println("3 – Descarregar Imagem de URL");
            System.out.println("4 – Guardar / Criar Backup");
            System.out.println("0 – Sair");
            System.out.print("👉 Escolha uma opção: ");

            String input = sc.nextLine();
            op = (input.length() > 0) ? input.charAt(0) : ' ';

            switch (op) {
                case '1':
                    System.out.print("📂 Caminho da imagem (ex: " + contexto + "foto.jpg): ");
                    String path = sc.nextLine();
                    imgAtual = new MyImage(path);
                    if (imgAtual.getContent() != null) imgAtual.view();
                    break;

                case '2':
                    if (imgAtual.getContent() == null) {
                        System.out.println("⚠️ Carrega primeiro uma imagem (Opção 1).");
                    } else {
                        String b64 = imgAtual.getBase64();
                        System.out.println("📝 Base64 (primeiros 50 chars): " + b64.substring(0, Math.min(50, b64.length())) + "...");
                        System.out.println("📏 Tamanho do texto: " + b64.length() + " caracteres.");
                    }
                    break;

                case '3':
                    System.out.print("🌐 URL da imagem: ");
                    String url = sc.nextLine();
                    try {
                        String destino = descarregar(url, contexto);
                        System.out.println("✅ Download concluído para: " + destino);
                        new MyImage(destino).view();
                    } catch (IOException e) {
                        System.err.println("❌ Erro no download: " + e.getMessage());
                    }
                    break;

                case '4':
                    if (imgAtual.getContent() == null) {
                        System.out.println("⚠️ Não há nada para guardar.");
                    } else {
                        System.out.print("💾 Nome do ficheiro para guardar: ");
                        String nome = sc.nextLine();
                        if (imgAtual.save(nome)) System.out.println("✅ Guardado com sucesso!");
                    }
                    break;

                case '0':
                    System.out.println("👋 Até à próxima!");
                    break;

                default:
                    System.out.println("❓ Opção inválida.");
            }
        } while (op != '0');
        
        sc.close();
    }


    public static void main(String[] args) {
        File pasta = new File(contexto);
        if (!pasta.exists()) pasta.mkdirs();

        menu();
    }
}