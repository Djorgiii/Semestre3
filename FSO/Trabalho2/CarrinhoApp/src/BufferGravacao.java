import java.util.concurrent.Semaphore;

public class BufferGravacao {

    private final int dimensaoBuffer;
    private Movimento[] buffer;
    private int putBuffer, getBuffer, count;

    private Semaphore elementosLivres;
    private Semaphore elementosOcupados;
    private Semaphore acessoElemento;

    public BufferGravacao(int tamanho) {
        this.dimensaoBuffer = tamanho;
        buffer = new Movimento[dimensaoBuffer];
        putBuffer = 0;
        getBuffer = 0;
        count = 0;
        elementosLivres = new Semaphore(dimensaoBuffer, true);
        elementosOcupados = new Semaphore(0, true);
        acessoElemento = new Semaphore(1, true);
    }

    public BufferGravacao() {
        this(64); // tamanho default
    }

    public void clear() {
        acessoElemento.acquireUninterruptibly();
        try {
            elementosOcupados.drainPermits();
            for (int i = 0; i < dimensaoBuffer; i++) buffer[i] = null;
            putBuffer = 0;
            getBuffer = 0;
            count = 0;

            elementosLivres.drainPermits();
            elementosLivres.release(dimensaoBuffer);
        } finally {
            acessoElemento.release();
        }
    }

    public void inserirElemento(Movimento m) {
        elementosLivres.acquireUninterruptibly();
        acessoElemento.acquireUninterruptibly();
        try {
            Movimento copia = new Movimento(m.getTipo(), m.getArg1(), m.getArg2());
            if (m.isManual()) copia.setManual(true);

            buffer[putBuffer] = copia;
            putBuffer = (putBuffer + 1) % dimensaoBuffer;
            count++;
        } finally {
            acessoElemento.release();
            elementosOcupados.release();
        }
    }

    public Movimento removerElemento() {
        elementosOcupados.acquireUninterruptibly();
        acessoElemento.acquireUninterruptibly();
        try {
            Movimento m = buffer[getBuffer];
            buffer[getBuffer] = null;
            getBuffer = (getBuffer + 1) % dimensaoBuffer;
            count--;
            return m;
        } finally {
            acessoElemento.release();
            elementosLivres.release();
        }
    }

    public boolean isVazio() {
        return getCount() == 0;
    }

    public int getCount() {
        acessoElemento.acquireUninterruptibly();
        try {
            return count;
        } finally {
            acessoElemento.release();
        }
    }
}
