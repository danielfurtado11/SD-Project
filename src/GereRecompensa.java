import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GereRecompensa implements Runnable{
    private Condition condition; // Condition para avaliar as recompensas
    // Este lock é preciso afinal para cenas a ver com a condition
    private ReentrantLock l;
    private Set<Recompensa> recompensas; 
    private Mapa mapa;
    private int raio;
    private boolean reservouOuEstacionou;

    public GereRecompensa(Mapa m, int r){
        this.reservouOuEstacionou = false;
        this.mapa = m;
        this.l = new ReentrantLock();
        this.condition = this.l.newCondition(); // A condition está associada ao lock do server e não ao lock do GereRecompensa
        this.recompensas = new HashSet<>();
        this.raio = r;
    }


    // Método que o Gera o Valor da Recompensa
    public double geraValorRecompensa(double dist){
        //estamos a passar uma dist por enquanto
        double val=0;
        //se andar menos que um metro nao recebe recompensa
        if(dist < 1){
            val= 0;
        }
        if(dist>0 && dist <=10){
            val= 0.1*dist;
        }
        if(dist < 10 && dist <=20){
            val= 0.2*dist;
        }
        if(dist < 20 && dist <=30){
            val= 0.3*dist;
        }
        if(dist>30 && dist <=40){
            val= 0.4*dist;
        }
        if(dist < 40 && dist <=50){
            return 0.5*dist;
        }
        
        return val; 
    }

    public void adicionaRecompensaLista(Recompensa r){
        this.recompensas.add(r);
    }
    
    public void removeRecompensaLista(Recompensa r){
        this.recompensas.remove(r);
    }

    public double verificaViagemRecompensa(Posicao ini, Posicao fim){ // Recebe a posição incial e final de uma reserva e verifica se existe uma recompensa asssociada a essa viagem
        double valorRecompensa = 0;

        try{
            this.l.lock();
            for(Recompensa r : this.recompensas){
                if(r.getPosInicio().equals(ini) && r.getPosFinal().equals(fim)){
                    this.recompensas.remove(r);
                    valorRecompensa = r.getValor();
                    break;
                } else if(r.getPosInicio().equals(ini)){
                    this.recompensas.remove(r);
                    break;
                }
            }
        }finally {
            this.l.unlock();
        }
        return valorRecompensa;
    }

    public int nrRecompensaPos(Posicao pos){
        int res = 0;
        for(Recompensa r : this.recompensas){
            if(r.getPosInicio().equals(pos))
                res++;
        }
        return res;
    }

    // Este método 
    public void verificaRecompensa(){
        int i = 0;
        List<List<List<Trotinete>>> matrix = this.mapa.getMatrix();
        for (List<List<Trotinete>> linha : matrix)
            for (List<Trotinete> posicao : linha)
                if (posicao.size() > 1 && this.nrRecompensaPos(posicao.get(0).getPosicao()) < posicao.size() - 1){
                    Trotinete t = posicao.get(0);
                    Posicao pI = t.getPosicao();
                    Posicao pF = dalista().get(i);
                    i++;
                    if(pF.getX() !=-1 && pF.getY() != -1){
                        if(!this.existsPosition(pF)){
                            Recompensa r = new Recompensa(pI,pF);
                            this.adicionaRecompensaLista(r);
                    }
                    else{
                        System.out.println("Não há recompensa!");
                    }
                    }
                }
    }

    // Método que devolve a lista de todas as posicoes disponiveis para serem
    private List<Posicao> dalista(){
        List<Posicao> l = new ArrayList<>();
        int dim = this.mapa.getDim();

        for (int i = 0; i<dim; i+=this.raio){

            for (int j = 0; j<dim; j+=raio){
                boolean b = notExistsTrotinete(i, j);
                if(b){
                    Posicao d = new Posicao(i,j);
                    l.add(d);
                }
            }
        }
        return l;
    }

    // Método que Devolve true caso no raio da posicao não tenha uma trotinete
    private boolean notExistsTrotinete(int x, int y){
        List<List<List<Trotinete>>> matrix = this.mapa.getMatrix();
        int dim = this.mapa.getDim();
        int raio =  this.raio;

        for (    int i =  x - raio; i <= x + raio; i++){
            for (int j =  y - raio; j <= y + raio; j++){
                if (i>=0 && i< dim && j>=0 && j<dim){
                    List<Trotinete> list = (matrix.get(i)).get(j);
                    if (!list.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Método que Devolve a true caso a PF ja esteja associada a uma recompensa
    public boolean existsPosition(Posicao p){
            
        for(Recompensa r : this.recompensas){
            if (r.getPosFinal() == p){
                return true;
            }
        }
        return false;
    }

    public List<Recompensa> listaRecompensas(Posicao posCl){
        try{
            this.l.lock();
            List<Recompensa> recompensasProx = new ArrayList<>();
            for(Recompensa r : this.recompensas){
                Posicao p = r.getPosInicio();
                if(this.mapa.calculaDist(p,posCl) <= this.raio)
                    recompensasProx.add(r);
            }
            return recompensasProx;
        }finally {
            this.l.unlock();
        }
    }

    // Método que acorda a thread para recalcular as recompensas
    public void recalculaRecompensas(){
        try{
            this.l.lock();
            this.reservouOuEstacionou = true;
            this.condition.signal();
        }finally {
            this.l.unlock();
        }
    }
    
    // O que está nesta função é o que a thread dedicada a gerir as recompensas vai fazer durante toda a execução do servidor
    public void run(){
        try {
            while(true){
                try{
                    this.l.lock();
                    while(!this.reservouOuEstacionou){
                        System.out.println("Dormir...");
                        this.condition.await();
                    }
                    System.out.println("Tou a verificiar recompensas!");
                    this.verificaRecompensa();
                    System.out.println(this.recompensas);
                    this.reservouOuEstacionou = false;
                }finally {
                    this.l.unlock();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setReservouOuEstacionou(boolean bo){
        try{
            this.l.lock();
            this.reservouOuEstacionou = bo;
        }finally {
            this.l.unlock();
        }
    }

    public boolean getReservouOuEstacionou(){
        try {
            this.l.lock();
            return this.reservouOuEstacionou;
        }finally {
            this.l.unlock();
        }
    }
    public ReentrantLock getL() {
        return l;
    }
}