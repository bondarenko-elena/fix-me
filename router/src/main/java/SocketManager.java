import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@Setter
public class SocketManager extends Thread {
    private Socket socket;
    private String clientId;
    private int port;
    private static Map<String, Socket> routingTable = new HashMap<>( );

    SocketManager( Socket socket ) {
        this.socket = socket;
        clientId = ( socket.getLocalPort() == 5000 ? "0" : "1" )
                + String.valueOf( System.currentTimeMillis() ).substring( 8 );
        try {
            if ( socket.getLocalPort() == 5000 ) {
                routingTable.put("BROKER -> " + clientId, socket);
                System.out.println( "ROUTER: broker connected" );
                SocketSingleton.getInstance().setInBroker( socket );
                SocketSingleton.getInstance().setOutBroker( socket );
            }
            if ( socket.getLocalPort() == 5001 ) {
                routingTable.put("MARKET -> " + clientId, socket);
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
                System.out.println( "ROUTER: message accepted from broker: " + readLine );
                if (!validateCheckSum(readLine)) {
                    System.out.println("CheckSum validation is failed");
                    System.exit(0);
                }
                SocketSingleton.getInstance().getOutMarket().write( readLine + "\n" );
                SocketSingleton.getInstance().getOutMarket().flush();
                System.out.println( "ROUTER: message rerouted to market: " + readLine );
            } else {
                SocketSingleton.getInstance().getOutMarket().write( clientId + "\n" );
                SocketSingleton.getInstance().getOutMarket().flush();
                readLine = SocketSingleton.getInstance().getInMarket().readLine();
                System.out.println("ROUTER: message accepted from market: " + readLine);
                if (!validateCheckSum(readLine)) {
                    System.out.println("CheckSum validation is failed");
                    System.exit(0);
                }
                SocketSingleton.getInstance().getOutBroker().write( readLine + "\n" );
                SocketSingleton.getInstance().getOutBroker().flush();
                System.out.println( "ROUTER: message from market rerouted to broker" );
                String[] strSplitted = routingTable.toString().split( ", " );
                System.out.println( "Routing table:" );
                for ( int i = 0; i < strSplitted.length; i++ ) {
                    System.out.println( strSplitted[i] );
                }
                System.out.println("-------------------ITERATION ENDED-------------------");
                routingTable.clear();
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

    private static boolean validateCheckSum(String msg) {
        String checkSum = Pattern.compile( ".*([CHECKSUM=])").split( msg)[1];
        String msgForValidationCheckSum = msg.substring(0, msg.indexOf("|CHECKSUM"));
        msgForValidationCheckSum = createCheckSum(msgForValidationCheckSum);
        return checkSum.equalsIgnoreCase(msgForValidationCheckSum);
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
