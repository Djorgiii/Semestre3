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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class GUI extends JFrame {

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
    private ComandosAleatorios comandosAleatorios;
    
    public void myPrint(String s) {
		textAreaConsola.append(s + "\n");
	}

    /**
     * Create the frame.
     */
    public GUI() {
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
                    setBounds(100, 100, 619, 446);
                    contentPane = new JPanel();
                    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
                    setContentPane(contentPane);
                    contentPane.setLayout(null);

                    // Botão Fazer Reta
                    btnFrente = new JButton("FRENTE");
                    btnFrente.setForeground(new Color(0, 0, 0));
                    btnFrente.setBackground(new Color(128, 255, 128));
                    btnFrente.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnFrente.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            bd.getRobot().Reta(bd.getDistancia());
                            bd.getRobot().Parar(false);
                            myPrint("Fez uma reta de " + bd.getDistancia() + " cm.");	
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
                    		bd.setDistancia(Integer.parseInt(textFieldDistancia.getText()));
                    		myPrint("A distância foi alterada para " + bd.getDistancia() + " cm.");
                    		
                    	}
                    });
                    textFieldDistancia.setBounds(277, 9, 34, 21);
                    contentPane.add(textFieldDistancia);
                    textFieldDistancia.setColumns(10);
                    
                    JScrollPane scrollPane = new JScrollPane();
                    scrollPane.setBounds(17, 246, 567, 123);
                    contentPane.add(scrollPane);
                    
                    textAreaConsola = new JTextArea();
                    textAreaConsola.setEditable(false);
                    scrollPane.setViewportView(textAreaConsola);
                    
                    lblRaio = new JLabel("Raio");
                    lblRaio.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblRaio.setBounds(10, 10, 45, 18);
                    contentPane.add(lblRaio);
                    
                    textFieldRaio = new JTextField();
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
                    		bd.getRobot().Parar(true);
                    		myPrint("O Robot parou!");
                    	}
                    });
                    btnParar.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnParar.setBounds(242, 119, 105, 37);
                    contentPane.add(btnParar);
                    
                    JButton btnDireita = new JButton("DIREITA");
                    btnDireita.setBackground(new Color(0, 128, 255));
                    btnDireita.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                    		bd.getRobot().CurvarDireita(bd.getRaio(), bd.getAngulo());
                    		bd.getRobot().Parar(false);
                    		myPrint("Fez uma curva à direita com um angulo de " + bd.getAngulo()+ "º e com " +  bd.getRaio() + " de raio.");
                    	}
                    });
                    btnDireita.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnDireita.setBounds(345, 119, 117, 37);
                    contentPane.add(btnDireita);
                    
                    JButton btnEsquerda = new JButton("ESQUERDA");
                    btnEsquerda.setBackground(new Color(255, 128, 255));
                    btnEsquerda.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                    		bd.getRobot().CurvarEsquerda(bd.getRaio(), bd.getAngulo());
                    		bd.getRobot().Parar(false);
                    		myPrint("Fez uma curva à esquerda com um angulo de " + bd.getAngulo()+ "º e com " +  bd.getRaio() + " de raio.");
                    	}
                    });
                    btnEsquerda.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnEsquerda.setBounds(134, 119, 111, 37);
                    contentPane.add(btnEsquerda);
                    
                    JButton btnTras = new JButton("TRÁS");
                    btnTras.setBackground(new Color(255, 128, 128));
                    btnTras.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                    		bd.getRobot().Reta(bd.getDistancia() * -1);
                            bd.getRobot().Parar(false);
                            myPrint("Fez uma reta de " + bd.getDistancia() + " cm.");	
                    	}
                    });
                    btnTras.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    btnTras.setBounds(242, 154, 105, 37);
                    contentPane.add(btnTras);
                    
                    JLabel lblConsola = new JLabel("Consola");
                    lblConsola.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblConsola.setBounds(17, 218, 67, 18);
                    contentPane.add(lblConsola);
                    
                    JLabel lblNmero = new JLabel("Número");
                    lblNmero.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    lblNmero.setBounds(252, 201, 70, 18);
                    contentPane.add(lblNmero);
                    
                    JSpinner spinner = new JSpinner();
                    spinner.setModel(new SpinnerNumberModel(5, 1, 16, 1));
                    spinner.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    spinner.setBounds(327, 199, 45, 21);
                    
                    contentPane.add(spinner);
                    
                    JRadioButton rdbtnMovimentosAleatrios = new JRadioButton("Movimentos Aleatórios");
                    rdbtnMovimentosAleatrios.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (rdbtnMovimentosAleatrios.isSelected()) {
                                if (!bd.isRobotAberto()) {
                                    myPrint("Abra o robot antes de executar movimentos aleatórios.");
                                    return;
                                }
                                if (comandosAleatorios == null) {
                                    // Pass the GUI reference so the task must go through GUI -> BaseDados -> Servidor
                                    comandosAleatorios = new ComandosAleatorios(GUI.this);
                                    comandosAleatorios.start();
                                }
                                comandosAleatorios.desbloquear();
                            }
                        }
                    });
                    rdbtnMovimentosAleatrios.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    rdbtnMovimentosAleatrios.setBounds(378, 200, 221, 21);
                    contentPane.add(rdbtnMovimentosAleatrios);

                    setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Allow clients (like ComandosAleatorios) to insert commands via the GUI
    public void inserirComandoNoBuffer(Comando c) {
        if (bufferCircular != null) {
            bufferCircular.inserirElemento(c);
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
 }