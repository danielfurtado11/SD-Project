import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.LocalTime;

// Classe que implementa a reserva de uma trotinete
public class Reserva {
    private Trotinete troti;
    private String codReserva;
    private Posicao posInicial;
    private LocalTime tempoReserva;
    private boolean estacionou; // Variável de suporte de comunicação (Tem que estar sempre atualizada!!!!!)
    private Posicao posFinal; // Variável de instância que só é iniciada aquando do estacionamento
    private int custo; // Variável de instância que só é iniciada aquando do estacionamento
    private boolean recompensa; // Variável de instância que só é iniciada aquando do estacionamento
    private double valorRecompensa; // Variável de instância que só é iniciada aquando do estacionamento

    public Reserva (Trotinete troti, String codReserva, Posicao posInicial){ // Construtor inicial da Reserva
        this.tempoReserva = LocalTime.now();
        this.estacionou = false;
        this.troti = troti;
        this.codReserva = codReserva;
        this.posInicial = posInicial.clone(); // A posição incial tem que ser clonada da posição da Troti (para que não seja alterada)
    }

    // Quando é chamado o construtor vazio quer dizer que a reserva não foi possível de concluir (não há nenhuma trotinete disponível)
    // Se não possível realizar a reserva não se pode meter a trotinete a null
    // Temos que meter uma trotinete inválida
    public Reserva(){
        this.estacionou = false;
        this.custo = 0;
        this.troti = new Trotinete();
        this.codReserva = "-1";
    }

    public Reserva(String codReserva, Posicao posInicial, Trotinete troti, Posicao posFinal, int custo, boolean recompensa, double valorRecompensa, boolean estacinou){
        this.estacionou = estacinou;
        this.posInicial = posInicial;
        this.posFinal = posFinal;
        this.codReserva = codReserva;
        this.troti = troti;
        this.custo = custo;
        this.recompensa = recompensa;
        this.valorRecompensa = valorRecompensa;
    }

    public void serialize (DataOutputStream out){
        try{
            this.troti.serialize(out);
            if(this.troti.getId() != -1){
                out.writeUTF(this.codReserva);
                this.posInicial.serialize(out);
                out.writeBoolean(this.estacionou);
                if(this.estacionou){
                    this.posFinal.serialize(out);
                    out.writeInt(this.custo);
                    out.writeBoolean(this.recompensa);
                    if(this.recompensa){
                        out.writeDouble(this.valorRecompensa);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Reserva deserialize (DataInputStream in){
        Trotinete troti = null; String codReserva = "-1";
        Posicao posInicial = null, posFinal = null;
        boolean estacionou = false, recompensa = false; int custo = 0; double valorRecompensa = 0;
        try{
            troti = Trotinete.deserialize(in);
            if(troti.getId() != -1){
                codReserva = in.readUTF();
                posInicial = Posicao.deserialize(in);
                estacionou = in.readBoolean();

                if(estacionou){
                    posFinal = Posicao.deserialize(in);
                    custo = in.readInt();
                    recompensa = in.readBoolean();
                    if(recompensa){
                        valorRecompensa = in.readDouble();
                    }
                }
            }else{
                return new Reserva();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return new Reserva(codReserva, posInicial, troti, posFinal, custo, recompensa, valorRecompensa, estacionou);
    }

    public String toString(){
        if(this.troti.getId() == -1){ // Não foi possível reservar a trotinete
            return "Não foi possível realizar a reserva!";
        }
        String str = "Reserva com o código " + this.codReserva + " com a posição inicial " + this.posInicial + " associada à " + this.troti + ".\n";
        if(this.estacionou){
            str += "Já foi estacionada na posição " + this.posFinal + " a reserva teve um custo de " + this.custo;
            if(this.recompensa){
                str += " e ainda teve uma recompensa com o valor de " + this.valorRecompensa + "!";
            }else{
                str += ".";
            }
        }
        return str;
    }

    public void setTroti(Trotinete troti) {
        this.troti = troti;
    }

    public void setCodReserva(String codReserva) {
        this.codReserva = codReserva;
    }

    public Trotinete getTroti() {
        return troti;
    }

    public String getCodReserva() {
        return codReserva;
    }

    public void setCusto(int custo) {
        this.custo = custo;
    }

    public void setRecompensa(boolean recompensa) {
        this.recompensa = recompensa;
    }

    public void setValorRecompensa(double valorRecompensa) {
        this.valorRecompensa = valorRecompensa;
    }

    public int getCusto() {
        return custo;
    }

    public boolean isRecompensa() {
        return recompensa;
    }

    public double getValorRecompensa() {
        return valorRecompensa;
    }
    public void setEstacionou(boolean estacionou) {
        this.estacionou = estacionou;
    }

    public boolean isEstacionou() {
        return estacionou;
    }

    public Posicao getPosInicial() {
        return posInicial;
    }

    public Posicao getPosFinal() {
        return posFinal;
    }

    public void setPosInicial(Posicao posInicial) {
        this.posInicial = posInicial;
    }

    public void setPosFinal(Posicao posFinal) {
        this.posFinal = posFinal;
    }

    public LocalTime getTempoReserva() {
        return tempoReserva;
    }
}


