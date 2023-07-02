import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Estacionamento {
    private String codReserva;
    private Posicao posEstacionamento;

    public Estacionamento(String codReserva, Posicao pos){
        this.codReserva = codReserva;
        this.posEstacionamento = pos;
    }

    public void serialize (DataOutputStream out){
        try{
            out.writeUTF(this.codReserva);
            this.posEstacionamento.serialize(out);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Estacionamento deserialize (DataInputStream in){
        String codReserva = ""; Posicao posEstacionamento = null;
        try{
            codReserva = in.readUTF();
            posEstacionamento = Posicao.deserialize(in);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Estacionamento(codReserva, posEstacionamento);
    }

    public String toString(){
        return "Cliente pede para estacionar a trotinete associada ao código de reserva " + this.codReserva + " na posição " + this.posEstacionamento;
    }

    public void setCodReserva(String codReserva) {
        this.codReserva = codReserva;
    }

    public void setPosEstacionamento(Posicao posEstacionamento) {
        this.posEstacionamento = posEstacionamento;
    }

    public String getCodReserva() {
        return codReserva;
    }

    public Posicao getPosEstacionamento() {
        return posEstacionamento;
    }
}
