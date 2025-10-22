import java.util.concurrent.Semaphore;

public class BufferCircular {
	final int dimensaoBuffer= 5;
	Comando[] bufferCircular;
	int putBuffer, getBuffer;
	// o semáforo elementosLivres indica se há posições livres para inserir Strings
	// o semáforo acessoElemento garante exclusão mútua no acesso a um elemento
	// o semáforo elementosOcupados indica se há posições com Strings válidas
	Semaphore elementosLivres, acessoElemento, elementosOcupados;
	private int lastRemovedIndex = -1;

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
		 System.out.println("Inserido no buffer[" + putBuffer + "]: " + s);
		 // Estado do buffer após inserção
		 System.out.print("Estado do buffer após inserção: ");
		 for (int i = 0; i < dimensaoBuffer; i++) {
			 System.out.print(bufferCircular[i] + " | ");
		 }
		 System.out.println();
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
			lastRemovedIndex = getBuffer;
			s =  bufferCircular[getBuffer];
			bufferCircular[getBuffer] = null;
			System.out.println("Removido do buffer[" + getBuffer + "]: " + s);
			// Estado do buffer após remoção
			System.out.print("Estado do buffer após remoção: ");
			for (int i = 0; i < dimensaoBuffer; i++) {
				System.out.print(bufferCircular[i] + " | ");
			}
			System.out.println();
			getBuffer= ++getBuffer % dimensaoBuffer;
			acessoElemento.release();
			elementosLivres.release();
			return s;
		}

		public int getLastRemovedIndex() {
			return lastRemovedIndex;
		}

}