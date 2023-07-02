import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Trotinete {
    private boolean livre;
    private int id;
    private Posicao pos;

    public Trotinete(int x, int y, int id, boolean status){
        this.pos = new Posicao(x,y);
        this.id = id;
        this.livre = status;
    }

    public Trotinete(boolean livre, int id, Posicao pos){
        this.livre = livre;
        this.id = id;
        this.pos = pos;
    }

    public Trotinete(){
        this.livre = false;
        this.id = -1;
        this.pos = new Posicao();
    }

    public void serialize (DataOutputStream out){
        try{
            out.writeBoolean(this.livre);
            out.writeInt(this.id);
            this.pos.serialize(out); // A posição já faz o flush
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Trotinete deserialize (DataInputStream in){
        boolean livre = true; int id = 0; Posicao pos = null;
        try{
            livre = in.readBoolean();
            id = in.readInt();
            pos = Posicao.deserialize(in);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Trotinete(livre, id, pos);
    }

    public String toString(){
        String str = null;
        if (this.livre){
            str = "Trotinete livre com o id " + this.id + " na posição " + this.pos.toString();
        }else{
            str = "Trotinete ocupada com o id " + this.id + " na posição " + this.pos.toString();
        }
        return str;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    public void setFree(boolean ocupacao){
        this.livre = ocupacao;
    }

    public boolean getFree(){
        return this.livre;
    }

    public void setPosicao(Posicao pos){
        this.pos = pos;
    }

    public Posicao getPosicao(){
        return this.pos;
    }
}