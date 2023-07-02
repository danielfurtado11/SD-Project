import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GereClientes {
    private ReadWriteLock l;
    private Map<String, Cliente> clientes; // Map de utilizadores de username para password

    public GereClientes(){
        this.l = new ReentrantReadWriteLock();
        this.clientes = new HashMap<>();
    }

    // Devolve falso se já existir o nome
    public boolean adicionaNovoCliente(Cliente c){
        try{
            this.l.writeLock().lock();
            String nome = c.getUsername();
            boolean existe = this.clientes.containsKey(nome);
            if(!existe)
                this.clientes.put(nome, c);
            return !existe;
        }finally {
            this.l.writeLock().unlock();
        }
    }

    public boolean autenticaCliente(Cliente c){
        try{
            this.l.readLock().lock();
            String nome = c.getUsername();
            String pass = c.getPassword();
            if(this.clientes.containsKey(nome))
                if(this.clientes.get(nome).getPassword().equals(pass))
                    return true;
            return false;
        }finally {
            this.l.readLock().lock();
        }
    }
}
