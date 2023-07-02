import java.io.*;
import java.net.*;

public class Cliente implements Serializable {
    private String username;
    private String password;
    private boolean autenticado;

    public Cliente(String username, String password){
        this.username = username;
        this.password = password;
    }

    public Cliente(String username, String password, boolean autenticado){
        this.username = username;
        this.password = password;
        this.autenticado = autenticado;
    }

    public void serialize (DataOutputStream out){
        try{
            out.writeUTF(this.username);
            out.writeUTF(this.password);
            out.writeBoolean(this.autenticado);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Cliente deserialize (DataInputStream in){
        String username = "", password = "";
        boolean autenticado = false;
        try{
            username = in.readUTF();
            password = in.readUTF();
            autenticado = in.readBoolean();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Cliente(username, password, autenticado);
    }

    public String toString(){
        return "Username: " + this.username + "\nPassword: " + this.password;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAutenticado(boolean autenticado){
        this.autenticado = autenticado;
    }

    public boolean getAutenticado(){
        return this.autenticado;
    }
}
