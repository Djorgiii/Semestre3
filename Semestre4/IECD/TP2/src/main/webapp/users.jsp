<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, java.util.*, java.time.*, java.time.format.*, java.security.MessageDigest, javax.xml.parsers.*, javax.xml.transform.*, javax.xml.transform.dom.*, javax.xml.transform.stream.*, org.w3c.dom.*, javax.xml.validation.*, javax.xml.XMLConstants, jakarta.servlet.http.Part"%>
<%
    // Forçar estritamente o processamento e encoding em UTF-8
    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    String paginaAtual = request.getRequestURI();
    String xmlPath = application.getRealPath("/users.xml");
    String xsdPath = application.getRealPath("/users.xsd");
    String nationalitiesXmlPath = application.getRealPath("/nationalities.xml");

    // Capturar mensagens vindas do Servlet através de parâmetros de redirecionamento
    String mensagemSucesso = "";
    String mensagemErro = "";

    // 1. Verificar se o Servlet deixou alguma mensagem de ERRO detalhada na Sessão
    if (session.getAttribute("mensagemErroSessao") != null) {
        mensagemErro = (String) session.getAttribute("mensagemErroSessao");
        
        // IMPORTANTÍSSIMO: Remove imediatamente da sessão após ler.
        // Isto garante que se o utilizador fizer F5/Refresh, o erro desaparece.
        session.removeAttribute("mensagemErroSessao");
        
    } else if (request.getParameter("msgErro") != null) {
        // Fallback caso alguma outra validação antiga envie pela URL
        mensagemErro = java.net.URLDecoder.decode(request.getParameter("msgErro"), "UTF-8");
    }

    // 2. Capturar mensagem de SUCESSO normal vinda por URL
    if (request.getParameter("msgSucesso") != null) {
        mensagemSucesso = java.net.URLDecoder.decode(request.getParameter("msgSucesso"), "UTF-8");
    }

    boolean modoEdicao = false;
    String editUserid = "", editUpdated = "", editBlocked = "false", editProfile = "0", editUsername = "", editFirstnames = "", editLastnames = "", editEmail = "", editGender = "", editNationality = "", editPhotoDataUri = "", editPasswordAtual = "", editBirthdate = "", editCorFundo = "#FFFFFF";

    // Mapeamento auxiliar de ISO para Emojis de Bandeiras
    java.util.function.Function<String, String> isoParaEmojiBandeira = (String codigoIso) -> {
        if (codigoIso == null || codigoIso.length() != 2) return "";
        try {
            int cp1 = codigoIso.codePointAt(0) - 'A' + 0x1F1E6;
            int cp2 = codigoIso.codePointAt(1) - 'A' + 0x1F1E6;
            return new String(Character.toChars(cp1)) + new String(Character.toChars(cp2));
        } catch (Exception e) {
            return "";
        }
    };

    // Método utilitário para calcular a idade com base em AAAA-MM-DD
    java.util.function.Function<String, String> calcularIdadeJava = (String dataNascStr) -> {
        if (dataNascStr == null || dataNascStr.trim().isEmpty()) return "N/D";
        try {
            LocalDate dataNasc = LocalDate.parse(dataNascStr.trim());
            return String.valueOf(Period.between(dataNasc, LocalDate.now()).getYears());
        } catch (Exception e) {
            return "N/D";
        }
    };

    String dataHojeIso = LocalDate.now().toString();

    // --- CARREGAR DICIONÁRIO DE NACIONALIDADES DO XML ---
    Map<String, String> mapaNacionalidadesNome = new LinkedHashMap<>();
    Map<String, String> mapaNacionalidadesBandeira = new HashMap<>();   
    
    try {
        File natFile = new File(nationalitiesXmlPath);
        if (natFile.exists() && natFile.length() > 0) {
            Document docNat = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(natFile);
            NodeList nlNat = docNat.getElementsByTagName("nationality");
            for (int i = 0; i < nlNat.getLength(); i++) {
                Element natEl = (Element) nlNat.item(i);
                String abv = natEl.getElementsByTagName("abbreviation").item(0).getTextContent().trim().toUpperCase();
                String nomePt = natEl.getElementsByTagName("pt-name").getLength() > 0 ? natEl.getElementsByTagName("pt-name").item(0).getTextContent().trim() : abv;
                String flagB64 = natEl.getElementsByTagName("flag").getLength() > 0 ? natEl.getElementsByTagName("flag").item(0).getTextContent().trim() : "";
                mapaNacionalidadesNome.put(abv, nomePt);
                mapaNacionalidadesBandeira.put(abv, flagB64);
            }
        }
    } catch (Exception e) {
        mensagemErro = "Aviso no catálogo geográfico: " + e.getMessage();
    }

    // --- PROCESSAMENTO DE ACÇÕES (EDIT) ---
    String action = request.getParameter("action");
    if (action != null) {
        try {
            File xmlFile = new File(xmlPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;
            if (xmlFile.exists() && xmlFile.length() > 0) {
                doc = dBuilder.parse(xmlFile);
            } else {
                doc = dBuilder.newDocument();
                Element rootElement = doc.createElement("users");
                doc.appendChild(rootElement);
            }

            // OPERAÇÃO: PREPARAÇÃO PARA EDIÇÃO (CARREGAR NO FORMULÁRIO)
            if (action.equals("edit")) {
                String idParaEditar = request.getParameter("id");
                NodeList userList = doc.getElementsByTagName("user");
                
                for (int i = 0; i < userList.getLength(); i++) {
                    Element user = (Element) userList.item(i);
                    String userid = user.getElementsByTagName("userid").item(0).getTextContent();
                    if (userid.equals(idParaEditar)) {
                        modoEdicao = true;
                        editUserid = userid;
                        editUpdated = user.getElementsByTagName("updated").item(0).getTextContent();
                        editBlocked = (user.getElementsByTagName("blocked").getLength() > 0) ? user.getElementsByTagName("blocked").item(0).getTextContent() : "false";
                        editProfile = user.getElementsByTagName("profile").item(0).getTextContent();
                        editUsername = user.getElementsByTagName("username").item(0).getTextContent();
                        editFirstnames = user.getElementsByTagName("firstnames").item(0).getTextContent();
                        editLastnames = user.getElementsByTagName("lastnames").item(0).getTextContent();
                        editEmail = user.getElementsByTagName("email").item(0).getTextContent();
                        editGender = (user.getElementsByTagName("gender").getLength() > 0) ? user.getElementsByTagName("gender").item(0).getTextContent() : "";
                        editNationality = user.getElementsByTagName("nationality").item(0).getTextContent();
                        editBirthdate = (user.getElementsByTagName("birthdate").getLength() > 0) ? user.getElementsByTagName("birthdate").item(0).getTextContent() : "";
                        editPasswordAtual = (user.getElementsByTagName("password").getLength() > 0) ? user.getElementsByTagName("password").item(0).getTextContent() : "";
                        editCorFundo = (user.getElementsByTagName("corFundo").getLength() > 0) ? user.getElementsByTagName("corFundo").item(0).getTextContent() : "#FFFFFF";
                        
                        String rawPhoto = (user.getElementsByTagName("photography").getLength() > 0) ? user.getElementsByTagName("photography").item(0).getTextContent().trim() : "";
                        if (!rawPhoto.isEmpty() && !rawPhoto.startsWith("data:image/")) {
                            editPhotoDataUri = "data:image/jpeg;base64," + rawPhoto;
                        } else {
                            editPhotoDataUri = rawPhoto;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            mensagemErro = "Erro ao processar ação: " + e.getMessage();
        }
    }

    // --- LEITURA ATUALIZADA PARA A TABELA ---
    List<Element> listaUtilizadores = new ArrayList<>();
    try {
        File xmlFile = new File(xmlPath);
        if (xmlFile.exists() && xmlFile.length() > 0) {
            Document docDb = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
            NodeList nl = docDb.getElementsByTagName("user");
            for (int i = 0; i < nl.getLength(); i++) {
                listaUtilizadores.add((Element) nl.item(i));
            }
        }
    } catch (Exception e) {
        mensagemErro = "Erro na leitura estrutural XML: " + e.getMessage();
    }
%>

<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>Painel de Gestão Avançado XML/XSD</title>
    <style>
        body { font-family: system-ui, sans-serif; background-color: #f7fafc; color: #2d3748; margin: 30px; }
        .container { max-width: 1300px; margin: 0 auto; }
        h1, h2 { color: #319795; margin-bottom: 5px; }
        .alert-success { background: #c6f6d5; color: #22543d; padding: 12px; border-radius: 6px; margin-bottom: 20px; font-weight: 500; }
        .alert-danger { background: #fed7d7; color: #742a2a; padding: 12px; border-radius: 6px; margin-bottom: 20px; font-family: monospace; white-space: pre-wrap; border-left: 4px solid #e53e3e; }
        .card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); margin-bottom: 30px; position: relative; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 0.9em; vertical-align: middle; }
        th { background-color: #edf2f7; color: #4a5568; }
        .btn { padding: 8px 14px; border-radius: 4px; text-decoration: none; font-size: 0.85em; font-weight: bold; cursor: pointer; border: none; display: inline-block; }
        .btn-primary { background: #319795; color: white; }
        .btn-danger { background: #e53e3e; color: white; }
        .btn-secondary { background: #718096; color: white; }
        .form-group { margin-bottom: 15px; }
        label { display: block; font-size: 0.85em; font-weight: bold; color: #4a5568; margin-bottom: 6px; }
        input[type="text"], input[type="email"], input[type="password"], input[type="date"], input[type="color"] { width: 100%; padding: 10px; border: 1px solid #cbd5e0; border-radius: 4px; box-sizing: border-box; background: #fff; }
        
        /* ------------------------------------------------------------------
           ZONA COMPACTA DE DRAG & DROP EM CIMA DA PRÓPRIA FOTOGRAFIA 
        --------------------------------------------------------------------- */
        .avatar-dropzone-wrapper {
            position: relative;
            width: 120px;
            height: 120px;
            border-radius: 50%;
            cursor: pointer;
            overflow: hidden;
            border: 3px solid #319795;
            background-color: #edf2f7;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            margin: 0 auto 15px auto;
        }
        .avatar-img-preview {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }
        .avatar-dropzone-overlay {
            position: absolute;
            top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(49, 151, 149, 0.85);
            color: white;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            text-align: center;
            font-size: 0.75em;
            font-weight: bold;
            padding: 8px;
            box-sizing: border-box;
            opacity: 0;
            transition: opacity 0.2s ease-in-out;
            pointer-events: none;
        }
        .avatar-dropzone-wrapper:hover .avatar-dropzone-overlay,
        .avatar-dropzone-wrapper.dragover .avatar-dropzone-overlay {
            opacity: 1;
        }
        .avatar-dropzone-wrapper.dragover {
            border-color: #234e52;
            transform: scale(1.02);
        }

        .range-container, .age-container { display: flex; align-items: center; gap: 15px; background: #edf2f7; padding: 8px 12px; border-radius: 4px; border: 1px solid #cbd5e0; box-sizing: border-box; height: 42px; }
        .range-slider { flex: 1; cursor: pointer; accent-color: #319795; }
        .range-output, .age-output { font-weight: bold; font-size: 1.1em; color: #319795; background: white; padding: 2px 10px; border-radius: 4px; border: 1px solid #cbd5e0; text-align: center; }
        .radio-group { display: flex; gap: 20px; padding: 5px 0; }
        .radio-option { display: flex; align-items: center; gap: 6px; font-size: 0.9em; cursor: pointer; }
        .checkbox-container { display: flex; align-items: center; gap: 8px; margin-top: 28px; cursor: pointer; font-size: 0.9em; font-weight: bold; color: #e53e3e; }
        .row { display: flex; gap: 20px; }
        .col { flex: 1; }
        .badge { padding: 3px 8px; border-radius: 4px; font-size: 0.75em; font-weight: bold; color: white; }
        .flag-img { width: 24px; height: 16px; object-fit: cover; box-shadow: 0 1px 3px rgba(0,0,0,0.15); border-radius: 2px; }
        
        .user-avatar-container { position: relative; width: 44px; height: 44px; cursor: pointer; }
        .user-avatar { width: 100%; height: 100%; object-fit: cover; border: 3px solid #d4af37; box-shadow: 0 0 4px rgba(212,175,55,0.6); background-color: #edf2f7; }
        .user-avatar-container.zoomed .user-avatar { transform: scale(3.5); z-index: 50; position: absolute; box-shadow: 0 0 15px rgba(212,175,55,0.9); }
        .user-avatar-placeholder { display: flex; align-items: center; justify-content: center; font-size: 1.1em; font-weight: bold; color: #718096; width: 100%; height: 100%; border: 3px solid #d4af37; background-color: #edf2f7; }
    
        .password-wrapper {
            position: relative;
            width: 100%;
        }
        .password-toggle {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            cursor: pointer;
            background: none;
            border: none;
            font-size: 1.1em;
            padding: 0;
            color: #4a5568;
            user-select: none;
        }
        /* Evita que o texto longo da password fique colado ao ícone */
        input[type="password"], input[type="text"].pwd-field {
            padding-right: 35px !important;
        }
    
    
    </style>
    
    <script>
        function toggleVisualizacaoPassword() {
            const passwordInput = document.getElementById("idPasswordInput");
            const toggleBtn = document.getElementById("idTogglePasswordBtn");
            
            if (passwordInput.type === "password") {
                passwordInput.type = "text";
                toggleBtn.textContent = "👁️‍🗨️"; // Emoji para quando está visível (ocultar)
                toggleBtn.title = "Clique para ocultar a password.";
            } else {
                passwordInput.type = "password";
                toggleBtn.textContent = "👁️"; // Emoji para quando está oculta (mostrar)
                toggleBtn.title = "Clique para mostrar a password em texto limpo.";
            }
        }
        function atualizarIdadeOutput(dataStr) {
            const output = document.getElementById("idOutputIdade");
            if (!dataStr) { output.value = "N/D"; return; }
            const dataNascimento = new Date(dataStr);
            const hoje = new Date();
            if (dataNascimento > hoje) {
                alert("A data de nascimento não pode ser no futuro!");
                document.getElementById("idBirthdateInput").value = "<%= editBirthdate %>";
                return;
            }
            let idade = hoje.getFullYear() - dataNascimento.getFullYear();
            const m = hoje.getMonth() - dataNascimento.getMonth();
            if (m < 0 || (m === 0 && hoje.getDate() < dataNascimento.getDate())) { idade--; }
            output.value = isNaN(idade) ? "N/D" : idade;
        }

        document.addEventListener("DOMContentLoaded", () => {
            const wrapper = document.getElementById("avatarWrapper");
            const fileInput = document.getElementById("photoFileInput");
            const previewImg = document.getElementById("avatarPreviewImg");
            const overlayText = document.getElementById("overlayText");

            if(wrapper && fileInput) {
                wrapper.addEventListener("click", () => fileInput.click());

                wrapper.addEventListener("dragover", (e) => {
                    e.preventDefault();
                    wrapper.classList.add("dragover");
                });

                wrapper.addEventListener("dragleave", () => {
                    wrapper.classList.remove("dragover");
                });

                wrapper.addEventListener("drop", (e) => {
                    e.preventDefault();
                    wrapper.classList.remove("dragover");
                    if (e.dataTransfer.files.length > 0) {
                        fileInput.files = e.dataTransfer.files;
                        handlePreview(e.dataTransfer.files[0]);
                    }
                });

                fileInput.addEventListener("change", () => {
                    if (fileInput.files.length > 0) {
                        handlePreview(fileInput.files[0]);
                    }
                });
            }

            function handlePreview(file) {
                if (!file.type.startsWith("image/")) {
                    alert("Ficheiro inválido! Por favor selecione uma imagem válida (PNG, JPG, GIF, WEBP).");
                    fileInput.value = "";
                    return;
                }
                const reader = new FileReader();
                reader.onload = (e) => {
                    previewImg.src = e.target.result;
                    overlayText.innerHTML = "✓ Sucesso<br><span style='font-size:0.8em;font-weight:normal;'>Pronta para Gravar</span>";
                };
                reader.readAsDataURL(file);
            }
        });
    </script>
</head>
<body>
<div class="container">
    <h1>🛡️ Painel de Controlo de Identidades</h1>
    <% if (!mensagemSucesso.isEmpty()) { %> <div class="alert-success">✓ <%= mensagemSucesso %></div> <% } %>
    <% if (!mensagemErro.isEmpty()) { %> <div class="alert-danger">⚠️ <%= mensagemErro %></div> <% } %>

    <div class="card">
        <h2><%= modoEdicao ? "🔄 Atualizar Registo Local (@" + editUsername + ")" : "👤 Criar Novo Registo Local" %></h2>
        <form action="UserServlet?action=save" method="post" enctype="multipart/form-data" style="margin-top: 15px;">
            <input type="hidden" name="userid" value="<%= editUserid %>">
            
            <div class="row" style="align-items: center;">
                <div class="col" style="flex: 0 0 150px; text-align: center;">
                    <label style="margin-bottom: 8px;">Fotografia</label>
                    <div class="avatar-dropzone-wrapper" id="avatarWrapper" 
                         title="Zona de Atualização de Fotografia. Arraste e solte um ficheiro de imagem aqui dentro ou clique para procurar localmente.">
                        <% 
                            String srcExibicao = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 24 24' fill='%23a0aec0'><path d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5-4-8-4z'/></svg>";
                            if (modoEdicao && !editPhotoDataUri.isEmpty()) {
                                srcExibicao = editPhotoDataUri;
                            }
                        %>
                        <img src="<%= srcExibicao %>" id="avatarPreviewImg" class="avatar-img-preview" alt="Avatar Preview">
                        <div class="avatar-dropzone-overlay">
                            <span id="overlayText">Largar foto ou clicar aqui</span>
                        </div>
                        <input type="file" id="photoFileInput" name="photoFile" accept="image/*" style="display: none;" 
                               title="Seletor de ficheiro de imagem para o perfil">
                    </div>
                </div>
        
                <div class="col form-group">
                    <label>Username *</label>
                    <input type="text" 
                           name="username" 
                           value="<%= editUsername %>" 
                           required 
                           maxlength="10"
                           pattern="[a-zA-Z0-9_-]{4,10}"
                           title="O username deve ter entre 4 e 10 carateres e conter apenas letras (a-z, A-Z), números (0-9), hífen (-) ou underscore (_)." 
                           placeholder="Ex: joao_99">
                </div>
        
                <div class="col form-group">
                    <label>Correio Eletrónico *</label>
                    <input type="email" 
                           name="email" 
                           value="<%= editEmail %>" 
                           required 
                           title="Introduza um endereço de email válido e único para comunicações." 
                           placeholder="Ex: joao.silva@dominio.pt">
                </div>
        
                <div class="col form-group">
                    <label>Password <%= modoEdicao ? "(Vazio para manter)" : "*" %></label>
                    <%
                        String pwdPlaceholder = modoEdicao ? "Deixe em branco para não alterar" : "Ex: P@ssw0rdSegura!";
                        String pwdRequired = modoEdicao ? "" : "required";
                    %>
                    <div class="password-wrapper">
						<input type="password" 
		                       name="password" 
		                       placeholder="Mínimo 8 carateres" 
		                       required 
		                       minlength="8" 
		                       title="A password tem de ter pelo menos 8 carateres.">
                        
                        <button type="button" 
                                id="idTogglePasswordBtn"
                                class="password-toggle" 
                                onclick="toggleVisualizacaoPassword()" 
                                title="Clique aqui para mostrar ou ocultar os carateres da password em texto limpo.">👁️</button>
                    </div>
                    <small id="passwordHelp" style="display: block; margin-top: 5px; color: #718096; font-size: 0.8em;"></small>
                </div>
            </div>
        
            <div class="row">
                <div class="col form-group">
                    <label>Nomes Próprios *</label>
                    <input type="text" 
                           name="firstnames" 
                           value="<%= editFirstnames %>" 
                           required 
                           maxlength="60"
                           pattern="[a-zA-Zà-úÀ-Ú\s',-]{1,60}"
                           title="O nome próprio pode ter até 60 carateres e deve conter apenas letras (com ou sem acentos), espaços, hífens ou apóstrofos." 
                           placeholder="Ex: João Pedro">
                </div>
        
                <div class="col form-group">
                    <label>Apelidos *</label>
                    <input type="text" 
                           name="lastnames" 
                           value="<%= editLastnames %>" 
                           required 
                           maxlength="60"
                           pattern="[a-zA-Zà-úÀ-Ú\s'-]{1,60}"
                           title="Os apelidos devem ter entre 1 e 60 carateres e podem conter apenas letras (com ou sem acentos), espaços, hifens ou apóstrofos." 
                           placeholder="Ex: Silva Santos">
                </div>
        
                <div class="col form-group">
                    <label>Data de Nascimento *</label>
                    <input type="date" 
                           id="idBirthdateInput" 
                           name="birthdate" 
                           max="<%= dataHojeIso %>" 
                           value="<%= editBirthdate %>" 
                           onchange="atualizarIdadeOutput(this.value)" 
                           required 
                           title="Selecione a data de nascimento do utilizador." 
                           placeholder="Ex: 1995-05-15">
                </div>
        
                <div class="col form-group" style="flex: 0 0 120px;">
                    <label>Idade</label>
                    <div class="age-container">
                        <output id="idOutputIdade" class="age-output" title="Idade calculada a partir da data de nascimento."><%= calcularIdadeJava.apply(editBirthdate) %></output>
                    </div>
                </div>
            </div>
        
            <div class="row">
                <div class="col form-group">
                    <label>Nível de Perfil *</label>
                    <div class="range-container">
                        <input type="range" 
                               class="range-slider" 
                               name="profile" 
                               min="0" 
                               max="10" 
                               value="<%= editProfile %>" 
                               oninput="idOutputPerfil.value = this.value" 
                               title="Desloque o cursor para definir o nível de perfil/privilégios de autorização de 0 a 10.">
                        <output id="idOutputPerfil" class="range-output" title="Nível numérico atualmente selecionado."><%= editProfile %></output>
                    </div>
                </div>
                
                <div class="col form-group">
                    <label>Nacionalidade *</label>
                    <%
                        String valorExibicaoNat = editNationality;
                        // Se for uma edição e tivermos apenas a sigla (ex: PT), formata para o utilizador ver a bandeira e o nome
                        if(modoEdicao && !editNationality.isEmpty() && mapaNacionalidadesNome.containsKey(editNationality)) {
                            valorExibicaoNat = isoParaEmojiBandeira.apply(editNationality) + " [" + editNationality + "] " + mapaNacionalidadesNome.get(editNationality);
                        }
                    %>
                    <input type="text" 
                           name="nationality" 
                           list="listaPaises" 
                           value="<%= valorExibicaoNat %>" 
                           required 
                           pattern=".*\[[A-Za-z]{2,3}\].*" 
                           title="A nacionalidade tem de incluir obrigatoriamente o código ISO do país entre parênteses retos. Exemplo: [PT]" 
                           placeholder="Ex: 🇵🇹 [PT] Portugal">
                    
                    <datalist id="listaPaises">
                        <% for (Map.Entry<String, String> entry : mapaNacionalidadesNome.entrySet()) { %>
                            <option value="<%= isoParaEmojiBandeira.apply(entry.getKey()) %> [<%= entry.getKey() %>] <%= entry.getValue() %>"></option>
                        <% } %>
                    </datalist>
                </div>
        
                <div class="col form-group">
                    <label>Género</label>
                    <div class="radio-group" title="Selecione a identidade de género do utilizador: M para Masculino, F para Feminino, X para Não especificado.">
                        <label class="radio-option"><input type="radio" name="gender" value="M" <%= editGender.equals("M") ? "checked" : "" %> title="Género: Masculino"> M</label>
                        <label class="radio-option"><input type="radio" name="gender" value="F" <%= editGender.equals("F") ? "checked" : "" %> title="Género: Feminino"> F</label>
                        <label class="radio-option"><input type="radio" name="gender" value="X" <%= editGender.equals("X") ? "checked" : "" %> title="Género: Indefinido / Outro"> X</label>
                    </div>
                </div>

                <div class="col form-group">
                    <label>Cor do Tabuleiro *</label>
                    <input type="color" 
                           name="corFundo" 
                           value="<%= editCorFundo %>" 
                           style="height: 42px; padding: 2px; cursor: pointer;"
                           title="Escolha a sua cor favorita para o fundo da janela do jogo.">
                </div>
        
                <div class="col form-group">
                    <label class="checkbox-container" title="Marque esta opção para suspender imediatamente o acesso do utilizador à plataforma.">
                        <input type="checkbox" name="blocked" value="true" <%= editBlocked.equals("true") ? "checked" : "" %> title="Estado de Bloqueio da Conta"> 🛑 Bloquear Conta
                    </label>
                </div>
            </div>
        
            <div style="margin-top: 15px;">
                <button type="submit" class="btn btn-primary" title="Clique para submeter o formulário e persistir os dados no documento XML.">Gravar Registo</button>
                <% if (modoEdicao) { %> 
                    <a href="<%= paginaAtual %>" class="btn btn-secondary" title="Clique para descartar as alterações atuais e regressar ao modo de inserção.">Cancelar</a> 
                <% } %>
            </div>
        </form>
    </div>

    <div class="card">
        <h2>📋 Utilizadores Registados no Sistema</h2>
        <table>
            <thead>
                <tr>
                    <th>Fotografia</th>
                    <th>Username</th>
                    <th>Nome Completo</th>
                    <th>Email</th>
                    <th>Nascimento</th>
                    <th>Perfil</th>
                    <th>Género</th>
                    <th>Nacionalidade</th>
                    <th>Estado</th>
                    <th>Ações</th>
                </tr>
            </thead>
            <tbody>
                <% if (listaUtilizadores.isEmpty()) { %>
                    <tr><td colspan="10" style="text-align: center; padding: 20px; color: #a0aec0;">Nenhum registo disponível.</td></tr>
                <% } else { 
                    for (Element user : listaUtilizadores) { 
                        String uid = user.getElementsByTagName("userid").item(0).getTextContent();
                        String uname = user.getElementsByTagName("username").item(0).getTextContent();
                        String fNames = user.getElementsByTagName("firstnames").item(0).getTextContent();
                        String lNames = user.getElementsByTagName("lastnames").item(0).getTextContent();
                        String uEmail = user.getElementsByTagName("email").item(0).getTextContent();
                        String uProfile = user.getElementsByTagName("profile").item(0).getTextContent();
                        String uGender = (user.getElementsByTagName("gender").getLength() > 0) ? user.getElementsByTagName("gender").item(0).getTextContent() : "";
                        String uNat = (user.getElementsByTagName("nationality").getLength() > 0) ? user.getElementsByTagName("nationality").item(0).getTextContent().trim().toUpperCase() : "N/D";                        String isBlocked = (user.getElementsByTagName("blocked").getLength() > 0) ? user.getElementsByTagName("blocked").item(0).getTextContent() : "false";
                        String uBirth = (user.getElementsByTagName("birthdate").getLength() > 0) ? user.getElementsByTagName("birthdate").item(0).getTextContent().trim() : "";
                        
                        String rawXmlPhoto = (user.getElementsByTagName("photography").getLength() > 0) ? user.getElementsByTagName("photography").item(0).getTextContent().trim() : "";
                        String currentGridPhotoDataUri = "";
                        if (!rawXmlPhoto.isEmpty()) {
                            if (!rawXmlPhoto.startsWith("data:image/")) {
                                currentGridPhotoDataUri = "data:image/jpeg;base64," + rawXmlPhoto;
                            } else {
                                currentGridPhotoDataUri = rawXmlPhoto;
                            }
                        }
                %>
                    <tr>
						<td>
						    <div class="user-avatar-container" title="Fotografia de perfil">
						        <% 
						            rawXmlPhoto = (user.getElementsByTagName("photography").getLength() > 0) ? user.getElementsByTagName("photography").item(0).getTextContent().trim() : "";
						            // Se estiver vazio, usa o default.jpg que está na mesma pasta
						            String srcFoto = (!rawXmlPhoto.isEmpty()) ? 
						                             (rawXmlPhoto.startsWith("data:image/") ? rawXmlPhoto : "data:image/jpeg;base64," + rawXmlPhoto) : 
						                             "default.jpg";
						        %>
						        <img src="<%= srcFoto %>" class="user-avatar" alt="Avatar">
						    </div>
						</td>
                        <td><strong>@<%= uname %></strong></td>
                        <td><%= fNames + " " + lNames %></td>
                        <td><%= uEmail %></td>
                        <td><%= uBirth.isEmpty() ? "N/D" : uBirth + " (" + calcularIdadeJava.apply(uBirth) + ")" %></td>
                        <td><span class="badge" style="background: #319795;"><%= uProfile %></span></td>
                        <td><%= uGender.equals("M") ? "👨 M" : uGender.equals("F") ? "👩 F" : "👤 X" %></td>
                        <td>
                            <div style="display: flex; align-items: center; gap: 8px;">
                                <% String flagB64 = mapaNacionalidadesBandeira.get(uNat);
                                   if(flagB64 != null && !flagB64.isEmpty()) { %>
                                    <img src="data:image/png;base64,<%= flagB64 %>" class="flag-img" alt="Bandeira">
                                <% } %>
                                <span><%= mapaNacionalidadesNome.getOrDefault(uNat, uNat) %></span>
                            </div>
                        </td>
                        <td><%= isBlocked.equals("true") ? "<span class='badge' style='background:#e53e3e;'>Bloqueado 🛑</span>" : "<span class='badge' style='background:#38a169;'>Ativo</span>" %></td>
                        <td>
                            <a href="<%= paginaAtual %>?action=edit&id=<%= uid %>" class="btn btn-primary" style="padding:4px 8px; font-size:0.8em;" title="Carregar os dados deste utilizador no formulário de edição acima.">Editar</a>
                            <a href="UserServlet?action=delete&userid=<%= uid %>" class="btn btn-danger" style="padding:4px 8px; font-size:0.8em;" title="Eliminar definitivamente este registo do ficheiro XML." onclick="return confirm('Confirmar a remoção do utilizador @<%= uname %>?');">Remover</a>
                        </td>
                    </tr>
                <% } } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>