import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Mapa {
    private int dim;
    private List<List<List<Trotinete>>> matriz;
    private List<Trotinete> trotinetes;
    private ReadWriteLock rwl;

    public Mapa(int dim){
        this.rwl = new ReentrantReadWriteLock();
        this.dim = dim;
        this.matriz = new ArrayList<>();
        this.trotinetes = new ArrayList<>(); // Lista de todas as trotinetes existentes no mapa

        // Inicialização da matriz de listas de trotis
        for (int i=0 ; i<dim ; i++){
            this.matriz.add(new ArrayList<>());
            for (int j=0 ; j<dim ; j++)
                this.matriz.get(i).add(new ArrayList<>());
        }

        int nrTrotinetes = 3*dim;
        Random random = new Random();
        for(int i=0; i<nrTrotinetes; i++){
            int x = random.nextInt(dim);
            int y = random.nextInt(dim);
            Trotinete troti = new Trotinete(x,y,i+1,true);
            this.matriz.get(x).get(y).add(troti);
            this.trotinetes.add(troti);
        }
    }

    // Apenas lê do trotinetes
    public List<Posicao> locaisComTrotisLivres(Posicao posCl, int distancia){
        List<Posicao> posicoes = new ArrayList<>();
        try{
            this.rwl.readLock().lock();

            for(Trotinete troti : this.trotinetes){
                if(troti.getFree()){ // Se a trotinete estiver livre
                    Posicao post = troti.getPosicao();
                    int dist = this.calculaDist(posCl, post);
                    if(dist <= distancia && !posicoes.contains(post)){
                        posicoes.add(post);
                    }
                }
            }
        }finally {
            this.rwl.readLock().unlock();
        }
        return posicoes;
    }

    public int calculaDist(Posicao pos1, Posicao pos2){
        int x1 = pos1.getX();
        int x2 = pos2.getX();
        int y1 = pos1.getY();
        int y2 = pos2.getY();
        return Math.abs(x1-x2)+Math.abs(y1-y2);
    }


    public List<Trotinete> getTrotinetes(){
        try{
            this.rwl.readLock().lock();
            return this.trotinetes;
        }finally {
            this.rwl.readLock().unlock();
        }
    }

    // Retira a trotinete reservada no mapa
    public void retiraTrotinete(Trotinete t){
        int x = t.getPosicao().getX();
        int y = t.getPosicao().getY();

        try{
            this.rwl.writeLock().lock();
            this.matriz.get(x).get(y).remove(t);
        }finally {
            this.rwl.writeLock().unlock();
        }
    }

    // Coloca a trotinete estacionada no mapa
    public void colocaTrotinete(Trotinete t){
        int x = t.getPosicao().getX();
        int y = t.getPosicao().getY();

        try{
            this.rwl.writeLock().lock();
            this.matriz.get(x).get(y).add(t);
        }finally {
            this.rwl.writeLock().unlock();
        }
    }

    public String toString(){
        String result = "Dimensão do mapa: " + Integer.toString(this.dim) + "\n";
        try{
            this.rwl.readLock().lock();
            for(int i=0; i<this.dim; i++){
                for(int j=0; j<this.dim; j++) {
                    if(this.matriz.get(i).get(j).isEmpty()){
                        result += " V ";
                    }else{
                        result += " T ";
                    }
                }
                result += "\n";
            }
            return result;
        }finally {
            this.rwl.readLock().unlock();
        }
    }

    public int getNrTrotis(){
        try{
            this.rwl.readLock().lock();
            return this.trotinetes.size();
        }finally {
            this.rwl.readLock().unlock();
        }
    }

    public int getDim(){
        try{
            this.rwl.readLock().lock();
            return this.dim;
        }finally {
            this.rwl.readLock().unlock();
        }
    }

    public List<List<List<Trotinete>>> getMatrix(){
        try{
            this.rwl.readLock().lock();
            return this.matriz;
        }finally {
            this.rwl.readLock().unlock();
        }
    }

    // Apenas para debug dá print ao mapa.
    public void printMap(){
        try{
            this.rwl.readLock().lock();
            String result = "Dimensão do mapa: " + Integer.toString(this.dim) + "\n";
            System.out.println(result);
            for (List<List<Trotinete>> linha : this.matriz){
                for (List<Trotinete> posicao  : linha)
                    if (posicao.isEmpty())
                        System.out.print("[ ]");
                    else{
                        int s = posicao.size();
                        System.out.print("["+ s +"]");
                    }
                System.out.println("");
            }
        }finally {
            this.rwl.readLock().unlock();
        }
    }
}
