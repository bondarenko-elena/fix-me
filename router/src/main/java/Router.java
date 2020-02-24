import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;

public class Router extends Thread {
    private static final int brokerPort = 5000;
    private static final int marketPort = 5001;
    private static ServerSocket serverSocket;
    private static BufferedReader inBroker;
    private static BufferedWriter outBroker;
    private static BufferedReader inMarket;
    private static BufferedWriter outMarket;
    private static String clientId;
    private static int port;
    private static ArrayList<String> routingTable = new ArrayList<>();
    private static String readLine = "";
    public static LinkedList<Socket> serverList = new LinkedList<>();

    public static void main( String[] args ) {
        System.out.println( "ROUTER: turned on" );
        while ( true ) {
            try {
                serverSocket = new ServerSocket( brokerPort );
                portThread( serverSocket );
            } catch ( IOException ex ) {
                printException( ex );
            }
            try {
                serverSocket = new ServerSocket( marketPort );
                portThread( serverSocket );
            } catch ( IOException ex ) {
                printException( ex );
            }
        }
    }

    @Override
    public void run() {
        portThread( serverSocket );
    }

    public static void portThread( ServerSocket serverSocket ) {
        try {
            Socket clientSocket = serverSocket.accept();
            if ( clientSocket.getLocalPort() == 5000 ) {
                System.out.println( "ROUTER: broker connected" );
                try {
                    inBroker = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                    outBroker = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) );
                    // read msg from client (broker/market)
                    readLine = inBroker.readLine();
                    System.out.println( "ROUTER: message accepted: " + readLine + "\n" );
                    port = clientSocket.getLocalPort();
                    clientId = ( clientSocket.getLocalPort() == 5000
                            ? "0"
                            : "1" ) + String.valueOf( Instant.now()
                                                             .toEpochMilli() )
                                            .substring( 8 );
//                    outBroker.write( createFixMessage( clientId + ";" + port + ";" + readLine + "\n" ) );
//                    outBroker.flush();
                } catch ( IOException ex ) {
                    printException( ex );
                }
            }
            if (clientSocket.getLocalPort() == 5001){
                System.out.println( "ROUTER: market connected" );
                inMarket = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                outMarket = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) );
                System.out.println( "ROUTER: message reroute to market " );
                outMarket.write( createFixMessage( clientId + ";" + port + ";" + readLine + "\n" ) );
                outMarket.flush();
            }

        } catch ( IOException ex ) {
            printException( ex );
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
        routingTable.add( fixMsg );
        return fixMsg;
    }

    protected static void printException( @NotNull Exception ex ) {
        System.out.println( "ROUTER: server exception" );
        ex.printStackTrace();
    }
}
