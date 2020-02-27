import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@Setter
public class SocketManager extends Thread {
    private Socket socket;
//    private static String clientId;
    private static String clientIdBroker;
    private static String clientIdMarket;
    private int port;
    private static Map<String, Socket> routingTable = new HashMap<>();

    SocketManager( Socket socket ) {
        this.socket = socket;
        if ( socket.getLocalPort() == 5000 ) {
            clientIdBroker = "0" + String.valueOf( Instant.now().toEpochMilli() ).substring( 8 );
        } else {
            clientIdMarket = "1" + String.valueOf( Instant.now().toEpochMilli() ).substring( 8 );
        }
//        clientId = ( socket.getLocalPort() == 5000 ? "0" : "1" ) + String.valueOf( Instant.now().toEpochMilli() )
//                                                                         .substring( 8 );
        try {
            if ( socket.getLocalPort() == 5000 ) {
                routingTable.put( "BROKER -> " + clientIdBroker, socket );
                System.out.println( "ROUTER: broker connected" );
                SocketSingleton.getInstance()
                               .setInBroker( new BufferedReader( new InputStreamReader( socket.getInputStream() ) ) );
                SocketSingleton.getInstance()
                               .setOutBroker( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) ) );
            }
            if ( socket.getLocalPort() == 5001 ) {
                routingTable.put( "MARKET -> " + clientIdMarket, socket );
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
        portThread();
    }

    private void portThread() {
        String readLine;
        try {
            if ( socket.getLocalPort() == 5000 ) {
                // send clientId to Broker
                SocketSingleton.getInstance().getOutBroker().write( clientIdBroker + "\n" );
                SocketSingleton.getInstance().getOutBroker().flush();
                // wait msg from broker
                readLine = SocketSingleton.getInstance().getInBroker().readLine();
                System.out.println( "ROUTER: message accepted from Broker: " + readLine );
                // validate checkSum
                if ( validateCheckSum( readLine ) == false ) {
                    System.out.println( "CheckSum validation is failed" );
                    System.exit( 0 );
                }
                // reroute msg to market
                //todo parse readLine
                SocketSingleton.getInstance().getOutMarket().write( readLine + "\n" );
                SocketSingleton.getInstance().getOutMarket().flush();
                System.out.println( "ROUTER: message rerouted to Market: " + readLine );
            } else {
                // send clientId to Market
                SocketSingleton.getInstance().getOutMarket().write( clientIdMarket + "\n" );
                SocketSingleton.getInstance().getOutMarket().flush();
                // wait msg from market
                readLine = SocketSingleton.getInstance().getInMarket().readLine();
                System.out.println( "ROUTER: message accepted from market: " + readLine );
                // validate checkSum
                if ( validateCheckSum( readLine ) == false ) {
                    System.out.println( "CheckSum validation is failed" );
                    System.exit( 0 );
                }
                // reroute msg to broker
                SocketSingleton.getInstance().getOutBroker().write( readLine + "\n" );
                SocketSingleton.getInstance().getOutBroker().flush();
                System.out.println( "ROUTER: message from market rerouted to broker" );
                String[] strSplitted = routingTable.toString().split( ", " );
                System.out.println( "Routing table:" );
                for ( int i = 0; i < strSplitted.length; i++ ) {
                    System.out.println( strSplitted[i] );
                }
                System.out.println( "-------------------ITERATION ENDED-------------------" );
            }
        } catch ( IOException ex ) {
//            Router.printException( ex );
            if ( socket.getLocalPort() == 5000 ) {
                System.out.println( "ROUTER: broker is down" );
            } else {
                System.out.println( "ROUTER: market is down" );
            }

            System.exit( 0 );

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

    private static boolean validateCheckSum( String msg ) {
        String checkSum = Pattern.compile( ".*([CHECKSUM=])" ).split( msg )[1];
        String msgForValidationCheckSum = msg.substring( 0, msg.indexOf( "|CHECKSUM" ) );
        msgForValidationCheckSum = createCheckSum( msgForValidationCheckSum );
        if ( !checkSum.equalsIgnoreCase( msgForValidationCheckSum ) ) {
            return false;
        }
        return true;
    }
}
