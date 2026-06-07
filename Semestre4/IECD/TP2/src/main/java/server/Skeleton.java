package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import user.User;
import util.XMLDoc;

/**
 * Skeleton — lado servidor do padrão Stub/Skeleton.
 *
 * Contém os métodos estáticos que tratam cada tipo de pedido TCP recebido
 * pelo servidor: iniciar sessão, registar conta, alterar perfil, obter
 * tabuleiro e jogar.
 *
 * Cada método recebe o socket do cliente e o documento XML já parseado,
 * executa a lógica de negócio e envia a resposta XML de volta ao cliente.
 * Todos os pedidos e respostas são registados no protocolo.log.
 */
public class Skeleton {

    /**
     * Valida um documento XML recebido do cliente contra o schema do servidor.
     * Lança uma excepção com mensagem legível se a validação falhar.
     *
     * @param doc documento XML a validar
     * @throws Exception se o documento violar o schema metodos-srv.xsd
     */
    private static void validXSD(final Document doc) throws Exception {
        try {
            XMLDoc.validDocXSD(doc, XMLDoc.getContexto() + "metodos-srv.xsd");
        } catch (SAXException | IOException e) {
            throw new Exception("Recebeu mensagem inválida: " + e.getLocalizedMessage());
        }
    }

    /**
     * Lê a próxima mensagem XML do stream de entrada e valida-a.
     * Regista a mensagem no protocolo.log antes de a devolver.
     *
     * @param is stream de leitura do socket do cliente
     * @return documento XML parseado e validado
     * @throws Exception se a ligação for perdida ou a mensagem for inválida
     */
    private static Document getNext(BufferedReader is) throws Exception {
        String line = is.readLine();
        registaLog("Servidor{" + line + "}");
        Document d = XMLDoc.parseString(line);
        validXSD(d);
        return d;
    }

    /**
     * Trata um pedido de registo de nova conta.
     *
     * Verifica se o username já existe no users.xml. Se não existir,
     * cria um novo elemento <user> com todos os dados fornecidos,
     * grava o ficheiro com backup e responde com 'Sucesso'.
     *
     * @param sk         socket do cliente
     * @param docPedido  documento XML com o pedido <registar>
     */
    public static void runRegistar(Socket sk, Document docPedido) throws Exception {
        PrintWriter os = new PrintWriter(sk.getOutputStream(), true);
        Element regEl = getMethod(docPedido, "registar");

        // Extrair os atributos do pedido de registo
        String username = regEl.getAttribute("nickname");
        String senha    = regEl.getAttribute("senha");
        String first    = regEl.getAttribute("firstnames");
        String last     = regEl.getAttribute("lastnames");
        String gender   = regEl.getAttribute("gender");
        String birth    = regEl.getAttribute("birthdate");
        String nac      = regEl.getAttribute("nacionalidade");
        String foto     = regEl.getElementsByTagName("photography").item(0).getTextContent();

        String ficheiroUsers = XMLDoc.getContexto() + "users.xml";
        Document docUsers    = XMLDoc.parseFile(ficheiroUsers);

        // Verificar se o username já está em uso
        String xpathQuery = "//user[username='" + username + "']";
        NodeList users    = XMLDoc.getXPath(xpathQuery, docUsers);

        if (users != null && users.getLength() > 0) {
            System.out.println("❌ Erro: O username '" + username + "' já está registado!");
            os.println("<metodo><registar resposta='Erro: Username já existe'/></metodo>");
            return;
        }

        // Construir o novo elemento <user> com todos os campos obrigatórios
        Element root     = docUsers.getDocumentElement();
        Element novoUser = docUsers.createElement("user");

        addTag(docUsers, novoUser, "userid",      java.util.UUID.randomUUID().toString());
        addTag(docUsers, novoUser, "updated",     java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                                                    .format(java.time.format.DateTimeFormatter.ISO_INSTANT));
        addTag(docUsers, novoUser, "blocked",     "false");
        addTag(docUsers, novoUser, "profile",     "1");
        addTag(docUsers, novoUser, "username",    username);
        addTag(docUsers, novoUser, "firstnames",  first);
        addTag(docUsers, novoUser, "lastnames",   last);
        addTag(docUsers, novoUser, "email",       username + "@mail.pt");
        addTag(docUsers, novoUser, "gender",      gender);
        addTag(docUsers, novoUser, "birthdate",   birth);
        addTag(docUsers, novoUser, "photography", foto);
        addTag(docUsers, novoUser, "nationality", nac.toUpperCase());
        addTag(docUsers, novoUser, "password",    XMLDoc.SHA256(senha)); // senha guardada com hash SHA-256

        root.appendChild(novoUser);
        // Gravar com backup automático para preservação em caso de falha
        XMLDoc.gravarLock(docUsers, ficheiroUsers, XMLDoc.gerarNomeFBackupVersao(ficheiroUsers));

        User._load(); // recarregar a cache de utilizadores em memória

        System.out.println("✅ Utilizador '" + username + "' registado com sucesso!");
        os.println("<metodo><registar resposta='Sucesso'/></metodo>");
    }

