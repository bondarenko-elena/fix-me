import com.sun.istack.internal.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Broker {
    private static BufferedReader br;

    public static void main( String[] args ) {

        while ( true ) {
            try (
                    Socket clientSocket = new Socket( "localhost", 5000 );
                    BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                    BufferedWriter out = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) )
            ) {
                br = new BufferedReader( new InputStreamReader( System.in ) );
                // wait for clientId from Router
                String clientId = in.readLine();
                System.out.println( "BROKER: available options: BUY, SELL" );
                System.out.println( "BROKER chose option:" );
                String readLine = br.readLine();
                while ( !( readLine.equalsIgnoreCase( "buy" ) || readLine.equalsIgnoreCase(
                        "sell" ) ) ) {
                    System.out.println( "BROKER: waiting message in console" );
                    System.out.println( "BROKER: available options: BUY, SELL" );
                    readLine = br.readLine();
                }
                int port = clientSocket.getLocalPort();
                String option = readLine;
                System.out.println( "BROKER: send message to server: " + readLine );
                out.write( createFixMessage( clientId + ";" + port + ";" + option ) + "\n" );
                out.flush();
                System.out.println( "BROKER: waiting for message from server: " );
                readLine = in.readLine();
                //todo parse input if needed
                System.out.println( "BROKER: accepted message from server: " + readLine );
                System.out.println( "-------------------ITERATION ENDED-------------------" );

            } catch ( IOException ex ) {
                try {
                    br.close();
                } catch ( IOException ex2 ) {
                    printException( ex2 );
                }
                System.out.println( "BROKER: server is down" );
                break;
            }
        }
    }

    private static String createCheckSum( @NotNull String clientId ) {
        String returnHash = "";
        try {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            md.update( clientId.getBytes() );
            returnHash = DatatypeConverter.printHexBinary( md.digest() ).toLowerCase();
        } catch ( NoSuchAlgorithmException ex ) {
            printException( ex );
        }
        if ( ( returnHash.length() < 1 ) ) {
            System.out.println( "ROUTER: unable to create check sum" );
            System.exit( 0 );
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

    private static void printException( @NotNull Exception ex ) {
        System.out.println( "BROKER: client exception" );
        ex.printStackTrace();
    }
}
