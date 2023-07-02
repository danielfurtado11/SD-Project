import java.io.*;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Servidor {
    private int d; // Distância fixa que vamos usar para vários calculos (Falta calular isto!!!)
    private Mapa mapa;
    private GereReserva gr;
    private GereRecompensa grecom;
    private GereClientes gc;
    private GereNotificacoes gn;

    public Servidor(){
        this.gc = new GereClientes();

        int dim = 20;
        this.mapa = new Mapa(dim);
        int nrTrotis = this.mapa.getNrTrotis();
        float prop = (float) dim/nrTrotis;
        this.d = (int) (3 * prop); // Calcular o valor D fixo aqui que é usado ao longo do trabalho em várias situações

        this.grecom = new GereRecompensa(this.mapa, this.d);
        new Thread(()->{grecom.run();}).start(); // Thread que calcula as recompensas

        this.gn = new GereNotificacoes(this.grecom);

        this.gr = new GereReserva(this.mapa, this.grecom);

        this.mapa.printMap();
    }

    public boolean novoCliente(Cliente c){ // Este lock aqui é necessário
        return this.gc.adicionaNovoCliente(c);
    }

    public boolean autenticaCliente(Cliente c){
        return this.gc.autenticaCliente(c);
    }

    public List<Posicao> calculaLocaisComTrotisLivres(Posicao posCl){
        return this.mapa.locaisComTrotisLivres(posCl, this.d);
    }

    public Reserva reservaTrotinete(Posicao posCl){
        Reserva r = this.gr.reservaTrotinete(posCl, this.d);
        this.grecom.recalculaRecompensas();
        this.gn.calculaNotificacoes();
        return r;
    }

    public Reserva estacionarTroti(Estacionamento est){
        Reserva reserva = this.gr.estacionar(est);
        this.grecom.recalculaRecompensas();
        this.gn.calculaNotificacoes();
        return reserva;
    }

    public List<Recompensa> listaRecompensas(Posicao posCl){
        return this.grecom.listaRecompensas(posCl);
    }

    public GereNotificacoes getGereNotificacoes(){
        return this.gn;
    }
}

class Sessao implements Runnable {
    private boolean notificacoes;
    private GereNotificacoes gn;
    private Condition cNotificacoes;
    private ReentrantLock lockNotificacoes;
    private ReentrantLock l; // Reentrant lock da sessão (da ligação com o cliente)
    private Servidor servidor;
    private TaggedConnection tc;

    public Sessao(Servidor servidor, TaggedConnection tc, GereNotificacoes gn) {
        this.notificacoes = false;
        this.l = new ReentrantLock();
        this.servidor = servidor;
        this.tc = tc;
        this.cNotificacoes = gn.getConditionNotificacoes();
        this.lockNotificacoes = gn.getLockNotificacoes();
        this.gn = gn;
    }

