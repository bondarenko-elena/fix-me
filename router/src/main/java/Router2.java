import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Getter
@Setter
public class Router2 extends Thread {
    private Socket socket;
    private String clientId;
    private String checkSum;

    Router2( @NotNull Socket socket ) {
        if ( socket.getLocalPort() == 5000 ) {
            System.out.println( "Router: broker connected" );
        } else {
            System.out.println( "Router: market connected" );
        }
        System.out.println( "Router: port " + socket.getLocalPort() );
        this.socket = socket;
        this.clientId = ( socket.getLocalPort() == 5000
                ? "0"
                : "1" ) + String.valueOf( Instant.now()
                                                 .toEpochMilli() )
                                .substring( 11 );
        this.checkSum = createCheckSum( clientId );
        System.out.println( "Router: clientId " + clientId );
        System.out.println( "Router: check sum " + checkSum );
    }

    private static String createCheckSum( @NotNull String clientId ) {
        String returnHash = "";
        try {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            md.update( clientId.getBytes() );
            returnHash = DatatypeConverter.printHexBinary( md.digest() ).toLowerCase();
        } catch ( NoSuchAlgorithmException ex ) {
            Router.printException( ex );
        }
        if ( ( returnHash.length() < 1 ) ) {
            System.out.println( "Router: unable to create check sum" );
        }
        return returnHash;
    }

    public String createFIXmessage( String msg ) {
        String elements[] = msg.split( ";" );
        String fixmessage =
                "ID=" + elements[0] +
                        "|Instrument=" + elements[1] +
                        "|Quantity=" + elements[2] +
                        "|Market=" + elements[3] +
                        "|Price=" + elements[4] + "|";
        fixmessage += "10=" + createCheckSum( fixmessage );
        return fixmessage;
    }

    @Override
    public void run() {
        portThread( socket );
    }

    private static void portThread( @NotNull Socket socket ) {
        String str;
        try ( BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) ) ) {
            PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
            while ( true ) {
                str = in.readLine();
                if ( str.equalsIgnoreCase( "exit" ) ) {
                    break;
                }
                out.println( "ROUTER: " + str );
            }
        } catch ( IOException ex ) {
            Router.printException( ex );
        }
    }
}
