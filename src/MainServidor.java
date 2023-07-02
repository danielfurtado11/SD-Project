import java.net.ServerSocket;
import java.net.Socket;

public class MainServidor {
    public static void main(String [] args){
        try{
            Thread t;
            ServerSocket ss = new ServerSocket(12345);
            Servidor servidor = new Servidor();
            GereNotificacoes gn = servidor.getGereNotificacoes();

            while(true){
                // Novo cliente
                Socket s = ss.accept();
                TaggedConnection tc = new TaggedConnection(s);
                // Executamos uma thread com uma nova Sessão (novo cliente) que é responsável por distribuir os diferentes pedidos por outras thread segundo o tag
                t = new Thread(new Sessao(servidor, tc, gn));
                t.start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
