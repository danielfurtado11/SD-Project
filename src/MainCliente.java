import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/*
* A main do Cliente, que é multi threaded, basicamente ficar é um while loop infinito que a  cada iteração printa
* um menu principal, depois dependendo do que o cliente quiser fazer é printado um dos 6 sub-menus que, por fim, delegam
* o trabalho para uma thread que trata de realizar o envio do pedido para o servidor e a recessão da resposta do servidor
*/

public class MainCliente {
    public static void main(String [] args) throws IOException {
        Thread t;
        Socket socket = new Socket("localhost", 12345);
        Demultiplexer dm = new Demultiplexer(new TaggedConnection(socket));
        ReentrantLock l = new ReentrantLock();
        MenuCliente menu = new MenuCliente();
        dm.start(); // Tenho uma thread independente a receber novas respostas vindas do servidor
        boolean continua = true;

        try {
            // Fase do resgistro/autenticação no servidor
            // Como ainda não estamos dentro da plataforma não vale apena delegar o trabalho de fazer a autenticação/registro para outra thread
            boolean autenticado = false;
            while(!autenticado){
                int opcao = menu.menuAutenticarRegistar();
                switch (opcao){
                    case 1:
                        Cliente cl = menu.menu1();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(baos);
                        cl.serialize(out);
                        out.flush();
                        byte [] data = baos.toByteArray();
                        dm.send(1, data); // Envia o pedido ao servidor

                        byte [] resp = dm.receive(opcao); // Recebe a resposta do servidor
                        ByteArrayInputStream bais = new ByteArrayInputStream(resp);
                        DataInputStream in = new DataInputStream(bais);
                        cl = Cliente.deserialize(in);
                        autenticado = cl.getAutenticado();
                        if(!autenticado)
                            System.out.println("Falha ao registar na plataforma.");
                        else
                            System.out.println("Registo na plataforma bem sucedido!");
                        break;
                    case 2:
                        cl = menu.menu1();
                        baos = new ByteArrayOutputStream();
                        out = new DataOutputStream(baos);
                        cl.serialize(out);
                        out.flush();
                        data = baos.toByteArray();
                        dm.send(2, data); // Envia o pedido ao servidor

                        resp = dm.receive(opcao); // Recebe a resposta do servidor
                        bais = new ByteArrayInputStream(resp);
                        in = new DataInputStream(bais);
                        cl = Cliente.deserialize(in);
                        autenticado = cl.getAutenticado();
                        if(!autenticado)
                            System.out.println("Falha na autenticação.");
                        else
                            System.out.println("Autenticação bem sucedida!");
                        break;
                }
            }
            Posicao pos = null;
            // Fase dos pedidos
            while(continua){
                int op = menu.menuPrincipal();
                switch (op){
                    case 1:
                        pos = menu.menu2_3_4();
                        t = new Thread(new ClientWorker1(pos, l, dm));
                        t.start();
                        break;
                    case 2:
                        pos = menu.menu2_3_4();
                        t = new Thread(new ClientWorker2(pos, l, dm));
                        t.start();
                        break;
                    case 3:
                        pos = menu.menu2_3_4();
                        t = new Thread(new ClientWorker3(pos, l, dm));
                        t.start();
                        break;
                    case 4:
                        Estacionamento est = menu.menu5();
                        t = new Thread(new ClientWorker4(est, l, dm));
                        t.start();
                        break;
                    case 5: // Notificações
                        int opcao = menu.menu6();
                        if(opcao == 1){
                            Posicao posNoti = menu.notificacoes();
                            t = new Thread(new ClientWorker5(posNoti, l, dm));
                            t.start();
                        }else if(opcao == 2){
                            String p = "Para notificações";
                            dm.send(8, p.getBytes()); // Avisa servidor o cliente não quer mais notificações
                        }
                        break;
                    case 6:
                        String p = "para";
                        try{
                            l.lock();
                            continua = false;
                            dm.send(8, p.getBytes()); // Primeiro avisa que quer parar as notificações
                            dm.send(9, p.getBytes()); // Avisa servidor que ligação vai ser terminada
                            dm.receive(9);
                            dm.close();
                            System.out.println("Ligação com o servidor terminada!");
                        }finally {
                            l.unlock();
                        }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Obrigado por usar a nossa aplicação de gestão de frota de trotinetes!");
    }
}

class ClientWorker1 implements Runnable{
    private Posicao posicao;
    private ReentrantLock l;
    private Demultiplexer dm;
    public ClientWorker1(Posicao pos, ReentrantLock l, Demultiplexer dm){
        this.posicao = pos;
        this.l = l;
        this.dm = dm;
    }

    public void run(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try{
            this.l.lock();
            this.posicao.serialize(out);
            out.flush();
            byte [] data = baos.toByteArray();
            this.dm.send(3, data); // Envia a posição para o servidor
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.l.unlock();
        }

        byte [] resp = this.dm.receive(3);
        ByteArrayInputStream bais = new ByteArrayInputStream(resp);
        DataInputStream in = new DataInputStream(bais);
        try {
            String str = "";
            int nrPos = in.readInt();

            if(nrPos == 0){
                System.out.println("\nNão existem trotinetes livres perto da posição fornecida!");
            }else{
                for(int i = 0; i < nrPos; i++){
                    Posicao posicao = Posicao.deserialize(in);
                    if(i == 0)
                        str += "\n";
                    str += "Posição com trotinetes livres: " + posicao + "\n";
                }
            }
            System.out.println(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class ClientWorker2 implements Runnable{
    private Posicao posicao;
    private ReentrantLock l;
    private Demultiplexer dm;
    public ClientWorker2(Posicao pos, ReentrantLock l, Demultiplexer dm){
        this.posicao = pos;
        this.l = l;
        this.dm = dm;
    }

    public void run(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try{
            this.l.lock();
            this.posicao.serialize(out);
            out.flush();
            byte [] data = baos.toByteArray();
            this.dm.send(4, data); // Envia o pedido ao servidor
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.l.unlock();
        }

        String str = "";
        byte [] resp = this.dm.receive(4); // Recebe a resposta do servidor
        ByteArrayInputStream bais = new ByteArrayInputStream(resp);
        DataInputStream in = new DataInputStream(bais);
        try {
            int nrRecompensas = in.readInt();

            if(nrRecompensas == 0){
                System.out.println("\nNão existem recompensas com origem perto da posição fornecida!");
            }else{
                for(int i = 0; i < nrRecompensas; i++){
                    Recompensa r = Recompensa.deserialize(in);
                    if(i == 0)
                        str += "\n";
                    str += r;
                }
            }
            System.out.println(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class ClientWorker3 implements Runnable{
    private Posicao posicao;
    private ReentrantLock l;
    private Demultiplexer dm;
    public ClientWorker3(Posicao pos, ReentrantLock l, Demultiplexer dm){
        this.posicao = pos;
        this.l = l;
        this.dm = dm;
    }

    public void run(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try{
            this.l.lock();
            this.posicao.serialize(out);
            out.flush();
            byte [] data = baos.toByteArray();
            this.dm.send(5, data); // Envia o pedido ao servidor
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.l.unlock();
        }

        byte [] resp = this.dm.receive(5); // Recebe a resposta do servidor
        ByteArrayInputStream bais = new ByteArrayInputStream(resp);
        DataInputStream in = new DataInputStream(bais);
        Reserva reserva = Reserva.deserialize(in);
        System.out.println("\n" + reserva);
    }
}

class ClientWorker4 implements Runnable{
    private Estacionamento est;
    private ReentrantLock l;
    private Demultiplexer dm;
    public ClientWorker4(Estacionamento est, ReentrantLock l, Demultiplexer dm){
        this.est = est;
        this.l = l;
        this.dm = dm;
    }

    public void run(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try {
            l.lock();
            est.serialize(out);
            out.flush();
            dm.send(6, baos.toByteArray()); // Envia o pedido ao servidor
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            l.unlock();
        }

        byte [] resp = dm.receive(6); // Recebe a resposta do servidor
        ByteArrayInputStream bais = new ByteArrayInputStream(resp);
        DataInputStream in = new DataInputStream(bais);
        Reserva reserva = Reserva.deserialize(in);
        System.out.println("\n" + reserva);
    }
}

class ClientWorker5 implements Runnable{
    private Posicao posicao;
    private ReentrantLock l;
    private Demultiplexer dm;
    public ClientWorker5(Posicao posicao, ReentrantLock l, Demultiplexer dm){
        this.posicao = posicao;
        this.l = l;
        this.dm = dm;
    }

    public void run(){
        boolean continua = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try{
            this.l.lock();
            this.posicao.serialize(out);
            out.flush();
            this.dm.send(7, baos.toByteArray()); // Envia a posição para o servidor
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.l.unlock();
        }

        byte [] resp;
        ByteArrayInputStream bais;
        DataInputStream in;
        while(continua){
            String str = "";
            resp = this.dm.receive(7); // Recebe a resposta do servidor
            bais = new ByteArrayInputStream(resp);
            in = new DataInputStream(bais);

            try {
                continua = in.readBoolean();
                if(continua){
                    int nrRecompensas = in.readInt();

                    for(int i = 0; i < nrRecompensas; i++){
                        Recompensa recomp = Recompensa.deserialize(in);
                        if(i == 0)
                            str = "\n";
                        str += recomp + "\n";
                    }
                    System.out.println(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}