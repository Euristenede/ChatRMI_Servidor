/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmichat_servidor;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import rmichat_comun.ChatException;
import rmichat_comun.UIUtilitarios;
/**
 *
 * @author euris
 */
public class ChatServidor extends JFrame implements ActionListener, WindowListener{
    //Botão para iniciar o servidor
    private JButton btnIniciar;
    //Botão para parar o servidor
    private JButton btnParar;
    //Barra de status
    private JLabel lblStatus;
    //O handler é quem implementa a lógica das operações
    private ServidorHandler handler;
    //Construtor
    public ChatServidor(){
        //Chama o construtor da superclasse passando o título da tela
        super("Servidor de Chat");
        //Define que se a janela for fechada, a aplicação irá terminar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Não permite o redimencionamento da tela
        setResizable(false);
        //Registra um listener para os eventos de janela. Neste caso o interesse maior
        //é saber quando a janela está sendo fechada (método windowClosing(WinsowEvent))
        addWindowListener(this);
        //Define o tamanho da janela em pixel
        setSize(250, 100);
        //Centraliza a janela na tela
        UIUtilitarios.centralizarJanela(this);
        //Define o layout como BorderLayout
        setLayout(new BorderLayout());
        //Cria um painel para os botões, utilizando um FlowLayout onde os componentes ficam centralizado
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        //Cria o botão "Iniciar" e adiciona ao painel
        btnIniciar = new JButton("Iniciar");
        btnIniciar.addActionListener(this);
        buttonPanel.add(btnIniciar);
        //Cria o botão "Parar" e adiciona ao painel
        btnParar = new JButton("Parar");
        btnParar.addActionListener(this);
        btnParar.setEnabled(false);//Inicialmente fica desabilitado
        buttonPanel.add(btnParar);
        //Adiciona o painel de botões no centro da janela
        add(BorderLayout.CENTER, buttonPanel);
        //Cria uma barra de status sem texto e adiciona ao painel
        lblStatus = new JLabel(" ");
        //A utilização de uma borda deste tipo dá um efeito de profundidade ao componente
        lblStatus.setBorder(BorderFactory.createLoweredBevelBorder());
        add(BorderLayout.SOUTH, lblStatus);
        try{
            handler = new ServidorHandler();
        }catch(Exception e){
            //Seocorrer alguma exceção, exibe a mesma em uma caixa de dialogo
            UIUtilitarios.disparaException(this, e);
        }
        //Exibe a janela
        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        try{
            if(event.getSource() == btnIniciar){
                //Houve um clique no botão "Iniciar"
                
                //Desabilita o botão "Iniciar" e habilita o botão "Parar"
                btnIniciar.setEnabled(false);
                btnParar.setEnabled(true);
                //Iniciar o servidor. Este método retorna a porta onde o servidor foi aberto
                int port = handler.iniciarServidor();
                //Escreve a mensagem na barra de status
                lblStatus.setText("Servidor iniciado na porta: "+port);
            }else if(event.getSource() == btnParar){
                //Houve um clique no botão "Parar"
                
                //Habilida o botão "Iniciar" e desabilita o botão "Parar"
                btnIniciar.setEnabled(true);
                btnParar.setEnabled(false);
                //Para o servidor
                handler.pararServidor();
                //Wscreve a mensagem na barra de status
                lblStatus.setText("Servidor Parado");
            }
        }catch(ChatException e){
            UIUtilitarios.disparaException(this, e);
        }
    }

    @Override
    public void windowOpened(WindowEvent we) {
    }

    @Override
    public void windowClosing(WindowEvent we) {  
        try{
            if(handler != null){
                handler.pararServidor();
            }
        }catch(ChatException e){
            e.printStackTrace();
        }
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
    }

    @Override
    public void windowActivated(WindowEvent we) {
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
    }
    
    //Método main
    public static void main(String[] args) {
        //Abra a interface gráfica do cliente
        new ChatServidor();
    }    
}
