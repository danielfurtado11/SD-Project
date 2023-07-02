import java.io.*;
import java.net.Socket;
public class TaggedConnection implements AutoCloseable {
    private Socket s;
    private DataInputStream in;
    private DataOutputStream out;
    public static class Frame {
        public final int tag;
        public final byte[] data;

        public Frame(int tag, byte[] data) {
            this.tag = tag; this.data = data;
        }
    }
    public TaggedConnection(Socket socket){
        this.s = socket;

        try{
            InputStream in = this.s.getInputStream();
            this.in = new DataInputStream(in);

            OutputStream out = this.s.getOutputStream();
            this.out = new DataOutputStream(out);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void send(Frame frame){
        try{
            this.out.writeInt(frame.tag);
            this.out.writeInt(frame.data.length);
            this.out.write(frame.data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void send(int tag, byte[] data){
        try{
            this.out.writeInt(tag);
            this.out.writeInt(data.length);
            this.out.write(data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Frame receive(){
        int tag = -1;
        byte [] data = null;

        try {
            tag = this.in.readInt();
            int comp = this.in.readInt();
            data = new byte[comp];
            this.in.readFully(data);
        }catch (Exception e){
            e.printStackTrace();
        }

        return new Frame(tag, data);
    }

    public void close() throws IOException {
        this.s.close();
    }
}