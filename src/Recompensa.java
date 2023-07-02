import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Recompensa {
    private Posicao posInicio; //estado inicial
    private Posicao posFinal; //estado final desejado 
    private double valor; // valor da recompensa

    
    
    public Recompensa(Posicao i ,Posicao f){
        this.posInicio = i;
        this.posFinal = f;
        this.valor = this.geraValor(i, f);
    }

    public Recompensa(Posicao i ,Posicao f, double valor){ // Este construtor é usado no deserealize (comunicação)
        this.posInicio = i;
        this.posFinal = f;
        this.valor = valor;
    }

    public void serialize (DataOutputStream out){
        try{
            out.writeDouble(this.valor);
            this.posInicio.serialize(out);
            this.posFinal.serialize(out);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Recompensa deserialize (DataInputStream in){
        Posicao posInicial = null; Posicao posFinal = null;
        double valor = 0;
        try{
            valor = in.readDouble();
            posInicial = Posicao.deserialize(in);
            posFinal = Posicao.deserialize(in);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Recompensa(posInicial, posFinal, valor);
    }

    public void setPosInicio(Posicao i){
        this.posInicio = i;
    }
    
    public void setPosFim(Posicao f){
        this.posFinal = f;
    }

    public void setValor(Double v){
        this.valor = v;
    }

    public String toString(){
        return "Recompensa: (PosI:"+this.posInicio+",PosF:"+this.posFinal+ ") Valor: " + this.valor;
    }

    @Override
    public boolean equals(Object o){
        if( o == this){
            return true;
        }

        if(( o == null) || (o.getClass() != this.getClass())){
            return false;
        }

        Recompensa r = (Recompensa) o;
        if (this.posInicio.equals(r.getPosInicio()) && this.posFinal.equals(r.getPosFinal())){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return this.posInicio.hashCode() + this.posFinal.hashCode();
    }

    public Posicao getPosInicio(){
        return this.posInicio;
    }
    
    public Posicao getPosFinal(){
        return this.posFinal;
    }

    public Double getValor(){
        return this.valor;
    }
    
    public double geraValor(Posicao i, Posicao f){
         return Math.abs(i.getX() - f.getX()) + Math.abs(i.getY() - f.getY());
    }
}
