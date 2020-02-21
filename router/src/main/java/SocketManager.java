import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

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

    SocketManager( @NotNull Socket socket ) {
        System.out.println( "Port " + socket.getPort() );
        this.socket = socket;
        this.clientId = ( socket.getPort() == 5000 ? "0" : "1" ) + String.valueOf( Instant.now().toEpochMilli() )
                                                                         .substring( 8 );
        System.out.println( "ClientId " + clientId );
    }

    //todo refactor
    @Override
    public void run() {
        portThread( socket );
    }

    private static void portThread( @NotNull Socket socket ) {
        String str;
        try ( BufferedReader br = new BufferedReader( new InputStreamReader( socket.getInputStream() ) ) ) {
            PrintWriter pw = new PrintWriter( socket.getOutputStream(), true );
            while ( true ) {
                str = br.readLine();
                if ( str.equalsIgnoreCase( "exit" ) ) {
                    break;
                }
                pw.println( "\u001b[32m" + str );
            }
        } catch ( IOException ex ) {
            Router.printException( ex );
        }
    }
}
