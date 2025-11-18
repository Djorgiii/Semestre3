import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import java.awt.Font;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Button;

public class GuiGravador extends JFrame {
	private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    private BaseDados bd;
    private JButton btnFrente;
    private JRadioButton rdbtnOnOff;
    private JTextField textFieldDistancia;
    private JTextArea textAreaConsola;
    private JLabel lblRaio;
    private JTextField textFieldRaio;
    private JTextField textFieldAngulo;
    private JTextField textFieldRobot;
    private BufferCircular bufferCircular;
    private MovimentosAleatorios movimentosAleatorios;
    private Movimento movimentoPendente;
    private EvitarObstaculo tObstaculo;
    private JTextField textField;
    
    
    public void myPrint(String s) {
		textAreaConsola.append(s + "\n");
	}

    public void setTarefas(MovimentosAleatorios tAleatorios) {
        this.movimentosAleatorios = tAleatorios;
    }

    public void pedirMovimentoManual(Movimento c) {
    	if (c == null) {
    		return;
    	}
    	
    	c.setManual(true);
    	java.util.concurrent.Semaphore mux = bd.getProdutorMux();
		if (mux.tryAcquire()) {
			try {
				bufferCircular.inserirElemento(c);
			}finally {
				mux.release();
			}
		}
		else {
			synchronized (this) {
				this.movimentoPendente = c;
			}
			myPrint("[GUI] Comando manual guardado como pendente: " + c.getTipo());
		}
    }

