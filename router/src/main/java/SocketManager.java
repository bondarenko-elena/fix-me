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
    private static String clientId;
    private int port;
    private static BufferedReader inBroker;
    private static BufferedWriter outBroker;
    private static BufferedReader inMarket;
    private static BufferedWriter outMarket;
    private static String readLine = "";

    SocketManager( Socket socket ) {
        this.socket = socket;
        clientId = ( socket.getLocalPort() == 5000 ? "0" : "1" ) + String.valueOf( Instant.now().toEpochMilli() )
                                                                         .substring( 8 );
        try {
            if ( socket.getLocalPort() == 5000 ) {
                System.out.println( "ROUTER: broker connected" );
                inBroker = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                outBroker = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
            }
            if ( socket.getLocalPort() == 5001 ) {
                System.out.println( "ROUTER: market connected" );
                inMarket = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                outMarket = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
            }
        } catch ( IOException ex ) {
            Router.printException( ex );
        }
    }

    @Override
    public void run() {
        portThread( socket );
    }

    private static void portThread( Socket clientSocket ) {
        try {
            if ( clientSocket.getLocalPort() == 5000 ) {
//                System.out.println( "ROUTER: Broker is here" );
                ////
                // send clientId to Broker
                outBroker.write( clientId + "\n" );
                outBroker.flush();
                ////
                readLine = inBroker.readLine();
                System.out.println( "ROUTER: message accepted from Broker: " + readLine );
                outMarket.write( createFixMessage( clientId + ";" + clientSocket.getLocalPort() + ";" + readLine ) + "\n" );
                outMarket.flush();
                System.out.println( "ROUTER: message rerouted to Market: " + readLine );
            }
            if ( clientSocket.getLocalPort() == 5001 ) {
//                System.out.println( "ROUTER: Market is here" );
                readLine = inMarket.readLine();
                outBroker.write( readLine );
                outBroker.flush();
                System.out.println( "ROUTER: message from market rerouted to broker" );
            }

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
        String[] elem = msgElem.split( ";" );
        String fixMsg =
                "ID=" + elem[0] +
                        "|PORT=" + elem[1] +
                        "|OPTION=" + elem[2];
        fixMsg += "|CHECKSUM=" + createCheckSum( fixMsg );
        return fixMsg;
    }
}
