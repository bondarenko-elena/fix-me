import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Getter
@Setter
public class SocketManager extends Thread {
    private Socket socket;
    private String clientId;
    private int port;
    private static String readLine = "";

    SocketManager( Socket socket ) {
        this.socket = socket;
        clientId = ( socket.getLocalPort() == 5000 ? "0" : "1" ) + String.valueOf( Instant.now().toEpochMilli() )
                                                                         .substring( 8 );
    }

    @Override
    public void run() {
        portThread( socket );
    }

    private static void portThread( Socket clientSocket ) {
        try (
                BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                PrintWriter out = new PrintWriter( clientSocket.getOutputStream(), true )
        ) {
            readLine = in.readLine();
            System.out.println( "ROUTER: message accepted: " + readLine + "\n" );
        } catch ( IOException ex ) {
            Router.printException( ex );
        }
    }

    private static String createCheckSum( @NotNull String fixMsg ) {
        String returnHash = "";
        try {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            md.update( fixMsg.getBytes() );
            returnHash = DatatypeConverter.printHexBinary( md.digest() ).toLowerCase();
        } catch ( NoSuchAlgorithmException ex ) {
            Router.printException( ex );
        }
        if ( ( returnHash.length() < 1 ) ) {
            System.out.println( "Router: unable to create check sum" );
        }
        return returnHash;
    }

    private static String createFixMessage( @NotNull String msgElem ) {
        String elem[] = msgElem.split( ";" );
        String fixMsg =
                "ID=" + elem[0] + "|" +
                        "PORT=" + elem[1] + "|" +
                        "OPTION=" + elem[2];
        fixMsg += "CHECKSUM=" + createCheckSum( fixMsg );
//        routingTable.add( fixMsg );
        return fixMsg;
    }
}
