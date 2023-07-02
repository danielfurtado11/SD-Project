import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MenuCliente {
    private BufferedReader systemIn;

    public MenuCliente(){
        this.systemIn = new BufferedReader(new InputStreamReader(System.in));
    }

    public int menuAutenticarRegistar(){
        System.out.println("+------------ MENU -----------+");
        System.out.println("| 1: Registar na plataforma   |");
        System.out.println("| 2: Autenticar na plataforma |");
        System.out.println("+-----------------------------+");
        System.out.print("Insira uma opção: ");
        try {
            int op = Integer.parseInt(this.systemIn.readLine());
            return op;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int menuPrincipal(){
        System.out.println("\n+------------------------------ MENU ----------------------------+");
        System.out.println("| 1: Listar trotinetes livres perto de um determinado local.     |");
        System.out.println("| 2: Listar recompensas perto de um determinado local.           |");
        System.out.println("| 3: Reservar uma trotinete livre perto de um determinado local. |");
        System.out.println("| 4: Estacionar trotinete com o código de reserva e o local.     |");
        System.out.println("| 5: Notificações.                                               |");
        System.out.println("| 6: Log out.                                                    |");
        System.out.println("+----------------------------------------------------------------+");

        try {
            System.out.print("Insira uma opção: ");
            int op = Integer.parseInt(this.systemIn.readLine());
            return op;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Cliente menu1(){
        String nome = "", pass = "";
        try {
            System.out.println("\nIndique o seu nome de utilizador: ");
            nome = this.systemIn.readLine();
            System.out.println("\nIndique a sua palavra-passe: ");
            pass = this.systemIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Cliente(nome, pass);
    }

    public Posicao menu2_3_4() {
        int x = 0, y = 0;
        try {
            System.out.println("\nIndique as coordenadas X: ");
            x = Integer.parseInt(this.systemIn.readLine());
            System.out.println("\nIndique as coordenadas Y: ");
            y = Integer.parseInt(this.systemIn.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Posicao(x,y);
    }

    public Estacionamento menu5(){
        String codRes = "";
        int x = 0, y = 0;
        try {
            System.out.println("\nIndique o código de reserva: ");
            codRes = this.systemIn.readLine();
            System.out.println("\nIndique as coordenadas X onde pretende estacionar: ");
            x = Integer.parseInt(this.systemIn.readLine());
            System.out.println("\nIndique as coordenadas Y onde pretende estacionar: ");
            y = Integer.parseInt(this.systemIn.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Estacionamento(codRes, new Posicao(x,y));
    }

    public int menu6(){
        int op = 0;
        System.out.println("\n1: Ativar notificações com origem perto de um determinado local");
        System.out.println("2: Desativar notificações");
        try {
            op = Integer.parseInt(this.systemIn.readLine());   
        } catch (IOException e) {
            e.printStackTrace();
        }

        return op;
    }

    public Posicao notificacoes(){
        int x = 0, y = 0;
        try {
            System.out.println("\nIndique as coordenadas X da origem das recompensas: ");
            x = Integer.parseInt(this.systemIn.readLine());
            System.out.println("\nIndique as coordenadas Y da origem das recompensas: ");
            y = Integer.parseInt(this.systemIn.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Posicao(x,y);
    }
}