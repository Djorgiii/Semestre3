package peer;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🗃️ <b>Classe GestorNos (Membership Service)</b>
 * <p>Serviço centralizado para a gestão de pertença de nós na arquitetura distribuída.</p>
 * <p>Implementa um detetor de falhas por timeout e mantém a consistência da visão local.</p>
 * * @author Engº P. Filipe
 * @version 1.0
 */
public class GestorNos {
    
    /** 🧠 Repositório de nós ativos, utilizando concorrência para segurança entre threads */
    private final ConcurrentHashMap<String, NoRemoto> nosAtivos = new ConcurrentHashMap<>();
    
    /** ⏱️ Intervalo de tolerância antes de declarar falha de um nó (15 segundos) */
    private static final long TIMEOUT_FALHA = 1000*15;

    /**
     * ➕ <b>Registar ou Atualizar Nó</b>
     * <p>Integra um novo componente na arquitetura ou renova o sinal de vida de um existente.</p>
     * @param id Identificador único do nó.
     * @param ip Endereço IP do recurso.
     * @param porto Porto de serviço do recurso.
     */
    public void atualizarOuAdicionarNo(String id, String ip, int porto) {
        NoRemoto existente = nosAtivos.get(id);
        if (existente != null) {
            existente.renovarPresenca(); // 💓 Heartbeat confirmado
        } else {
            nosAtivos.put(id, new NoRemoto(ip, porto)); // ✨ Novo recurso federado
        }
    }

    /**
     * 🧹 <b>Detetor de Falhas (Failure Detector)</b>
     * <p>Remove componentes que deixaram de emitir sinais de vida, garantindo 
     * que a arquitetura não direcione pedidos para nós inacessíveis.</p>
     */
    public void limparNosInativos() {
        nosAtivos.entrySet().removeIf(entry -> !entry.getValue().estaAtivo(TIMEOUT_FALHA));
    }

    /**
     * 📋 <b>Obter Vista da Arquitetura</b>
     * @return Uma snapshot imutável de todos os nós atualmente operacionais.
     */
    public Map<String, NoRemoto> getNosAtivos() {
        return Collections.unmodifiableMap(nosAtivos);
    }

    /** @return {@code true} se não existirem outros nós detetados na arquitetura. */
    public boolean estaIsolado() {
        return nosAtivos.isEmpty();
    }

    /** * 🎯 <b>Selecionar Nó Alvo</b>
     * @return O primeiro nó disponível para interação ou {@code null} se isolado.
     */
    public NoRemoto getNoAlvo() {
        return nosAtivos.values().stream().findFirst().orElse(null);
    }
    
    /**
     * 📜 <b>Listar Arquitetura Ativa</b>
     * <p>Retorna uma representação textual de todos os nós que ainda são 
     * considerados operacionais. Executa uma limpeza automática antes da listagem.</p>
     * @return String com o relatório de todos os nós ativos ou mensagem de isolamento.
     */
    public String listar() {
        // 1. Garantir que a vista está atualizada removendo os inativos
        limparNosInativos();

        if (estaIsolado()) {
            return "📭 Sem nós ativos na arquitetura de momento.";
        }

        StringBuilder relatorio = new StringBuilder();
        relatorio.append(" === 📊 COMPOSIÇÃO DA ARQUITETURA ===\n");
        
        // 2. Iterar sobre os nós que restaram (os ativos)
        nosAtivos.forEach((id, no) -> {
            relatorio.append(String.format("🆔 ID: %s%n", id));
            relatorio.append(no.toString()); // Reutiliza o método mostra() da classe NoRemoto
            relatorio.append("\n");
        });

        relatorio.append("=====================================");
        return relatorio.toString();
    }
    
    /**
     * 🔍 <b>Procurar Nó por ID</b>
     * <p>Retorna a instância do nó se este estiver ativo, ou {@code null} se tiver 
     * expirado ou não constar no registo.</p>
     * @param id Identificador único do nó.
     * @return O objeto {@link NoRemoto} ou {@code null}.
     */
    public NoRemoto getNoPorId(String id) {
        // Remove nós que ultrapassaram o TIMEOUT_FALHA
        limparNosInativos();
        
        // O ConcurrentHashMap retorna null automaticamente se a chave não existir
        return nosAtivos.get(id);
    }
}