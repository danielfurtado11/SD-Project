import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GereReserva{
    private ReentrantLock l;                       // Lock
    private Mapa mapa;                             // Mapa do Sistema
    private Map<String,Reserva> reservas;          // Map com todas as reservas
    private GereRecompensa grecom;
    
    // Construtor de GereReservas
    public GereReserva(Mapa mapa, GereRecompensa grecom){
        this.mapa = mapa;
        this.l = new ReentrantLock();
        this.reservas = new HashMap<>();
        this.grecom = grecom;
    }
   
    public Reserva reservaTrotinete(Posicao posCl, int distancia){
        int menorDist = 10000;
        Trotinete trotiMaisProxima = null;

        try{
            this.l.lock();
            List<Trotinete> trotinetes = this.mapa.getTrotinetes();
            //Procura a trotinete mais proxima
            for(Trotinete troti : trotinetes){
                if(troti.getFree()){
                    Posicao post = troti.getPosicao();
                    int dist = this.mapa.calculaDist(posCl, post);
                    if(dist < menorDist && dist <= distancia){
                        menorDist = dist;
                        trotiMaisProxima = troti;
                    }
                }
            }

            //Verifica que ha uma trotine livre e proxima,
            //coloca essa trotina ocupada e gera a reserva
            if (trotiMaisProxima != null) {
                String codReserva = this.geraCodReserva();
                trotiMaisProxima.setFree(false);
                this.mapa.retiraTrotinete(trotiMaisProxima);
                this.mapa.printMap();
                Reserva reserva = new Reserva(trotiMaisProxima, codReserva, trotiMaisProxima.getPosicao());
                this.adicionaListaReservas(reserva);
                return reserva;
            }

            return new Reserva();
        }finally {
            this.l.unlock();
        }
    }
    
    public String geraCodReserva(){
        String codReserva = "";

        for (int i = 0; i < 20; i++) {
            int c = ThreadLocalRandom.current().nextInt(1, 3);
            if (c == 1) {
                Random r = new Random();
                codReserva += (char) (r.nextInt(26) + 'A');
            } else if (c == 2) {
                Random r = new Random();
                codReserva += (char) (r.nextInt(26) + 'a');
            }
        }

        return codReserva;
    }

    //Método que adiciona a reserva ao Map de reservas (método que chama este método já tem o lock)
    public void adicionaListaReservas(Reserva r){
        this.reservas.put(r.getCodReserva(),r);
    }
    
    //Método que retira a reserva do Map de reservas e coloca a trotinete livre
    public Reserva estacionar(Estacionamento est){
        Reserva r = null;
        try{
            this.l.lock();
            r = this.reservas.remove(est.getCodReserva()); // Vai buscar a reserva associada ao estacionamento
        }finally{
            l.unlock();
        }

        r.getTroti().setFree(true);
        r.getTroti().setPosicao(est.getPosEstacionamento());
        this.mapa.colocaTrotinete(r.getTroti()); // Coloca a trotinete estacionada no mapa
        r.setPosFinal(est.getPosEstacionamento().clone()); // A posição final também não pode ser alterada
        r.setEstacionou(true);
        Duration duration = Duration.between(r.getTempoReserva(), LocalTime.now());
        r.setCusto(2 * this.mapa.calculaDist(r.getPosInicial(), r.getPosFinal()) + (int) (duration.getSeconds()/10));
        this.mapa.printMap();   // dar print ao map para dar debug
        double valorRecompensa = this.grecom.verificaViagemRecompensa(r.getPosInicial(), r.getPosFinal());
        if(valorRecompensa != 0){
            r.setRecompensa(true);
            r.setValorRecompensa(valorRecompensa);
        }
        return r;
    }
}
