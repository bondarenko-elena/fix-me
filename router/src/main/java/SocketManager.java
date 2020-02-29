import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Getter
@Setter
public class SocketManager extends Thread {
    private Socket socket;
    private static String clientId;
    private int port;

    SocketManager( Socket socket ) {
        this.socket = socket;
        clientId = ( socket.getLocalPort() == 5000 ? "0" : "1" )
                + String.valueOf( System.currentTimeMillis() ).substring( 8 );
        try {
            if ( socket.getLocalPort() == 5000 ) {
                System.out.println( "ROUTER: broker connected" );
                SocketSingleton.getInstance().setInBroker( socket );
                SocketSingleton.getInstance().setOutBroker( socket );
            }
            if ( socket.getLocalPort() == 5001 ) {
                System.out.println( "ROUTER: market connected" );
                SocketSingleton.getInstance().setInMarket( socket );
                SocketSingleton.getInstance().setOutMarket( socket );
            }
        } catch ( IOException ex ) {
            Router.printException( ex );
        }
    }

    @Override
    public void run() {
        portThread();
    }

    private void portThread() {
        String readLine;
        try {
            if ( socket.getLocalPort() == 5000 ) {
                // send clientId to Broker
                SocketSingleton.getInstance().getOutBroker().write( clientId + "\n" );
                SocketSingleton.getInstance().getOutBroker().flush();
                readLine = SocketSingleton.getInstance().getInBroker().readLine();
                System.out.println( "ROUTER: message accepted from Broker: " + readLine );
                SocketSingleton.getInstance().getOutMarket().write( readLine + "\n" );
                SocketSingleton.getInstance().getOutMarket().flush();
                System.out.println( "ROUTER: message rerouted to Market: " + readLine );
            } else {
                readLine = SocketSingleton.getInstance().getInMarket().readLine();
                System.out.println("ROUTER: message accepted from market: " + readLine);
                SocketSingleton.getInstance().getOutBroker().write( readLine + "\n" );
                SocketSingleton.getInstance().getOutBroker().flush();
                System.out.println( "ROUTER: message from market rerouted to broker" );
                System.out.println("-------------------ITERATION ENDED-------------------");
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
