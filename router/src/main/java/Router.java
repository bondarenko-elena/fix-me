import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class Router extends Thread {
    private static final int brokerPort = 5000;
    private static final int marketPort = 5001;
    private static ServerSocket serverSocket;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static String clientId;
    private static String checkSum;

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
            try {
                if (clientSocket.getLocalPort() == 5000) {
                    System.out.println("ROUTER: broker connected");
                } else {
                    System.out.println("ROUTER: market connected");
                }
                in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                out = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) );
                // read msg from client
                String readLine = in.readLine();
                System.out.println( readLine );
                out.write( "ROUTER: message accepted" );
                ////////////////////////
                System.out.println("ROUTER: port " + clientSocket.getLocalPort());
                clientId = ( clientSocket.getLocalPort() == 5000
                        ? "0"
                        : "1" ) + String.valueOf( Instant.now()
                                                         .toEpochMilli() )
                                        .substring( 11 );
                checkSum = createCheckSum( clientId );
                System.out.println( "ROUTER: clientId " + clientId );
                System.out.println( "ROUTER: check sum " + checkSum );
                ////////////////////////
                out.flush();
            } catch ( IOException ex ) {
                printException( ex );
            }
        } catch ( IOException ex ) {
            printException( ex );
        }
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

    protected static void printException( @NotNull Exception ex ) {
        System.out.println( "ROUTER: server exception" );
        ex.printStackTrace();
    }
}
