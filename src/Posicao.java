import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Posicao {
    private int x;
    private int y;

    public Posicao(){
        this.x = 0;
        this.y = 0;
    }

    public Posicao(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void serialize (DataOutputStream out){
        try{
            out.writeInt(this.x);
            out.writeInt(this.y);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Posicao deserialize (DataInputStream in){
        int x = 0 , y = 0;
        try{
            x = in.readInt();
            y = in.readInt();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Posicao(x,y);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString(){
        return "(" + this.x + "," + this.y+")";
    }

    @Override
    public boolean equals(Object o){
        if( o == this){
            return true;
        }

        if(( o == null) || (o.getClass() != this.getClass())){
            return false;
        }

        Posicao p = (Posicao) o;
        if (this.x == p.getX() && this.y == p.getY()){
            return true;
        }
        return false;
    }

    public Posicao clone(){
        return new Posicao(this.x, this.y);
    }

    @Override
    public int hashCode(){
        return this.x * 31 + this.y * 31; 
    }
}