    public synchronized Movimento obterMovimentoManual() {
        Movimento tmp = movimentoPendente;
        movimentoPendente = null;
        return tmp;
    }

    
    /**
     * Create the frame.
     */
    public GuiGravador() {
        bd = new BaseDados();
        bufferCircular = new BufferCircular();

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try { 
                    addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent arg0) {
                            if (bd.isRobotAberto()) {
                                bd.getRobot().CloseEV3();
                            }
                            bd.setTerminar(true);
                        }
                    });

                    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    setBounds(100, 100, 619, 569);
                    contentPane = new JPanel();
                    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
                    setContentPane(contentPane);
                    contentPane.setLayout(null);

                    // Botão Fazer Reta
                    btnFrente = new JButton("FRENTE");
                    btnFrente.setForeground(new Color(0, 0, 0));
                    btnFrente.setBackground(new Color(128, 255, 128));
                    btnFrente.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnFrente.setEnabled(false);
                    btnFrente.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            if (!bd.isRobotAberto()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                            pedirMovimentoManual(new Movimento("RETA", bd.getDistancia(), 0));
                        }
                    });
                    
                    btnFrente.setBounds(242, 84, 105, 37);
                    contentPane.add(btnFrente);

                    // Botão On/Off
                    rdbtnOnOff = new JRadioButton("Abrir/Fechar");
                    rdbtnOnOff.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    rdbtnOnOff.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (bd.isRobotAberto()) {
                                bd.getRobot().CloseEV3();
                                bd.setRobotAberto(false);
                            } else {
                                bd.setRobotAberto(bd.getRobot().OpenEV3("EV2"));
                                tObstaculo.desbloquear();
                                
                            }
                            rdbtnOnOff.setSelected(bd.isRobotAberto());
                            myPrint("O Robot foi " + (bd.isRobotAberto()? "aberto": "fechado" +"."));
                            btnFrente.setEnabled(bd.isRobotAberto());
                        }
                    });
                    rdbtnOnOff.setBounds(471, 35, 94, 21);
                    contentPane.add(rdbtnOnOff);
                    
                    JLabel lblDistancia = new JLabel("Distância");
                    lblDistancia.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblDistancia.setBounds(203, 9, 81, 21);
                    contentPane.add(lblDistancia);
                    
                    textFieldDistancia = new JTextField();
                    textFieldDistancia.setText("33");
                    textFieldDistancia.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    textFieldDistancia.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent arg0) {
                            if (!bd.isRobotAberto()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                    		bd.setDistancia(Integer.parseInt(textFieldDistancia.getText()));
                    		myPrint("A distância foi alterada para " + bd.getDistancia() + " cm.");
                    		
                    	}
                    });
                    textFieldDistancia.setBounds(277, 9, 34, 21);
                    contentPane.add(textFieldDistancia);
                    textFieldDistancia.setColumns(10);
                    
                    JScrollPane scrollPane = new JScrollPane();
                    scrollPane.setBounds(10, 399, 567, 123);
                    contentPane.add(scrollPane);
                    
                    textAreaConsola = new JTextArea();
                    scrollPane.setViewportView(textAreaConsola);
                    
                    lblRaio = new JLabel("Raio");
                    lblRaio.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblRaio.setBounds(10, 10, 45, 18);
                    contentPane.add(lblRaio);
                    
                    textFieldRaio = new JTextField();
                    textFieldRaio.setText("20");
                    textFieldRaio.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                            if (!bd.isRobotAberto()) {
                                myPrint("Abra o robot antes de escrever.");
                                return;
                            }
                    		bd.setRaio(Integer.parseInt(textFieldRaio.getText()));
                    		myPrint("O Raio foi alterado para " + bd.getRaio() + " cm.");
                    	}
                    });
                    textFieldRaio.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    textFieldRaio.setBounds(50, 10, 34, 19);
                    contentPane.add(textFieldRaio);
                    textFieldRaio.setColumns(10);
                    
                    JLabel lblAngulo = new JLabel("Ângulo");
                    lblAngulo.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblAngulo.setBounds(94, 4, 69, 30);
                    contentPane.add(lblAngulo);
                    
                    textFieldAngulo = new JTextField();
                    textFieldAngulo.setText("90");
                    textFieldAngulo.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                            if (!bd.isRobotAberto()) {
                                textFieldRaio.setEditable(false);
                                myPrint("Abra o robot antes de escrever.");
                                return;
                            }
                    		bd.setAngulo(Integer.parseInt(textFieldAngulo.getText()));
                    		myPrint("O Ângulo foi alterado para " + bd.getAngulo() + " graus.");
                    	}
                    });
                    textFieldAngulo.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    textFieldAngulo.setColumns(10);
                    textFieldAngulo.setBounds(159, 10, 34, 19);
                    contentPane.add(textFieldAngulo);
                    
                    JLabel lblRobot = new JLabel("Robot");
                    lblRobot.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblRobot.setBounds(471, 4, 57, 30);
                    contentPane.add(lblRobot);
                    
                    textFieldRobot = new JTextField();
                    textFieldRobot.setEditable(false);
                    textFieldRobot.setText("EV2");
                    textFieldRobot.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    textFieldRobot.setColumns(10);
                    textFieldRobot.setBounds(527, 9, 34, 19);
                    contentPane.add(textFieldRobot);
                    
                    JButton btnParar = new JButton("PARAR");
                    btnParar.setBackground(new Color(255, 0, 0));
                    btnParar.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent arg0) {
                            if (!bd.isRobotAberto()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                    		pedirMovimentoManual(new Movimento("PARAR", false));
                    	}
                    });
                    btnParar.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnParar.setBounds(242, 119, 105, 37);
                    contentPane.add(btnParar);
                    
                    JButton btnDireita = new JButton("DIREITA");
                    btnDireita.setBackground(new Color(0, 128, 255));
                    btnDireita.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                            if (!bd.isRobotAberto()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                    		pedirMovimentoManual(new Movimento("CURVARDIREITA", bd.getRaio(), bd.getAngulo()));
                    	}
                    });
                    btnDireita.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnDireita.setBounds(345, 119, 117, 37);
                    contentPane.add(btnDireita);
                    
                    JButton btnEsquerda = new JButton("ESQUERDA");
                    btnEsquerda.setBackground(new Color(255, 128, 255));
                    btnEsquerda.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                            if (!bd.isRobotAberto()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                    		pedirMovimentoManual(new Movimento("CURVARESQUERDA", bd.getRaio(), bd.getAngulo()));
                    	}
                    });
                    btnEsquerda.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnEsquerda.setBounds(134, 119, 111, 37);
                    contentPane.add(btnEsquerda);
                    
                    JButton btnTras = new JButton("TRÁS");
                    btnTras.setBackground(new Color(255, 128, 128));
                    btnTras.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                            if (!bd.isRobotAberto()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                    		pedirMovimentoManual(new Movimento("RETA", -bd.getDistancia(), 0));
                    	}
                    });
                    btnTras.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnTras.setBounds(242, 154, 105, 37);
                    contentPane.add(btnTras);
                    
                    JLabel lblConsola = new JLabel("Consola");
                    lblConsola.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblConsola.setBounds(10, 371, 67, 18);
                    contentPane.add(lblConsola);
                    
                    JRadioButton rdbtnMovimentosAleatrios = new JRadioButton("Movimentos Aleatórios");
                    rdbtnMovimentosAleatrios.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (rdbtnMovimentosAleatrios.isSelected()) {
                                if (!bd.isRobotAberto()) {
                                    myPrint("Abra o robot antes de executar movimentos aleatórios.");
                                    return;
                                }
                                bd.setAleatoriosOn(true);
                                if (movimentosAleatorios != null) {
                                    // Pass the GUI reference so the task must go through GUI -> BaseDados -> Servidor
                                    movimentosAleatorios.desbloquear();
                                }
                            }
                            else {
                            	bd.setAleatoriosOn(false);
                            	if (bufferCircular != null) {
									bufferCircular.clear();
								}
                            }
                        }
                    });
                    rdbtnMovimentosAleatrios.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    rdbtnMovimentosAleatrios.setBounds(378, 200, 221, 21);
                    contentPane.add(rdbtnMovimentosAleatrios);
                    
                    JLabel lblGravador = new JLabel("Gravador");
                    lblGravador.setBackground(new Color(0, 128, 255));
                    lblGravador.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblGravador.setBounds(10, 244, 81, 18);
                    contentPane.add(lblGravador);
                    
                    JLabel lblFicheiro_1 = new JLabel("Ficheiro");
                    lblFicheiro_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
                    lblFicheiro_1.setBackground(new Color(0, 128, 255));
                    lblFicheiro_1.setBounds(10, 272, 57, 19);
                    contentPane.add(lblFicheiro_1);
                    
                    textField = new JTextField();
                    textField.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    textField.setColumns(10);
                    textField.setBounds(88, 270, 386, 19);
                    contentPane.add(textField);
                    
                    JButton btnGravar = new JButton("Gravar");
                    btnGravar.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    btnGravar.setForeground(new Color(0, 0, 0));
                    btnGravar.setBounds(181, 299, 84, 20);
                    contentPane.add(btnGravar);
                    
                    JButton btnReproduzir = new JButton("Reproduzir");
                    btnReproduzir.setForeground(new Color(0, 0, 0));
                    btnReproduzir.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    btnReproduzir.setBounds(304, 299, 94, 20);
                    contentPane.add(btnReproduzir);
                    
                    JButton btnBotaoFicheiro = new JButton("...");
                    btnBotaoFicheiro.setForeground(new Color(0, 0, 0));
                    btnBotaoFicheiro.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    btnBotaoFicheiro.setBounds(484, 270, 57, 20);
                    contentPane.add(btnBotaoFicheiro);

                    setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Allow clients (like ComandosAleatorios) to insert commands via the GUI
    public void inserirComandoNoBuffer(Movimento c) {
    	
    	java.util.concurrent.Semaphore mux = bd.getProdutorMux();
        if (mux.tryAcquire()) {
        	try {
        		bufferCircular.inserirElemento(c);
        	}finally {
        		mux.release();
        	}
        }
        else {
			// Could not acquire semaphore, store command as pending
			this.movimentoPendente = c;
        }
    }
    
     public BaseDados getBd() {
         return bd;
     }

     public void setBd(BaseDados bd) {
         this.bd = bd;
     }

     public BufferCircular getBufferCircular() {
         return bufferCircular;
     }

     public void setServidor(Servidor servidor) {
         this.bd.setServidor(servidor);
     }
     public void setTarefaObstaculo(EvitarObstaculo tObstaculo) {
 		this.tObstaculo = tObstaculo;
 		
 	 }
 }