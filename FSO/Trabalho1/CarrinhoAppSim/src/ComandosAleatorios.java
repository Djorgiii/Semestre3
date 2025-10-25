public class ComandosAleatorios extends Tarefa {
    private GUI gui;

    public ComandosAleatorios(GUI gui) {
        super(null);
        this.gui = gui;
    }

    public void execucao() {
        if (gui != null && gui.getBd() != null && gui.getBd().getServidor() != null) {
            gui.getBd().getServidor().resetContadorAleatorios();
        }
        String[] tipos = {"PARAR", "RETA", "CURVARDIREITA", "CURVARESQUERDA"};
        for (int i = 0; i < 5; i++) {
            String tipo = tipos[(int)(Math.random() * tipos.length)];
            Comando comando;
            if (tipo.equals("PARAR")) {
                comando = new Comando(tipo, false);
            } else if (tipo.equals("RETA")) {
                int distancia = 10 + (int)(Math.random() * 41);
                comando = new Comando(tipo, distancia, 0);
            } else {
                int raio = 10 + (int)(Math.random() * 21);
                int angulo = 20 + (int)(Math.random() * 71);
                comando = new Comando(tipo, angulo, raio);
            }
            // Insert the command via the GUI so it goes through GUI -> BaseDados -> Servidor
            if (gui != null) gui.inserirComandoNoBuffer(comando);
            dormir();
        }
        bloquear();
    }

}