    /**
     * Utilitário para criar e adicionar um elemento XML filho com conteúdo textual.
     *
     * @param doc    documento XML owner
     * @param parent elemento pai onde adicionar o filho
     * @param name   nome do elemento a criar
     * @param value  conteúdo textual do elemento
     */
    private static void addTag(Document doc, Element parent, String name, String value) {
        Element e = doc.createElement(name);
        e.setTextContent(value);
        parent.appendChild(e);
    }

    /**
     * Trata um pedido de alteração de perfil (senha e/ou fotografia).
     *
     * Localiza o utilizador no users.xml por XPath, actualiza os campos
     * fornecidos e grava o ficheiro com backup.
     *
     * @param sk         socket do cliente
     * @param docPedido  documento XML com o pedido <alterar>
     */
    public static void runAlterar(Socket sk, Document docPedido) throws Exception {
        PrintWriter os = new PrintWriter(sk.getOutputStream(), true);

        Element alterarEl = getMethod(docPedido, "alterar");
        String username   = alterarEl.getAttribute("nickname");
        String novaSenha  = alterarEl.getAttribute("senha");

        // A fotografia é opcional no pedido de alteração
        String novaFoto = null;
        if (alterarEl.getElementsByTagName("photography").getLength() > 0) {
            novaFoto = alterarEl.getElementsByTagName("photography").item(0).getTextContent();
        }

        String ficheiroUsers = XMLDoc.getContexto() + "users.xml";
        Document docUsers    = XMLDoc.parseFile(ficheiroUsers);

        // Localizar o utilizador por XPath
        NodeList users = XMLDoc.getXPath("//user[username='" + username + "']", docUsers);

        if (users != null && users.getLength() > 0) {
            Element userNode = (Element) users.item(0);

            // Actualizar apenas os campos fornecidos
            if (novaSenha != null && !novaSenha.isEmpty()) {
                userNode.getElementsByTagName("password").item(0).setTextContent(XMLDoc.SHA256(novaSenha));
            }
            if (novaFoto != null && !novaFoto.isEmpty()) {
                userNode.getElementsByTagName("photography").item(0).setTextContent(novaFoto);
            }

            XMLDoc.gravarLock(docUsers, ficheiroUsers, XMLDoc.gerarNomeFBackupVersao(ficheiroUsers));
            os.println("<metodo><alterar resposta='Sucesso'/></metodo>");
            System.out.println("✅ Perfil do utilizador '" + username + "' atualizado com sucesso!");
        } else {
            os.println("<metodo><alterar resposta='Erro: Utilizador não encontrado'/></metodo>");
        }
    }

    /**
     * Trata o pedido de início de sessão de um jogador.
     *
     * Autentica o utilizador com username/senha, anexa os dados do perfil
     * à resposta XML e envia de volta ao cliente com o símbolo atribuído.
     *
     * @param sk         socket do cliente
     * @param simbolo    símbolo atribuído a este jogador ('X' ou 'O')
     * @param docPedido  documento XML com o pedido <iniciar>
     * @throws Exception se a autenticação falhar
     */
    public static void runIniciar(Socket sk, char simbolo, Document docPedido) throws Exception {
        PrintWriter os = new PrintWriter(sk.getOutputStream(), true);

        System.out.println("   Jogador '" + simbolo + "': " + sk);
        String Nome  = getMethod(docPedido, "iniciar").getAttribute("nickname");
        String Senha = getMethod(docPedido, "iniciar").getAttribute("senha");

        System.out.println("   Jogador '" + simbolo + "': " + Nome + " / " + Senha);

        // Autenticar o utilizador (verifica password com hash SHA-256)
        User jg = User._authenticate(Nome, Senha);
        if (jg == null)
            throw new Exception("Falhou a autenticação do utilizador '" + Nome + "'!");

        System.out.println("Autenticação/login do utilizador '" + Nome + "' realizado com sucesso!");

        // Construir a resposta: o pedido <iniciar> original com o perfil do jogador anexado
        Document d = XMLDoc.parseString(jg.toXMLString(simbolo));
        Node jogador     = d.getElementsByTagName("jogador").item(0);
        Node cloneElement = docPedido.importNode(jogador, true);
        docPedido.getElementsByTagName("iniciar").item(0).appendChild(cloneElement);

        // Serializar e enviar (sem quebras de linha para manter o protocolo de uma linha por mensagem)
        String msg = XMLDoc.documentToString(docPedido).replaceAll("\\r\\n|\\r|\\n", "");
        registaLog("Cliente{" + msg + "}");
        os.println(msg);
    }

