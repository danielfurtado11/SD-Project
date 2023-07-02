import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable {
    private TaggedConnection tc;
    // Mapa de tag para objeto com uma condition e uma queue de frames
    private Map<Integer, TaggedFrame> queueMsg;
    private ReentrantLock l;

    public Demultiplexer(TaggedConnection conn) {
        this.tc = conn;
        this.l = new ReentrantLock();
        this.queueMsg = new HashMap<>();
        // Vamos inicializar o mapa com 6 entradas (uma por tag) cada uma com uma queue de frames
        for(int i = 0; i < 9; i++){
            TaggedFrame tf = new TaggedFrame(this.l);
            this.queueMsg.put(i+1,tf);
        }
    }

    public Thread start(){

        Thread t = new Thread(() -> {
            int tag = 0;
            while(tag != 9){ // Implementa o logout (tag 9)
                try{
                    TaggedConnection.Frame f = this.tc.receive();
                    tag = f.tag;
                    this.l.lock();
                    try{
                        this.queueMsg.get(f.tag).msgDeque.add(f);
                        this.queueMsg.get(f.tag).c.signal();
                    }finally {
                        this.l.unlock();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        t.start();
        return t;
    }

    public void send(TaggedConnection.Frame frame){
        try{
            this.tc.send(frame);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void send(int tag, byte[] data){
        try{
            this.tc.send(tag, data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public byte[] receive(int tag){
        TaggedConnection.Frame f = null;
        this.l.lock();
        try{
            // Se a queue associada à tag estiver vazia a thread vai esperar por uma frame com essa tag
            while(this.queueMsg.get(tag).msgDeque.isEmpty())
                this.queueMsg.get(tag).c.await();

            f = this.queueMsg.get(tag).msgDeque.remove();
            this.l.unlock();
        }catch (Exception e){
            e.printStackTrace();
        }
        return f.data;
    }

    public void close() throws IOException {
        this.tc.close();
    }
}

class TaggedFrame{
    public Condition c;
    public Deque<TaggedConnection.Frame> msgDeque;

    public TaggedFrame(ReentrantLock l){
        this.c = l.newCondition();
        this.msgDeque = new ArrayDeque<>();
    }
}
