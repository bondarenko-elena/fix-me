import com.sun.istack.internal.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class Market {

    private static BufferedReader br;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static String clientId;
    private static String option;
    private static final Map<Integer, String> instruments = null;

    public static void main( String[] args ) {
        while ( true ) {
            try ( Socket clientSocket = new Socket( "localhost", 5001 ) ) {
                br = new BufferedReader( new InputStreamReader( System.in ) );
                in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                out = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ));
                System.out.println( "MARKET: waiting message from server" );
                String readLine = in.readLine();
                System.out.println( "MARKET: message accepted: " + readLine );
                option = readLine.split( "|" )[2];
                clientId = readLine.split( "|" )[0];
                if ( option.equalsIgnoreCase( "buy" ) ) {
                    readLine = "Rejected";
                    System.out.println( "MARKET: send message to server: " + readLine + "\n" );
                    out.write( readLine );
                    out.flush();
                }
                if ( option.equalsIgnoreCase( "sell" ) ) {
                    readLine = "Executed";
                    instruments.put( 1, "Pliers" );
                    System.out.println( "MARKET: send message to server: " + readLine + "\n" );
                    out.write( createFixMessage( clientId + ";" + "Pliers;" + "1;" + "150") );
                    out.flush();
                }
            } catch ( IOException ex ) {
                printException( ex );
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
        }
        return returnHash;
    }

    private static String createFixMessage( String msgElem ) {
        String elem[] = msgElem.split( ";" );
        String fixMsg =
                "ID=" + elem[0] +
                        "|INSTR=" + elem[1] +
                        "|QUANT=" + elem[2] +
                        "|PRICE=" + elem[4] + "|";
        fixMsg += "|CHECKSUM=" + createCheckSum( fixMsg );
        return fixMsg;
    }

    private static void printException( @NotNull Exception ex ) {
        System.out.println( "MARKET: client exception" );
        ex.printStackTrace();
    }
}
