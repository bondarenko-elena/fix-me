import lombok.Getter;
import lombok.Setter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;

@Getter
@Setter
public class SocketManager extends Thread {
    private Socket socket;
    private String clientId;

    SocketManager( Socket socket) {
        System.out.println("port" + socket.getPort());
        this.socket = socket;
        String epochString = String.valueOf( Instant.now().toEpochMilli());
        this.clientId = (socket.getPort() == 5000? "0":"1") + epochString.substring(8);
        System.out.println("\u001B[36mID: "+ clientId);
    }

    @Override
    public void run() {
        portThread(socket);
    }

    private static void portThread(Socket socket) {
        String ANSI_CYAN = "\u001B[36m";
        try {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader( socket.getInputStream()));
            PrintWriter output = new PrintWriter( socket.getOutputStream(), true);
            while(true) {
                String echoString = input.readLine();
                if(echoString.equals("exit")) {
                    break;
                }
                output.println(ANSI_CYAN + echoString);
            }
        } catch( IOException e) {
            System.out.println("Oops: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch(IOException e) {
                // TODO catch?
            }
        }
    }
}
