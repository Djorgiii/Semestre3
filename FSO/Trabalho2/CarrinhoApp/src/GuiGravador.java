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

public class GuiGravador extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private BaseDados bd;
    private JButton btnFrente, btnTras, btnDireita, btnEsquerda, btnParar;
    private JRadioButton rdbtnOnOff;
    private JTextField textFieldDistancia;
    private JTextArea textAreaConsola;
    private JLabel lblRaio;
    private JTextField textFieldRaio;
    private JTextField textFieldAngulo;
    private JTextField textFieldRobot;
    private Gravador gravador;
    private JTextField textField;
    private boolean mesmoRobot = false;
    private BufferCircular bufferCentral;
    private JRadioButton rdbtnMesmoRobot;

    

    private RobotLegoEV3 robotNovo = new RobotLegoEV3();
    private boolean robotNovoAberto = false;
    private String nomeRobot = "EV3";

    public void myPrint(String s) {
        textAreaConsola.append(s + "\n");
    }
    
    public void setBufferCentral(BufferCircular buffer) {
        this.bufferCentral = buffer;
    }

    
    public Gravador getGravador() {
        return gravador;
    }

    public void registarExterno(Movimento m) {
        if (gravador.isAGravar() && m != null) {
            gravador.registar(m);
        }
    }

    private boolean podeGravar() {
        if (gravador.isEmReproducao()) {
            myPrint("[AVISO] Não pode gravar enquanto está a reproduzir.");
            return false;
        }
        return true;
    }

    private boolean podeReproduzir() {
        if (gravador.isAGravar()) {
            myPrint("[AVISO] Não pode reproduzir enquanto está a gravar.");
            return false;
        }
        return true;
    }

    
    public boolean isMesmoRobot() {
        return mesmoRobot;
    }
    
    private void enviarMovimento(Movimento m) {
        if (m == null) return;

        if (mesmoRobot) {
            bufferCentral.inserirElemento(m);
            return;
        }

        executarDireto(m);
    }

    private void executarDireto(Movimento m) {

        switch (m.getTipo()) {

            case "RETA":
                robotNovo.Reta(m.getArg1());
                break;

            case "CURVARDIREITA":
                robotNovo.CurvarDireita(m.getArg1(), m.getArg2());
                break;

            case "CURVARESQUERDA":
                robotNovo.CurvarEsquerda(m.getArg1(), m.getArg2());
                break;

            case "PARAR":
                robotNovo.Parar(false);
                return;
        }

        robotNovo.Parar(false);
    }

    private boolean podeExecutar() {

        if (mesmoRobot) {
            // robot é o da GUI principal
            return bd.isRobotAberto();
        }

        // modo isolado
        return robotNovoAberto;
    }

 
    public GuiGravador(BaseDados bd) {
        this.bd = bd;
        gravador = new Gravador();

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent arg0) {
                        	if (robotNovoAberto && robotNovo != null) {

                        	    try {
                        	        robotNovo.Parar(true);  // Para imediatamente
                        	        Thread.sleep(150);      // Garante que o comando chega ao EV3
                        	    } catch (Exception e1) {}

                        	    robotNovo.CloseEV3();       // Agora fecha em segurança
                        	    robotNovoAberto = false;
                        	}

                        	bd.setTerminar(true);  
                        	dispose();
                        }
                    });

                    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    setBounds(100, 100, 619, 569);
                    contentPane = new JPanel();
                    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
                    setContentPane(contentPane);
                    contentPane.setLayout(null);

                    btnFrente = new JButton("FRENTE");
                    btnFrente.setForeground(new Color(0, 0, 0));
                    btnFrente.setBackground(new Color(128, 255, 128));
                    btnFrente.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnFrente.setEnabled(false);
                    btnFrente.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                        	
                            if (!podeExecutar()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                            
                            
                            Movimento m = new Movimento("RETA", bd.getDistancia(), 0);
                            Movimento m2 = new Movimento("PARAR", false);
                            
                            if (gravador.isAGravar()) {
                            	gravador.registar(m);
                            	myPrint("[Gravador] Gravado: " + m.getTipo());
                            	gravador.registar(m2);
                            	myPrint("[Gravador] Gravado: " + m2.getTipo());
                            	return;
                            																	
                            }
                            
                            if(gravador.isEmReproducao()) {
								myPrint("[GUI] O robot está a reproduzir, aguarde...");
								return;
							}
                            
                            enviarMovimento(m);
                            enviarMovimento(m2);
                        }
                    });
                    btnFrente.setBounds(242, 84, 105, 37);
                    contentPane.add(btnFrente);

                    JLabel lblDistancia = new JLabel("Distância");
                    lblDistancia.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblDistancia.setBounds(203, 9, 81, 21);
                    contentPane.add(lblDistancia);

                    textFieldDistancia = new JTextField();
                    textFieldDistancia.setEnabled(false);
                    textFieldDistancia.setText("33");
                    textFieldDistancia.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    textFieldDistancia.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            bd.setDistancia(Integer.parseInt(textFieldDistancia.getText()));
                            myPrint("A distância foi alterada para " + bd.getDistancia() + " cm.");
                        }
                    });
                    textFieldDistancia.setBounds(277, 9, 34, 21);
                    contentPane.add(textFieldDistancia);
                    textFieldDistancia.setColumns(10);

                    JScrollPane scrollPane = new JScrollPane();
                    scrollPane.setBounds(28, 371, 537, 123);
                    contentPane.add(scrollPane);

                    textAreaConsola = new JTextArea();
                    scrollPane.setViewportView(textAreaConsola);

                    lblRaio = new JLabel("Raio");
                    lblRaio.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblRaio.setBounds(10, 10, 45, 18);
                    contentPane.add(lblRaio);

                    textFieldRaio = new JTextField();
                    textFieldRaio.setEnabled(false);
                    textFieldRaio.setText("20");
                    textFieldRaio.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
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
                    textFieldAngulo.setEnabled(false);
                    textFieldAngulo.setText("90");
                    textFieldAngulo.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
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
                    textFieldRobot.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                    		nomeRobot = textFieldRobot.getText().trim();
							myPrint("O nome do robot foi alterado para \"" + nomeRobot + "\".");
                    	}
                    });
                    textFieldRobot.setEditable(true);
                    textFieldRobot.setText("EV3"); // nome padrão do Bluetooth
                    textFieldRobot.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    textFieldRobot.setColumns(10);
                    textFieldRobot.setBounds(527, 9, 60, 19);
                    contentPane.add(textFieldRobot);

                    btnParar = new JButton("PARAR");
                    btnParar.setEnabled(false);
                    btnParar.setBackground(new Color(255, 0, 0));
                    btnParar.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            if (!podeExecutar()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                            
                            if (gravador.isEmReproducao()) {
                                myPrint("[GUI] A reproduzir — comandos ignorados.");
                                return;
                            }
                            
                            Movimento m = new Movimento("PARAR", false);
                            

                            if (gravador.isAGravar()) {
                                gravador.registar(m);
                                myPrint("[Gravador] Gravado: PARAR");
                                return;
                            }
                            
                            enviarMovimento(new Movimento("PARAR", false));
                        }
                    });
                    btnParar.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnParar.setBounds(242, 119, 105, 37);
                    contentPane.add(btnParar);

                    btnDireita = new JButton("DIREITA");
                    btnDireita.setEnabled(false);
                    btnDireita.setBackground(new Color(0, 128, 255));
                    btnDireita.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (!podeExecutar()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                            
                            if (gravador.isEmReproducao()) {
								myPrint("[GUI] A reproduzir — comandos ignorados.");
								return;
							}
                            
                            Movimento m = new Movimento("CURVARDIREITA", bd.getRaio(), bd.getAngulo());
                            Movimento m2 = new Movimento("PARAR", false);
                            
                            if (gravador.isAGravar()) {
								gravador.registar(m);
								myPrint("[Gravador] Gravado: " + m.getTipo());
								gravador.registar(m2);
								myPrint("[Gravador] Gravado: " + m2.getTipo());
								return;
                            }
                            enviarMovimento(m);
                            enviarMovimento(m2);
                        }
                    });
                    btnDireita.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnDireita.setBounds(345, 119, 117, 37);
                    contentPane.add(btnDireita);

                    btnEsquerda = new JButton("ESQUERDA");
                    btnEsquerda.setEnabled(false);
                    btnEsquerda.setBackground(new Color(255, 128, 255));
                    btnEsquerda.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (!podeExecutar()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                            
                            if (gravador.isEmReproducao()) {
                                myPrint("[GUI] A reproduzir — comandos ignorados.");
                                return;
                            }
                            
                            Movimento m = new Movimento("CURVARESQUERDA", bd.getRaio(), bd.getAngulo());
                            Movimento m2 = new Movimento("PARAR", false);
                            
                            if (gravador.isAGravar()) {
								gravador.registar(m);
								myPrint("[Gravador] Gravado: " + m.getTipo());
								gravador.registar(m2);
								myPrint("[Gravador] Gravado: " + m2.getTipo());
								return;
								}
							
                            enviarMovimento(m);
                            enviarMovimento(m2);
                        }
                    });
                    btnEsquerda.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnEsquerda.setBounds(134, 119, 111, 37);
                    contentPane.add(btnEsquerda);

                    btnTras = new JButton("TRÁS");
                    btnTras.setEnabled(false);
                    btnTras.setBackground(new Color(255, 128, 128));
                    btnTras.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (!podeExecutar()) {
                                myPrint("Abra o robot antes de executar movimentos.");
                                return;
                            }
                            
                            if (gravador.isEmReproducao()) {
                                myPrint("[GUI] A reproduzir — comandos ignorados.");
                                return;
                            }
                            
                            Movimento m = new Movimento("RETA", -bd.getDistancia(), 0);
                            Movimento m2 = new Movimento("PARAR", false);
                            
                            if (gravador.isAGravar()) {
                            	gravador.registar(m);
                            	myPrint("[Gravador] Gravado: " + m.getTipo());
								gravador.registar(m2);
								myPrint("[Gravador] Gravado: " + m2.getTipo());
								return;
							}
							
                            enviarMovimento(m);
                            enviarMovimento(m2);

                        }
                    });
                    btnTras.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnTras.setBounds(242, 154, 105, 37);
                    contentPane.add(btnTras);

                    JLabel lblConsola = new JLabel("Consola");
                    lblConsola.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblConsola.setBounds(28, 343, 67, 18);
                    contentPane.add(lblConsola);

                    JLabel lblGravador = new JLabel("Gravador");
                    lblGravador.setBackground(new Color(0, 128, 255));
                    lblGravador.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblGravador.setBounds(28, 244, 81, 18);
                    contentPane.add(lblGravador);

                    JLabel lblFicheiro_1 = new JLabel("Ficheiro");
                    lblFicheiro_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
                    lblFicheiro_1.setBackground(new Color(0, 128, 255));
                    lblFicheiro_1.setBounds(38, 272, 57, 19);
                    contentPane.add(lblFicheiro_1);

                    textField = new JTextField();
                    textField.setEditable(false);
                    textField.setEnabled(false);
                    textField.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    textField.setColumns(10);
                    textField.setBounds(112, 271, 386, 19);
                    contentPane.add(textField);

                    JButton btnGravar = new JButton("Gravar");
                    btnGravar.setEnabled(false);
                    btnGravar.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    btnGravar.setForeground(new Color(0, 0, 0));
                    btnGravar.setBounds(203, 300, 84, 20);
                    contentPane.add(btnGravar);

                    btnGravar.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                        	
                            if (!podeGravar()) {
								myPrint("[GUI] Não pode gravar agora.");
								return;
								}

                        	
                        	if (!gravador.isAGravar()) {
                        	    gravador.iniciarGravacao();
                        	    btnGravar.setText("Parar");
                        	    myPrint("[Gravador] A gravar...");
                        	    return;
                        	}

                        	gravador.pararGravacao();
                        	btnGravar.setText("Gravar");

                        	String nome = textField.getText().trim();
                        	if (nome.isEmpty()) {
                        	    myPrint("[Gravador] Indique um nome de ficheiro.");
                        	    return;
                        	}

                        	gravador.guardarEmFicheiro(nome);

                            myPrint("[Gravador] Sequência gravada em: " + nome);
                        }
                    });

                    JButton btnReproduzir = new JButton("Reproduzir");
                    btnReproduzir.setEnabled(false);
                    btnReproduzir.setForeground(new Color(0, 0, 0));
                    btnReproduzir.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    btnReproduzir.setBounds(338, 300, 94, 20);
                    contentPane.add(btnReproduzir);

                    btnReproduzir.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                        	
                        	if (!podeReproduzir()) {
                        		myPrint("[GUI] Não pode reproduzir agora.");
                        		return;
                        	}
                        	
                            if (gravador.isEmReproducao()) {
                                myPrint("[GUI] Reprodução em curso.");
                                return;
                            }

                            String nome = textField.getText().trim();
                            if (nome.isEmpty()) {
                                myPrint("[Gravador] Indique o ficheiro.");
                                return;
                            }

                            gravador.lerFicheiro(nome);
                            

                            if (mesmoRobot) {
                                gravador.reproduzirParaBuffer(bufferCentral);
                                myPrint("[Gravador] Reprodução via Servidor.");
                            } else {
                                gravador.iniciarReproducao(robotNovo);
                                myPrint("[Gravador] Reprodução direta.");
                            }
                        }
                    });

                    JButton btnBotaoFicheiro = new JButton("...");
                    btnBotaoFicheiro.setEnabled(false);
                    btnBotaoFicheiro.setForeground(new Color(0, 0, 0));
                    btnBotaoFicheiro.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    btnBotaoFicheiro.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            javax.swing.JFileChooser jfc = new javax.swing.JFileChooser();
                            int returnValue = jfc.showOpenDialog(null);
                            if (returnValue == javax.swing.JFileChooser.APPROVE_OPTION) {
                                java.io.File selectedFile = jfc.getSelectedFile();
                                textField.setText(selectedFile.getAbsolutePath());
                            }
                        }
                    });
                    btnBotaoFicheiro.setBounds(508, 272, 57, 20);
                    contentPane.add(btnBotaoFicheiro);
                    
                    rdbtnMesmoRobot = new JRadioButton("Mesmo Robot");
                    rdbtnMesmoRobot.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
							mesmoRobot = rdbtnMesmoRobot.isSelected();
							
							if(mesmoRobot) {
					            nomeRobot = bd.getNomeRobotPrincipal();
					            textFieldRobot.setText(nomeRobot);                            
					            myPrint("[GUI] Modo PARTILHADO: comandos via Servidor.");
					            
							}
							else {
					            nomeRobot = textFieldRobot.getText().trim();
					            myPrint("[GUI] Modo ISOLADO: execução direta.");				            
							}
                    	}
                    });
                    rdbtnMesmoRobot.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    rdbtnMesmoRobot.setBounds(471, 56, 105, 21);
                    contentPane.add(rdbtnMesmoRobot);
                    

                    // Botão On/Off - cria/abre/fecha o robotNovo
                    rdbtnOnOff = new JRadioButton("Abrir/Fechar");
                    rdbtnOnOff.setFont(new Font("Tahoma", Font.PLAIN, 12));
                    rdbtnOnOff.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (robotNovoAberto) {
                                // FECHAR
                                    try {
                                    	robotNovo.Parar(true);  // Para imediatamente
										Thread.sleep(150); // Garante que o comando chega ao EV3
									} catch (Exception ex) {}
                                    
                                    robotNovo.CloseEV3();
                                    robotNovoAberto = false;
                               }
                            else {
                                // ABRIR
                            	String nomePrincipal = bd.getNomeRobotPrincipal();
                            	boolean principalAberto = bd.isRobotAberto();
                            	
                                if (principalAberto &&
                                        nomeRobot.equalsIgnoreCase(nomePrincipal)) {

                                        myPrint("O Robot '" + nomeRobot + "' já está aberto na GUI principal.");
                                        return;
                                    }
                                
                                if(mesmoRobot) {
                                    robotNovoAberto = true; // lógico apenas
                                    myPrint("[GUI] A usar robot principal (via Servidor).");
                                    return;
								}
                            	
                            	boolean abriu = robotNovo.OpenEV3(nomeRobot);
                            	
                            	if(!abriu) {
									myPrint("Não foi possível abrir o robot \"" + nomeRobot + "\".");
									return;
								}
                            	robotNovoAberto = true;
                            }

                            rdbtnOnOff.setSelected(robotNovoAberto);
                            myPrint("Robot " + nomeRobot  + " foi " + (robotNovoAberto ? "ABERTO" : "FECHADO") + ".");

                            btnFrente.setEnabled(robotNovoAberto);
                            btnTras.setEnabled(robotNovoAberto);
                            btnDireita.setEnabled(robotNovoAberto);
                            btnEsquerda.setEnabled(robotNovoAberto);
                            btnParar.setEnabled(robotNovoAberto);
                            btnReproduzir.setEnabled(robotNovoAberto);
                            btnGravar.setEnabled(robotNovoAberto);
                            btnBotaoFicheiro.setEnabled(robotNovoAberto);
                            textField.setEnabled(robotNovoAberto);
                            textFieldRaio.setEditable(robotNovoAberto);
                            textFieldRaio.setEnabled(robotNovoAberto);
                            textFieldAngulo.setEditable(robotNovoAberto);
                            textFieldAngulo.setEnabled(robotNovoAberto);
                            textFieldDistancia.setEditable(robotNovoAberto);
                            textFieldDistancia.setEnabled(robotNovoAberto);
                            
                        }
                    });
                    rdbtnOnOff.setBounds(471, 35, 94, 21);
                    contentPane.add(rdbtnOnOff);

                    setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public BaseDados getBd() {
        return bd;
    }

    public void setBd(BaseDados bd) {
        this.bd = bd;
    }

    public void setServidor(Servidor servidor) {
        this.bd.setServidor(servidor);
    }
}
