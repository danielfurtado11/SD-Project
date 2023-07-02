import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class GereNotificacoes {
    private List<Boolean> notificacoes;
    private ReentrantLock lockNotificacoes;
    private Condition conditionNotificacoes;
    private GereRecompensa gr;

    public GereNotificacoes(GereRecompensa gr){
        this.notificacoes = new ArrayList<>();
        this.gr = gr;
        this.lockNotificacoes = gr.getL(); // O lock das notificacoes é o mesmo das recompensas
        this.conditionNotificacoes = this.lockNotificacoes.newCondition(); // Condition associada às notificações
    }

    // Acorda todas as threads de todos os clientes das notificações
    public void calculaNotificacoes(){
        try{
            this.lockNotificacoes.lock();
            for(int i = 0; i < this.notificacoes.size(); i++)
                this.notificacoes.set(i,true);
            this.conditionNotificacoes.signalAll();
        }finally{
            this.lockNotificacoes.unlock();
        }
    }

    public List<Recompensa> calculcaNotificacoes(Posicao posNoti){
        return this.gr.listaRecompensas(posNoti);
    }
    
    public int adicionaNotificacao(){
        this.notificacoes.add(false);
        return this.notificacoes.size()-1;
    }

    public ReentrantLock getLockNotificacoes() {
        return lockNotificacoes;
    }

    public Condition getConditionNotificacoes() {
        return conditionNotificacoes;
    }

    public GereRecompensa getGr() {
        return gr;
    }

    public boolean isNotificao(int index) {
        return this.notificacoes.get(index);
    }

    public void setNotificacao(int index, boolean bool) {
        this.notificacoes.set(index,bool);
    }
}
