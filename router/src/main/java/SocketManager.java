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

    SocketManager( Socket socket ) {
        this.socket = socket;
        clientId = ( socket.getLocalPort() == 5000 ? "0" : "1" ) + String.valueOf( Instant.now().toEpochMilli() )
                                                                         .substring( 8 );
        try {
            if ( socket.getLocalPort() == 5000 ) {
                System.out.println( "ROUTER: broker connected" );
                SocketSingleton.getInstance()
                               .setInBroker( new BufferedReader( new InputStreamReader( socket.getInputStream() ) ) );
                SocketSingleton.getInstance()
                               .setOutBroker( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) ) );
            }
            if ( socket.getLocalPort() == 5001 ) {
                System.out.println( "ROUTER: market connected" );
                SocketSingleton.getInstance()
                               .setInMarket( new BufferedReader( new InputStreamReader( socket.getInputStream() ) ) );
                SocketSingleton.getInstance()
                               .setOutMarket( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) ) );
            }
        } catch ( IOException ex ) {
            Router.printException( ex );
        }
    }

    @Override
    public void run() {
        portThread(  );
    }

    private  void portThread(  ) {
        String readLine = "";
        try {
            if ( socket.getLocalPort() == 5000 ) {
//                System.out.println( "ROUTER: Broker is here" );
                ////
                // send clientId to Broker
                SocketSingleton.getInstance().getOutBroker().write( clientId + "\n" );
                SocketSingleton.getInstance().getOutBroker().flush();
                ////
                readLine = SocketSingleton.getInstance().getInBroker().readLine();
                System.out.println( "ROUTER: message accepted from Broker: " + readLine );
                //todo parse readLine
                SocketSingleton.getInstance().getOutMarket().write( readLine + "\n" );
                SocketSingleton.getInstance().getOutMarket().flush();
                System.out.println( "ROUTER: message rerouted to Market: " + readLine );
            }
            else {
//                System.out.println( "ROUTER: Market is here" );
                readLine = SocketSingleton.getInstance().getInMarket().readLine();
                SocketSingleton.getInstance().getOutBroker().write( readLine );
                SocketSingleton.getInstance().getOutBroker().flush();
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
