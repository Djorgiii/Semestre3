package peer;

/**
 * 📦 <b>Classe NoRemoto</b>
 * <p>Representa a abstração de um nó remoto dentro da arquitetura distribuída.</p>
 * <p>Esta classe armazena os metadados de ligação e gere o estado de vitalidade 
 * (liveness) através de timestamps de atividade.</p>
 * * @author Engº P. Filipe
 * @version 1.0
 */
public class NoRemoto {
    /** 🌐 Endereço IP do nó na arquitetura distribuída */
    private final String ip;
    /** 🔢 Porto TCP onde o nó disponibiliza o seu serviço de dados */
    private final int porto;
    /** 🕒 Carimbo temporal da última atividade detetada (Heartbeat) */
    private long ultimaVezVisto;

    /**
     * 🏗️ <b>Construtor do Nó Remoto</b>
     * @param ip Endereço IP do componente externo.
     * @param porto Porto de serviço do componente externo.
     */
    public NoRemoto(String ip, int porto) {
        this.ip = ip;
        this.porto = porto;
        this.ultimaVezVisto = System.currentTimeMillis();
    }

    /** @return O endereço IP do nó. */
    public String getIp() { return ip; }

    /** @return O porto do serviço de dados. */
    public int getPorto() { return porto; }

    /** * ⏱️ <b>Renovar Liveness</b>
     * <p>Atualiza o registo de atividade para o instante atual, confirmando que 
     * o nó ainda faz parte da arquitetura ativa.</p>
     */
    public void renovarPresenca() {
        this.ultimaVezVisto = System.currentTimeMillis();
    }

    /**
     * ✅ <b>Verificar Estado Operacional</b>
     * <p>Avalia se o nó deve ser mantido na arquitetura com base num intervalo de tolerância.</p>
     * @param timeout Limite de tempo em milissegundos (ex: 15000 para 15s).
     * @return {@code true} se o nó for considerado ativo.
     */
    public boolean estaAtivo(long timeout) {
        return (System.currentTimeMillis() - this.ultimaVezVisto) < timeout;
    }

    @Override
    public String toString() {
        long segundosPassados = (System.currentTimeMillis() - this.ultimaVezVisto) / 1000;
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🌐 Endereço IP: %s ", this.ip));
        sb.append(String.format("🔢 Porto TCP:   %d ", this.porto));
        sb.append(String.format("🕒 Atividade:   há %d segundos", segundosPassados));
        
        return sb.toString();
    }
}