/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmichat_servidor;
import com.sun.istack.internal.logging.Logger;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import rmichat_comun.ClienteCallback;

import rmichat_comun.InformacaoCliente;
import rmichat_comun.NomeDuplicadoException;
import rmichat_comun.OperacoesServidor;
/**
 *
 * @author euris
 */
public class ServidorOperacoesImpl extends UnicastRemoteObject implements OperacoesServidor{
    //Objeto Logger
    private static final Logger logger = Logger.getLogger(ServidorOperacoesImpl.class);
    //Objeto que formata hora
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    //Conjunto de clientes conectados ao servidor
    private Set<InformacaoCliente> clientes = new HashSet<InformacaoCliente>();
    //Construtor

    public ServidorOperacoesImpl() throws RemoteException {
    }   
        
    @Override
    public synchronized void connect(InformacaoCliente clientInfo) throws RemoteException, NomeDuplicadoException {
        //Adciona o cliente no conjunto de clientes
        boolean added = clientes.add(clientInfo);
        //Se ele não pode ser adicionado, é porque já existe outro cliente com o mesmo nome.
        //Neste caso, lança uma NomeDuplicadoException.
        if(!added){
            throw  new NomeDuplicadoException("O nome "+clientInfo.getNome()+" já existe no servidor.");
        }
        //Cria a mensagem a ser enviada para os clientes
        String mensagem = clientInfo.getNome()+" entrou no chat!";
        //Formata a mensagem gerada pelo servidor
        String mensagemFormatada = menseagemFormatadaDoServidorParaCliente(mensagem);
        //Envia a mensagem para todos
        enviarMensagemParaTodos(mensagemFormatada);
    }

    @Override
    public synchronized void disconect(InformacaoCliente clientInfo, String mensagem) throws RemoteException {
        //Remove o cliente do conjunto de clientes
        clientes.remove(clientInfo);
        //Cria a mensagem a ser enviada para os clientes
        String mensage = clientInfo.getNome()+" saiu do chat!";
        //Formata a mensagem gerada pelo servidor
        String mensagemFormatada = menseagemFormatadaDoServidorParaCliente(mensage);
        //Envia a mensagem para todos
        enviarMensagemParaTodos(mensagemFormatada);
    }

    @Override
    public synchronized void enviarMensagem(InformacaoCliente clientInfo, String mensagem) throws RemoteException {
        //Formada a mensagem vinda do cliente
        String mensagemFormatada = mensagemFormatadaDoClienteParaCliente(mensagem, clientInfo.getNome());
        //Envia a mensagem para todos
        enviarMensagemParaTodos(mensagemFormatada);
    }
    //Formata uma mensagem criada pelo servidor e que deve ser enviada aos clientes
    private String menseagemFormatadaDoServidorParaCliente(String mensagem){
        String mensagemFormatada = String.format("(%s) %s", sdf.format(new Date()), mensagem);
        return mensagemFormatada;
    }
    //Formata uma mensagem recebida por um cliente e que deve ser enviada aos clientes
    private String mensagemFormatadaDoClienteParaCliente(String mensagem, String nomeCliente){
        String mensagemFormatada = String.format("(%s) [%s] %s", sdf.format(new Date()), nomeCliente, mensagem);
        return mensagemFormatada;
    }
    //Emvia mensagem para todos os clientes
    private void enviarMensagemParaTodos(final String mensagem){
        //Loga a mensagem que será enviada
        logger.info(mensagem);
        for(InformacaoCliente cliente : clientes){
            //Obtem o objeto de callback do cliente
            final ClienteCallback callback = cliente.getCallback();
            //Cria uma nova thread para disparar a mensagem para o cliente. Isto evita o servidor ter 
            //que ficar esperando a entrega da mensagem para cada cliente para continuar enviando para o
            //proximo
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        //Envia a mensagem para o cliente
                        callback.onEnviandoMensagem(mensagem);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    //Obtem o conjunto de cliente conectados ao servidor
    public synchronized  Set<InformacaoCliente> getClientes(){
        return clientes;
    }

}
