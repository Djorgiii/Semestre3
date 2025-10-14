import java.util.concurrent.Semaphore;

public class BufferCircular {
	final int dimensaoBuffer= 5;
	Comando[] bufferCircular;
	int putBuffer, getBuffer;
	// o semáforo elementosLivres indica se há posições livres para inserir Strings
	// o semáforo acessoElemento garante exclusão mútua no acesso a um elemento
	// o semáforo elementosOcupados indica se há posições com Strings válidas
	Semaphore elementosLivres, acessoElemento, elementosOcupados;

	public BufferCircular(){
		 bufferCircular= new Comando[dimensaoBuffer];
		 putBuffer= 0;
		 getBuffer= 0;
		 elementosLivres= new Semaphore(dimensaoBuffer);
		 elementosOcupados= new Semaphore(0);
		 acessoElemento= new Semaphore(1);
	}
	
	public void inserirElemento(Comando s){
		try {
		 elementosLivres.acquire();
		 acessoElemento.acquire();
		 bufferCircular[putBuffer]= new Comando(s.getTipo(), s.getArg1(), s.getArg2());
		 putBuffer= ++putBuffer % dimensaoBuffer;
		 acessoElemento.release();
		} catch (InterruptedException e) {}
		 elementosOcupados.release();
		}
	
		public Comando removerElemento() {
		 Comando s= null;
		 try {
		 elementosOcupados.acquire();
		 acessoElemento.acquire();
		 } catch (InterruptedException e) {}
		 s =  bufferCircular[getBuffer];
		 getBuffer= ++getBuffer % dimensaoBuffer;
		 acessoElemento.release();
		 elementosLivres.release();
		 return s;
		}

}