    /**
     * Trata um pedido <obter> — envia o estado actual do tabuleiro ao cliente.
     * Usado pelo ServidorDedicado no protocolo antigo (CLI/GUI).
     *
     * @param is     stream de leitura do socket
     * @param os     stream de escrita do socket
     * @param simbolo símbolo do jogador ('X' ou 'O')
     * @param sk     socket do cliente
     * @param jogo   estado actual do jogo
     */
    public static void runObter(BufferedReader is, PrintWriter os, char simbolo, Socket sk, JogoXML jogo) throws Exception {
        Document obter = getNext(is);
        getMethod(obter, "obter"); // verificar que é de facto um pedido <obter>
        String msgObter = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
        registaLog("Cliente{" + msgObter + "}");
        os.println(msgObter);
    }

    /**
     * Trata um pedido <jogar> — processa a jogada e envia o tabuleiro actualizado.
     * Usado pelo ServidorDedicado no protocolo antigo (CLI/GUI).
     *
     * @param is      stream de leitura do socket
     * @param os      stream de escrita do socket
     * @param simbolo símbolo do jogador ('X' ou 'O')
     * @param sk      socket do cliente
     * @param jogo    estado actual do jogo (modificado in-place)
     * @return jogo actualizado após a jogada
     */
    public static JogoXML runJogar(BufferedReader is, PrintWriter os, char simbolo, Socket sk, JogoXML jogo) throws Exception {
        Document jogar  = getNext(is);
        Element jogadaEl = getMethod(jogar, "jogar");
        String jogadaStr = jogadaEl.getAttribute("jogada");
        String[] partes  = jogadaStr.trim().split("\\s+");

        try {
            if (partes.length == 4) {
                int[] coords = new int[4];
                for (int i = 0; i < 4; i++) coords[i] = Integer.parseInt(partes[i]);
                jogo.joga(coords, simbolo);
            } else {
                throw new Exception("Formato de coordenadas inválido.");
            }
        } catch (Exception e) {
            System.err.println("Erro na jogada do Skeleton: " + e.getMessage());
        }

        String msg = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
        registaLog("Cliente{" + msg + "}");
        os.println(msg);
        return jogo;
    }

    /**
     * Extrai o elemento XML correspondente a um método específico do documento.
     * Lança uma excepção se o método não existir ou existir mais do que uma vez.
     *
     * @param doc    documento XML
     * @param Method nome do elemento a extrair (ex: "iniciar", "jogar")
     * @return elemento XML encontrado
     * @throws Exception se o elemento não for encontrado exactamente uma vez
     */
    private static Element getMethod(final Document doc, final String Method) throws Exception {
        NodeList items = doc.getElementsByTagName(Method);
        if (items.getLength() != 1) {
            throw new Exception("Erro de lógica: espera método '" + Method + "'!");
        }
        return ((Element) doc.getElementsByTagName(Method).item(0));
    }

    /**
     * Adiciona uma linha de texto ao final de um ficheiro.
     * Utilitário interno usado pelo registo de log.
     *
     * @param ficheiro caminho do ficheiro
     * @param texto    linha a adicionar
     */
    private static void adicionarStringFicheiro(String ficheiro, String texto) throws IOException {
        try (PrintWriter escritor = new PrintWriter(new BufferedWriter(new FileWriter(ficheiro, true)))) {
            escritor.println(texto);
        }
    }

    /**
     * Regista um evento no ficheiro protocolo.log para auditoria.
     * O timestamp é adicionado automaticamente. Quebras de linha são removidas
     * para garantir uma entrada por linha.
     *
     * @param evento texto a registar (ex: "Servidor{<xml>}" ou "Cliente{<xml>}")
     */
    private static void registaLog(String evento) {
        try {
            adicionarStringFicheiro(
                XMLDoc.getContexto() + "protocolo.log",
                LocalDateTime.now() + " - " + evento.replaceAll("\n", "").replaceAll("\r", "")
            );
        } catch (IOException e) {
            System.err.println("[LOG] Erro ao escrever protocolo.log: " + e.getMessage());
        }
    }
}