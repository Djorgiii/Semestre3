import java.util.concurrent.Semaphore;

public class BufferGravacao {

    private final int dimensaoBuffer;
    private Movimento[] buffer;
    private int putBuffer, getBuffer, count;

    private final Semaphore elementosLivres;
    private final Semaphore elementosOcupados;
    private final Semaphore acessoElemento;

    public BufferGravacao(int tamanho) {
        this.dimensaoBuffer = tamanho;
        buffer = new Movimento[tamanho];
        putBuffer = 0;
        getBuffer = 0;
        count = 0;

        elementosLivres = new Semaphore(tamanho, true);
        elementosOcupados = new Semaphore(0, true);
        acessoElemento = new Semaphore(1, true);
    }

    public BufferGravacao() {
        this(128);
    }

    public void clear() {
        try {
            acessoElemento.acquire();
            elementosOcupados.drainPermits();
            elementosLivres.drainPermits();
            elementosLivres.release(dimensaoBuffer);

            putBuffer = 0;
            getBuffer = 0;
            count = 0;

            for (int i = 0; i < dimensaoBuffer; i++) buffer[i] = null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            acessoElemento.release();
        }
    }

    public void inserirElemento(Movimento m) {
        try {
            elementosLivres.acquire();
            acessoElemento.acquire();

            buffer[putBuffer] = new Movimento(m.getTipo(), m.getArg1(), m.getArg2());
            putBuffer = (putBuffer + 1) % dimensaoBuffer;
            count++;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            acessoElemento.release();
            elementosOcupados.release();
        }
    }

    public Movimento removerElemento() {
        Movimento m = null;
        try {
            elementosOcupados.acquire();
            acessoElemento.acquire();

            m = buffer[getBuffer];
            buffer[getBuffer] = null;
            getBuffer = (getBuffer + 1) % dimensaoBuffer;
            count--;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            acessoElemento.release();
            elementosLivres.release();
        }
        return m;
    }

    public int getCount() {
        int c = 0;
        try {
            acessoElemento.acquire();
            c = count;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            acessoElemento.release();
        }
        return c;
    }

    public boolean isVazio() {
        return getCount() == 0;
    }
}
