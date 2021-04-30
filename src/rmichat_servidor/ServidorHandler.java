/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmichat_servidor;
import com.sun.istack.internal.logging.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.Set;
import rmichat_comun.ChatException;
import rmichat_comun.ClienteCallback;
import rmichat_comun.InformacaoCliente;
import rmichat_comun.OperacoesServidor;
/**
 *
 * @author euris
 */
//Responsável pela lógica de funcionamento do servidor. Implementa operações como de iniciar e parar o servidor
public class ServidorHandler {
    //Objeto Logger
    private static final Logger logger = Logger.getLogger(ServidorHandler.class);
    //Arquivo de configuração onde estão definidas as propriedades do servidor
    private static final String CONFIG_FILE = "configuracao_servidor";
    //Constante que representa o nome da propriedade que armazena o número da porta do servidor
    //utilizada, dentro do arquivo de configuração
    private static final String PROP_PORT = "port";
    //Porta padrão, quando uma não está definida
    private static final int DEFAULT_PORT = 1909;
    //Referência ao {@link ServerOperetionsImpk}, que é utilizado pelos clientes para invocarem a *** no servidor
    private ServidorOperacoesImpl servidorOperacoes;
    //RMI Registry
    private Registry registry;
    //Propriedades lidas do arquivo de configuração
    private Properties props;
    //Controla se o servidor está iniciando ou não
    private boolean iniciando;
    //Construtor

    public ServidorHandler() throws ChatException, IOException{
        //Lê os dados de arquivo de configuração
        props = new Properties();
        File arquivo = new File(CONFIG_FILE);
        
        if(arquivo.exists()){
            FileInputStream fis = null;
            try{
                fis = new FileInputStream(arquivo);
                props.load(fis);
            }finally{
                if(fis != null){
                    fis.close();
                }
            }
        }else{
            //Se o arquivo não existe, define a porta com o valor padrão
            props.setProperty(PROP_PORT, String.valueOf(DEFAULT_PORT));
        }
        try{
            //Inicia o RMI Registry
            registry = LocateRegistry.createRegistry(getPortaServidor());
        }catch(RemoteException e){
            throw  new ChatException("Erro ao criar o Registry. Erro: ", e);
        } 
    }
    //Inicia o servidor
    public int iniciarServidor() throws ChatException{
        try{
            logger.info("O servidor foi iniciado");
            //Cria o objeto remoto
            servidorOperacoes = new ServidorOperacoesImpl();
            //Registra o objeto no RMI Registry
            registry.bind(OperacoesServidor.SERVER_OBJ_NAME, (Remote) servidorOperacoes);
            //Marca o servidor como inicializado
            iniciando = true;
            //Retorna a porta do servidor
            return getPortaServidor();
        }catch(Exception e){
            throw new ChatException("Erro ao iniciar o servidor. Erro: ", e);
        }
    }
    //Parar o servidor
    public void pararServidor() throws ChatException{
        try{
            //Se o servidor não está mais execultando, não faz nada
            if(!iniciando){
                return;
            }
            logger.info("O servidor foi parado");
            //Remove o objeto remoto do RMI Registry
            registry.unbind(OperacoesServidor.SERVER_OBJ_NAME);
            //Obtém o conjunto de clientes conectados ao servidor
            Set<InformacaoCliente> clientes = servidorOperacoes.getClientes();
            //Intera sobre cada cliente
            for(InformacaoCliente clienteInfo : clientes){
                //Obtem o objeto de callback, que permite se comunicar com o cliente
                final ClienteCallback callback = clienteInfo.getCallback();
                //Avisa o cliente utilizando uma nova thread. Isto evita que o servidor tenha que ficar
                //esperando que o cliente processe esta informação
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            //Avisa o cliente que o servidor está prestes a terminar
                            callback.onServidorDesligando();
                        }catch(RemoteException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            //Removo todos os clientes da lista de clientes
            clientes.clear();
            //Marca o servidor como padrão
            iniciando = false;
        }catch(Exception e){
            throw  new ChatException("Erro ao parar o servidor. Erro: ", e);
        }
    }
    //Obtem a porta do servidor
    private int getPortaServidor() throws ChatException{
        //Le a porta a partir do objeto properties
        String portaStr = props.getProperty(PROP_PORT);
        //A porta deve ter sido definida
        if(portaStr == null){
            throw new ChatException("A porta do servidor não foi definida.");
        }
        try{
            //A porta deve poder ser convertida para um número inteiro
            int porta = Integer.parseInt(portaStr);
            //A porta deve estar num intervalo entre 1 e 65635
            if (porta < 1 || porta > 65635){
                throw  new ChatException("A porta não está num intervalo válido.");
            }
            //Retorna a porta lida
            return porta;
        }catch(NumberFormatException e){
            throw  new ChatException("A porta não é um número válido.");
        }
    }
}