    // Esta thread é a thread que recebe novos pedidos (lê a tag) e atribui o trabalho a novas threads dependendo da tag
    public void run() {
        boolean continua = true;

        while (continua) {
            TaggedConnection.Frame frame = tc.receive();
            int tag = frame.tag;

            switch (tag) {
                case 1:
                    Thread t1 = new Thread(new ServerWorker1(servidor, tc, frame, this.l));
                    t1.start();
                    break;
                case 2:
                    Thread t2 = new Thread(new ServerWorker2(servidor, tc, frame, this.l));
                    t2.start();
                    break;
                case 3:
                    Thread t3 = new Thread(new ServerWorker3(servidor, tc, frame, this.l));
                    t3.start();
                    break;
                case 4:
                    Thread t4 = new Thread(new ServerWorker4(servidor, tc, frame, this.l));
                    t4.start();
                    break;
                case 5:
                    Thread t5 = new Thread(new ServerWorker5(servidor, tc, frame, this.l));
                    t5.start();
                    break;
                case 6:
                    Thread t6 = new Thread(new ServerWorker6(servidor, tc, frame, this.l));
                    t6.start();
                    break;
                case 7: // Implementa as notificações
                    new Thread(()->{
                        this.notificacoes = true;
                        int index = this.gn.adicionaNotificacao(); // Adiciona um boolean ao array de booleans do GereNotificações
                        byte [] data = frame.data;
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        DataInputStream in = new DataInputStream(bais);
                        Posicao posNoti = Posicao.deserialize(in); // Posição das notificações
                        System.out.println("Posição das notificações: " + posNoti.toString());

                        while(this.notificacoes){
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            DataOutputStream out = new DataOutputStream(baos);
                            try{
                                this.lockNotificacoes.lock();
                                while(!this.gn.isNotificao(index)){
                                    this.cNotificacoes.await(); // Espera que o servidor acorde esta thread para calcular as notificações
                                    System.out.println("Acordei notificações!");
                                }

                                if(this.notificacoes == false){
                                    break;
                                }

                                System.out.println("Vou calcular notificações");
                                List<Recompensa> recompensas = this.gn.calculcaNotificacoes(posNoti);
                                System.out.println("Notificações: " + recompensas);
                                out.writeBoolean(true); // Representa que não é para terminar a receção de notificações
                                int nrRecompensas = recompensas.size();
                                out.writeInt(nrRecompensas);
                                for(Recompensa r: recompensas){
                                    r.serialize(out);
                                }
                                out.flush();
                                tc.send(7, baos.toByteArray());
                                this.gn.setNotificacao(index, false);
                            } catch (IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            } finally {
                                this.lockNotificacoes.unlock();
                            }
                        }
                    }).start();
                    break;
                case 8:
                    this.notificacoes = false;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(baos);
                    try {
                        out.writeBoolean(false); // Avisa o cliente que é para parar de receber notificações
                        out.writeUTF("para");
                        this.tc.send(7,baos.toByteArray());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case 9:
                    try {
                        this.l.lock();
                        String str = "Ok!";
                        this.tc.send(9, str.getBytes());
                        this.tc.close();
                        continua = false;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        this.l.unlock();
                    }
                    break;
            }
        }
    }
}

// A classe ServerWorker1 serve para responder a pedido com a tag 1 (registar na plataforma)
class ServerWorker1 implements Runnable{
    private ReentrantLock l; // Reentrant lock da sessão (da ligação com o cliente) é partilhado por todos os Server Workers
    private Servidor servidor;
    private TaggedConnection tc;
    private TaggedConnection.Frame f;
    public ServerWorker1(Servidor servidor, TaggedConnection tc, TaggedConnection.Frame f, ReentrantLock l){
        try {
            this.l = l;
            this.servidor = servidor;
            this.tc = tc;
            this.f = f;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        System.out.println("Método run do worker 1!");
        byte [] data = this.f.data;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        Cliente cl = Cliente.deserialize(in);
        boolean sucesso = this.servidor.novoCliente(cl);
        System.out.println(cl);
        if(sucesso)
            cl.setAutenticado(true);
        else
            cl.setAutenticado(false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try{ // Evita que respostas diferentes se misturem no socket
            this.l.lock();
            cl.serialize(out);
            out.flush();
            this.tc.send(1, baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally{
            this.l.unlock();
        }
    }
}

// A classe ServerWorker2 serve para responder a pedido com a tag 2 (autenticar na plataforma)
class ServerWorker2 implements Runnable{
    private ReentrantLock l; // Reentrant lock da sessão (da ligação com o cliente) é partilhado por todos os Server Workers
    private Servidor servidor;
    private TaggedConnection tc;
    private TaggedConnection.Frame f;
    public ServerWorker2(Servidor servidor, TaggedConnection tc, TaggedConnection.Frame f, ReentrantLock l){
        try {
            this.l = l;
            this.servidor = servidor;
            this.tc = tc;
            this.f = f;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        System.out.println("Método run do worker 1!");
        byte [] data = this.f.data;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        Cliente cl = Cliente.deserialize(in);
        boolean sucesso = this.servidor.autenticaCliente(cl);
        System.out.println(cl);
        if(sucesso)
            cl.setAutenticado(true);
        else
            cl.setAutenticado(false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try{ // Evita que respostas diferentes se misturem no socket
            this.l.lock();
            cl.serialize(out);
            out.flush();
            this.tc.send(2,baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally{
            this.l.unlock();
        }
    }
}

// A classe ServerWorker3 serve para responder a pedido com a tag 3 (listar trotinetes livres)
class ServerWorker3 implements Runnable{
    private ReentrantLock l; // Reentrant lock da sessão (da ligação com o cliente) é partilhado por todos os Server Workers
    private Servidor servidor;
    private TaggedConnection tc;
    private TaggedConnection.Frame f;
    public ServerWorker3(Servidor servidor, TaggedConnection tc, TaggedConnection.Frame f, ReentrantLock l){
        try {
            this.l = l;
            this.servidor = servidor;
            this.tc = tc;
            this.f = f;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        byte [] data = this.f.data;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        Posicao posCl = Posicao.deserialize(in);
        System.out.println("Posição do cliente: " + posCl.toString());

        List<Posicao> posicoes = this.servidor.calculaLocaisComTrotisLivres(posCl);
        System.out.println("Posições livres: " + posicoes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try {
            int nrPos = posicoes.size();
            try{ // Evita que respostas diferentes se misturem no socket
                this.l.lock();
                out.writeInt(nrPos); // Envia o número de trotinetes primeiro
                System.out.println("Número de posições com trotinetes livres: " + nrPos);

                for(Posicao pos : posicoes){
                    pos.serialize(out);
                }
                out.flush();
                this.tc.send(3, baos.toByteArray());
            }finally {
                this.l.unlock();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

// A classe ServerWorker4 serve para responder a pedido com a tag 4 (listar recompensas)
class ServerWorker4 implements Runnable{
    private  ReentrantLock l; // Reentrant lock da sessão (da ligação com o cliente) é partilhado por todos os Server Workers
    private Servidor servidor;
    private TaggedConnection tc;
    private TaggedConnection.Frame f;
    public ServerWorker4(Servidor servidor, TaggedConnection tc, TaggedConnection.Frame f, ReentrantLock l){
        try {
            this.l = l;
            this.servidor = servidor;
            this.tc = tc;
            this.f = f;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        byte [] data = this.f.data;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        Posicao posCl = Posicao.deserialize(in);
        System.out.println("Posição do cliente: " + posCl.toString());

        List<Recompensa> recompensas = this.servidor.listaRecompensas(posCl);
        int nrRecompensas = recompensas.size();
        System.out.println("NrRecompensas: " + nrRecompensas);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try{
            this.l.lock();
            out.writeInt(nrRecompensas);
            for(Recompensa r: recompensas) {
                System.out.println("Recompensa resposta: " + r);
                r.serialize(out);
            }
            out.flush();
            this.tc.send(4, baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.l.unlock();
        }
    }
}

// A classe ServerWorker5 serve para responder a pedido com a tag 5 (Reservar trotinete)
class ServerWorker5 implements Runnable{
    private ReentrantLock l; // Reentrant lock da sessão (da ligação com o cliente) é partilhado por todos os Server Workers
    private Servidor servidor;
    private TaggedConnection tc;
    private TaggedConnection.Frame f;
    public ServerWorker5(Servidor servidor, TaggedConnection tc, TaggedConnection.Frame f, ReentrantLock l){
        try {
            this.l = l;
            this.servidor = servidor;
            this.tc = tc;
            this.f = f;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        byte [] data = this.f.data;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        Posicao posCl = Posicao.deserialize(in);
        System.out.println("Posição do cliente: " + posCl.toString());

        Reserva reserva = this.servidor.reservaTrotinete(posCl);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try{ // Evita que respostas diferentes se misturem no socket
            this.l.lock();
            reserva.serialize(out);
            out.flush();
            this.tc.send(5, baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.l.unlock();
        }
        // Serve para debug
        System.out.println(reserva);
    }
}

// A classe ServerWorker6 serve para responder a pedido com a tag 6 (Estacionar troti)
class ServerWorker6 implements Runnable{
    private ReentrantLock l; // Reentrant lock da sessão (da ligação com o cliente) é partilhado por todos os Server Workers
    private Servidor servidor;
    private TaggedConnection tc;
    private TaggedConnection.Frame f;
    public ServerWorker6(Servidor servidor, TaggedConnection tc, TaggedConnection.Frame f, ReentrantLock l){
        try {
            this.l = l;
            this.servidor = servidor;
            this.tc = tc;
            this.f = f;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        byte [] data = this.f.data;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        Estacionamento est = Estacionamento.deserialize(in);
        System.out.println(est);

        Reserva reserva = this.servidor.estacionarTroti(est);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try{ // Evita que respostas diferentes se misturem no socket
            this.l.lock();
            reserva.serialize(out);
            out.flush();
            this.tc.send(6, baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.l.unlock();
        }
        // Serve para debug
        System.out.println(reserva);
    }
}
