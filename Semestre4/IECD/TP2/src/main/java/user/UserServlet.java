package user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@WebServlet("/UserServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1,  // 1 MB
    maxFileSize = 1024 * 1024 * 5,       // 5 MB max por ficheiro
    maxRequestSize = 1024 * 1024 * 10    // 10 MB max por pedido
)
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private void limparLinhasEmBranco(Node node) {
        if (node == null) return;
        
        NodeList filhos = node.getChildNodes();
        for (int i = filhos.getLength() - 1; i >= 0; i--) {
            Node filho = filhos.item(i);
            
            if (filho.getNodeType() == Node.TEXT_NODE) {
                // Se for um nó de texto que contém apenas espaços ou quebras de linha, é removido
                if (filho.getNodeValue().trim().isEmpty()) {
                    node.removeChild(filho);
                }
            } else if (filho.getNodeType() == Node.ELEMENT_NODE) {
                // Se for um elemento (ex: <proverbio>), continua a procurar lá dentro
                limparLinhasEmBranco(filho);
            }
        }
    }
    
    /**
     * Valida o documento DOM contra o XSD (se existir) e escreve as alterações no ficheiro XML.
     * Se a validação XSD falhar, lança uma SAXException e impede a gravação no disco.
     */
    private void validarESalvarXML(Document doc, String xmlPath, String xsdPath) throws Exception {
        File xsdFile = new File(xsdPath);
        
        // 1. Validar contra o Schema XSD antes de tocar no ficheiro em disco
        if (xsdFile.exists()) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(xsdFile);
            Validator validator = schema.newValidator();
            
            // Se falhar aqui, lança SAXException e o código abaixo (gravação) é ignorado
            validator.validate(new DOMSource(doc));
            System.out.println("[XSD CONFORME] O documento XML cumpre integralmente as regras do user.xsd.");
        }

        // 2. Se passou na validação (ou não há XSD), procede à gravação física no disco
        File xmlFile = new File(xmlPath);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        limparLinhasEmBranco(doc);
        doc.getDocumentElement().normalize();
        doc.normalizeDocument();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(xmlFile);
        transformer.transform(source, result);
        System.out.println("[XML GRAVADO] Ficheiro gravado com sucesso em: " + xmlPath);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Forçar processamento global em UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        String reqId = request.getParameter("userid");
        if (reqId == null) reqId = "";

        String xmlPath = getServletContext().getRealPath("/users.xml");
        String xsdPath = getServletContext().getRealPath("/users.xsd");
        File xmlFile = new File(xmlPath);

        if (!xmlFile.exists()) {
            response.sendRedirect("users.jsp?msgErro=" + java.net.URLEncoder.encode("O ficheiro users.xml não foi encontrado.", "UTF-8"));
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            factory.setCoalescing(true);
            factory.setNamespaceAware(true); 
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            Element root = doc.getDocumentElement();

            boolean modificado = false;
            String mensagemSucesso = "";

            // =================================================================
            // OPERAÇÃO: APAGAR UTILIZADOR (action=delete)
            // =================================================================
            if ("delete".equals(action) && !reqId.isEmpty()) {
                NodeList listaUsers = root.getElementsByTagName("user");
                Element userParaRemover = null;

                for (int i = 0; i < listaUsers.getLength(); i++) {
                    Element u = (Element) listaUsers.item(i);
                    NodeList idNodes = u.getElementsByTagName("userid");
                    if (idNodes.getLength() > 0 && idNodes.item(0).getTextContent().equals(reqId)) {
                        userParaRemover = u;
                        break;
                    }
                }

                if (userParaRemover != null) {
                    System.out.println("\n❌ [TOMCAT CONSOLA] UTILIZADOR A SER REMOVIDO:");
                    System.out.println("------------------------------------------------");
                    System.out.println("-> UserID    : " + reqId);
                    System.out.println("-> Username  : " + userParaRemover.getElementsByTagName("username").item(0).getTextContent());
                    System.out.println("-> Email     : " + userParaRemover.getElementsByTagName("email").item(0).getTextContent());
                    System.out.println("------------------------------------------------\n");

                    root.removeChild(userParaRemover);
                    modificado = true; 
                    mensagemSucesso = "Utilizador removido do ficheiro XML com sucesso!";
                } else {
                    response.sendRedirect("users.jsp?msgErro=" + java.net.URLEncoder.encode("Utilizador não encontrado no documento XML.", "UTF-8"));
                    return;
                }
            }
            // =================================================================
            // OPERAÇÃO: GRAVAR / ATUALIZAR UTILIZADOR (action=save)
            // =================================================================
            else if ("save".equals(action)) {
                String reqProfile     = request.getParameter("profile");
                String reqUsername    = request.getParameter("username");
                String reqFirstnames  = request.getParameter("firstnames");
                String reqLastnames   = request.getParameter("lastnames");
                String reqEmail       = request.getParameter("email");
                String reqBirthdate   = request.getParameter("birthdate");
                String reqGender      = request.getParameter("gender");
                String reqNationality = request.getParameter("nationality");
                String reqPassword    = request.getParameter("password");
                String reqBlocked     = request.getParameter("blocked");
                String reqCorFundo    = request.getParameter("corFundo");

                // Garantir isolamento contra nulls vindos do request
                if (reqProfile == null) reqProfile = "0"; else reqProfile = reqProfile.trim();
                if (reqUsername == null) reqUsername = ""; else reqUsername = reqUsername.trim();
                if (reqFirstnames == null) reqFirstnames = ""; else reqFirstnames = reqFirstnames.trim();
                if (reqLastnames == null) reqLastnames = ""; else reqLastnames = reqLastnames.trim();
                if (reqEmail == null) reqEmail = ""; else reqEmail = reqEmail.trim();
                if (reqBirthdate == null) reqBirthdate = ""; else reqBirthdate = reqBirthdate.trim();
                if (reqGender == null) reqGender = ""; else reqGender = reqGender.trim();
                if (reqNationality == null) reqNationality = ""; else reqNationality = reqNationality.trim();
                if (reqPassword == null) reqPassword = "";
                reqBlocked = "true".equals(reqBlocked) ? "true" : "false";
                if (reqCorFundo == null) reqCorFundo = "#FFFFFF"; else reqCorFundo = reqCorFundo.trim();

                // 🚨 1. NOVA VALIDAÇÃO DE SEGURANÇA: Bloquear campos obrigatórios vazios
                if (reqUsername.isEmpty() || reqFirstnames.isEmpty() || reqLastnames.isEmpty() || 
                    reqEmail.isEmpty() || reqBirthdate.isEmpty() || reqNationality.isEmpty()) {
                    
                    // Se estiver a criar novo utilizador, volta para o register.jsp. Se estiver a editar, volta para o users.jsp.
                    String urlErro = reqId.isEmpty() ? "register.jsp" : "users.jsp?action=edit&id=" + reqId;
                    response.sendRedirect(urlErro + (urlErro.contains("?") ? "&" : "?") + "msgErro=" + java.net.URLEncoder.encode("Erro: Por favor, preenche todos os campos obrigatórios (incluindo Nacionalidade e Data de Nascimento).", "UTF-8"));
                    return; // Aborta a operação imediatamente!
                }

                // 🚨 2. VALIDAÇÃO DO FORMATO DA NACIONALIDADE (Agora é obrigatória, logo corre sempre)
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[([A-Z-a-z]+)\\]");
                java.util.regex.Matcher matcher = pattern.matcher(reqNationality);

                if (matcher.find()) {
                    reqNationality = matcher.group(1).toUpperCase(); // Extrai apenas "PT", "BR", etc.
                } else {
                    String urlErro = reqId.isEmpty() ? "register.jsp" : "users.jsp?action=edit&id=" + reqId;
                    response.sendRedirect(urlErro + (urlErro.contains("?") ? "&" : "?") + "msgErro=" + java.net.URLEncoder.encode("Erro no formato da nacionalidade! Deve conter o código do país entre parênteses retos. Ex: [PT]", "UTF-8"));
                    return; // Aborta a operação imediatamente!
                }
                
                // Obter fluxo do ficheiro de imagem
                Part filePart = request.getPart("photoFile");
                String reqPhotoBase64 = "";
                boolean novaFotoSubmetida = false;
                if (filePart != null && filePart.getSize() > 0) {
                    try (InputStream is = filePart.getInputStream()) {
                        reqPhotoBase64 = Base64.getEncoder().encodeToString(is.readAllBytes());
                        novaFotoSubmetida = true;
                    }
                }

                String timestampAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

                // Print completo na consola para monitorizar tudo em tempo real
                System.out.println("\n💾 [TOMCAT CONSOLA] VALORES RECEBIDOS NO FORMULÁRIO:");
                System.out.println("----------------------------------------------------------------");
                System.out.println("-> Tipo Operação    : " + (reqId.isEmpty() ? "INSERÇÃO" : "EDIÇÃO"));
                System.out.println("-> UserID Alvo      : " + (reqId.isEmpty() ? "[Novo UUID]" : reqId));
                System.out.println("-> Username         : '" + reqUsername + "'");
                System.out.println("-> Nomes Próprios   : '" + reqFirstnames + "'");
                System.out.println("-> Apelidos         : '" + reqLastnames + "'");
                System.out.println("-> Email            : '" + reqEmail + "'");
                System.out.println("-> Código Perfil    : '" + reqProfile + "'");
                System.out.println("-> Bloqueado        : '" + reqBlocked + "'");
                System.out.println("-> Género (Opcional): '" + (reqGender.isEmpty() ? "[Vazio - Omitir Tag]" : reqGender) + "'");
                System.out.println("-> Data Nasc.(Opc.) : '" + (reqBirthdate.isEmpty() ? "[Vazio - Omitir Tag]" : reqBirthdate) + "'");
                System.out.println("-> Nacionalidade(Op): '" + (reqNationality.isEmpty() ? "[Vazio - Omitir Tag]" : reqNationality) + "'");
                System.out.println("-> Nova Password    : " + (reqPassword.isEmpty() ? "[Não alterada]" : "[Texto enviado - Aplicando Hash]"));
                System.out.println("-> Nova Foto        : " + (novaFotoSubmetida ? "[Sim - Atualizando]" : "[Não enviada]"));
                System.out.println("----------------------------------------------------------------\n");

                if (reqId.isEmpty()) {
                    // ==========================================
                    // ---- MODO: INSERIR UTILIZADOR NOVO ----
                    // ==========================================
                    String novoId = UUID.randomUUID().toString();
                    if (reqPassword.isEmpty()) {
                        reqPassword = "Alterar123!"; // password por omissão
                    }

                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    byte[] digest = md.digest(reqPassword.getBytes("UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    for (byte b : digest) { sb.append(String.format("%02x", b)); }
                    String senhaHash = sb.toString();

                    Element novoUser = doc.createElement("user");

                    Element uId = doc.createElement("userid"); uId.setTextContent(novoId);
                    Element uUpdated = doc.createElement("updated"); uUpdated.setTextContent(timestampAtual);
                    Element uBlocked = doc.createElement("blocked"); uBlocked.setTextContent(reqBlocked);
                    Element uProfile = doc.createElement("profile"); uProfile.setTextContent(reqProfile);
                    Element uName = doc.createElement("username"); uName.setTextContent(reqUsername);
                    Element uFirst = doc.createElement("firstnames"); uFirst.setTextContent(reqFirstnames);
                    Element uLast = doc.createElement("lastnames"); uLast.setTextContent(reqLastnames);
                    Element uEmail = doc.createElement("email"); uEmail.setTextContent(reqEmail);
                    
                    novoUser.appendChild(uId);
                    novoUser.appendChild(uUpdated);
                    novoUser.appendChild(uBlocked);
                    novoUser.appendChild(uProfile);
                    novoUser.appendChild(uName);
                    novoUser.appendChild(uFirst);
                    novoUser.appendChild(uLast);
                    novoUser.appendChild(uEmail);

                    // Só anexa as tags opcionais se o valor NÃO for vazio
                    if (!reqGender.isEmpty()) {
                        Element uGender = doc.createElement("gender"); uGender.setTextContent(reqGender);
                        novoUser.appendChild(uGender);
                    }
                    if (!reqBirthdate.isEmpty()) {
                        Element uBirth = doc.createElement("birthdate"); uBirth.setTextContent(reqBirthdate);
                        novoUser.appendChild(uBirth);
                    }
                    if (!reqPhotoBase64.isEmpty()) {
                        Element uPhoto = doc.createElement("photography"); uPhoto.setTextContent(reqPhotoBase64);
                        novoUser.appendChild(uPhoto);
                    }
                    if (!reqNationality.isEmpty()) {
                        Element uNat = doc.createElement("nationality"); uNat.setTextContent(reqNationality);
                        novoUser.appendChild(uNat);
                    }
                    if (!reqCorFundo.isEmpty()) {
                        Element uCor = doc.createElement("corFundo"); uCor.setTextContent(reqCorFundo);
                        novoUser.appendChild(uCor);
                    }

                    Element uPass = doc.createElement("password"); uPass.setTextContent(senhaHash);
                    novoUser.appendChild(uPass);

                    root.appendChild(novoUser);
                    modificado = true;
                    mensagemSucesso = "Utilizador registado com sucesso!";
                } else {
                    // ==========================================
                    // ---- MODO: ATUALIZAR UTILIZADOR ----
                    // ==========================================
                    NodeList listaUsers = root.getElementsByTagName("user");
                    Element userAlvo = null;
                    
                    for (int i = 0; i < listaUsers.getLength(); i++) {
                        Element u = (Element) listaUsers.item(i);
                        NodeList idNodes = u.getElementsByTagName("userid");
                        if (idNodes.getLength() > 0 && idNodes.item(0).getTextContent().equals(reqId)) {
                            userAlvo = u;
                            break;
                        }
                    }

                    if (userAlvo != null) {
                        // 1. Recuperar dados antigos que não devem ser perdidos
                        String senhaHashFinal = userAlvo.getElementsByTagName("password").item(0).getTextContent();
                        
                        // Se não mandou foto nova, recupera a foto que já estava guardada no XML (se houvesse)
                        if (!novaFotoSubmetida) {
                            NodeList fotoNodes = userAlvo.getElementsByTagName("photography");
                            if (fotoNodes.getLength() > 0) {
                                reqPhotoBase64 = fotoNodes.item(0).getTextContent();
                            }
                        }

                        // Se mandou texto na password, gera o novo hash SHA-256
                        if (!reqPassword.isEmpty()) {
                            MessageDigest md = MessageDigest.getInstance("SHA-256");
                            byte[] digest = md.digest(reqPassword.getBytes("UTF-8"));
                            StringBuilder sb = new StringBuilder();
                            for (byte b : digest) { sb.append(String.format("%02x", b)); }
                            senhaHashFinal = sb.toString();
                        }

                        // 2. Limpar todos os subelementos antigos do utilizador (Zera a estrutura interna do nó)
                        while (userAlvo.hasChildNodes()) {
                            userAlvo.removeChild(userAlvo.getFirstChild());
                        }

                        // 3. Reconstruir a sequência perfeita respeitando a ausência de tags vazias
                        Element uId = doc.createElement("userid"); uId.setTextContent(reqId);
                        Element uUpdated = doc.createElement("updated"); uUpdated.setTextContent(timestampAtual);
                        Element uBlocked = doc.createElement("blocked"); uBlocked.setTextContent(reqBlocked);
                        Element uProfile = doc.createElement("profile"); uProfile.setTextContent(reqProfile);
                        Element uName = doc.createElement("username"); uName.setTextContent(reqUsername);
                        Element uFirst = doc.createElement("firstnames"); uFirst.setTextContent(reqFirstnames);
                        Element uLast = doc.createElement("lastnames"); uLast.setTextContent(reqLastnames);
                        Element uEmail = doc.createElement("email"); uEmail.setTextContent(reqEmail);

                        userAlvo.appendChild(uId);
                        userAlvo.appendChild(uUpdated);
                        userAlvo.appendChild(uBlocked);
                        userAlvo.appendChild(uProfile);
                        userAlvo.appendChild(uName);
                        userAlvo.appendChild(uFirst);
                        userAlvo.appendChild(uLast);
                        userAlvo.appendChild(uEmail);

                        // Injetar tags opcionais apenas se tiverem conteúdo válido (Não quebra o XSD!)
                        if (!reqGender.isEmpty()) {
                            Element uGender = doc.createElement("gender"); uGender.setTextContent(reqGender);
                            userAlvo.appendChild(uGender);
                        }
                        if (!reqBirthdate.isEmpty()) {
                            Element uBirth = doc.createElement("birthdate"); uBirth.setTextContent(reqBirthdate);
                            userAlvo.appendChild(uBirth);
                        }
                        if (!reqPhotoBase64.isEmpty()) {
                            Element uPhoto = doc.createElement("photography"); uPhoto.setTextContent(reqPhotoBase64);
                            userAlvo.appendChild(uPhoto);
                        }
                        if (!reqNationality.isEmpty()) {
                            Element uNat = doc.createElement("nationality"); uNat.setTextContent(reqNationality);
                            userAlvo.appendChild(uNat);
                        }
                        if (!reqCorFundo.isEmpty()) {
							Element uCor= doc.createElement("corFundo"); uCor.setTextContent(reqCorFundo);
							userAlvo.appendChild(uCor);
						}

                        // Password (Sempre o último elemento da sequência devido à extensão)
                        Element uPass = doc.createElement("password"); uPass.setTextContent(senhaHashFinal);
                        userAlvo.appendChild(uPass);

                        modificado = true;
                        mensagemSucesso = "Informação do utilizador atualizada com sucesso!";
                    }
                }
            }

         // -----------------------------------------------------------------
            // VALIDAÇÃO CONTRA O SCHEMA E ESCRITA FINAL NO DISCO XML
            // -----------------------------------------------------------------
            if (modificado) {
                try {
                    // Chamada ao método que arrumámos acima
                    validarESalvarXML(doc, xmlPath, xsdPath);
                    
                    // Sucesso: Limpa qualquer erro anterior
                    request.getSession().removeAttribute("mensagemErroSessao");
                    
                    // --- NOVA LÓGICA DE REDIRECIONAMENTO INTELIGENTE ---
                    if (reqId.isEmpty()) {
                        // Se o reqId está vazio, significa que é um REGISTO NOVO. Vai para o login!
                        response.sendRedirect("login.jsp?msgSucesso=" + java.net.URLEncoder.encode("Conta criada com sucesso! Podes iniciar sessão.", "UTF-8"));
                    } else {
                        // Se tem reqId, foi uma EDIÇÃO no painel. Volta para a página de gestão!
                        response.sendRedirect("users.jsp?action=edit&id=" + reqId + "&msgSucesso=" + java.net.URLEncoder.encode(mensagemSucesso, "UTF-8"));
                    }
                    // ---------------------------------------------------
                    
                } catch (org.xml.sax.SAXException e) {
                    // Captura especificamente o erro de validação do XSD
                    System.err.println("\n❌ [CONTROLO XSD] Erro de validação estrutural!");
                    System.err.println("Detalhe: " + e.getMessage() + "\n");
                    
                    String msgAmigavel = "Os dados submetidos não respeitam as regras do sistema (XSD).\n"
                                       + "Por favor, contacte o administrador para reportar o erro.\n\n"
                                       + "Detalhe técnico: " + e.getMessage();
                    
                    request.getSession().setAttribute("mensagemErroSessao", msgAmigavel);
                    response.sendRedirect("users.jsp?action=edit&id=" + reqId);
                    return;
                    
                } catch (Exception e) {
                    // Captura outros erros (ex: falta de permissão de escrita no ficheiro)
                    System.err.println("[ERRO ESCRITA] Falha ao gravar no ficheiro XML.");
                    e.printStackTrace();
                    
                    request.getSession().setAttribute("mensagemErroSessao", "Erro ao gravar no ficheiro: " + e.getMessage());
                    response.sendRedirect("users.jsp");
                }
            } else {
                response.sendRedirect("users.jsp");
            }

        } catch (Exception e) {
            // Catch geral do bloco do Servlet (erros de parsing iniciais, SHA-256, etc.)
            System.err.println("[ERRO GERAL] Falha crítica no processamento.");
            e.printStackTrace();
            request.getSession().setAttribute("mensagemErroSessao", "Erro inesperado: " + e.getMessage());
            response.sendRedirect("users.jsp");
        }
    }
}