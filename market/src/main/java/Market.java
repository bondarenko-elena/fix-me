import com.sun.istack.internal.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Market {

    private static final Map<Integer, String> instruments = new HashMap<>();

    public static void main( String[] args ) {
        while ( true ) {
            try (
                    Socket clientSocket = new Socket( "localhost", 5001 );
                    BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                    BufferedWriter out = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) )
            ) {
                String clientId = in.readLine();
                System.out.println( "MARKET: waiting message from server" );
                String readLine = in.readLine();
                System.out.println( "MARKET: message accepted: " + readLine );
                String[] data = Pattern.compile( "\\|*\\w+=" ).split( readLine );
                String port = data[2];
                String option = data[3];
                if ( option.equalsIgnoreCase( "buy" ) ) {
                    readLine = "Rejected";
                    out.write( createFixMessage( clientId + ";" + port + ";" + "none;" + "0;" + "0" ) + "\n" );
                    out.flush();
                }
                if ( option.equalsIgnoreCase( "sell" ) ) {
                    readLine = "Executed";
                    instruments.put( 1, "Pliers" );
                    out.write( createFixMessage( clientId + ";" + port + ";" + "Pliers;" + "1;" + "150" ) + "\n" );
                    out.flush();
                }
                System.out.println( "MARKET: send message to server: " + readLine );
                System.out.println( "-------------------ITERATION ENDED-------------------" );
            } catch ( IOException ex ) {
                System.out.println("MARKET: server is down");
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
        if ( returnHash.length() < 1 ) {
            System.out.println( "ROUTER: unable to create check sum" );
        }
        return returnHash;
    }

    private static String createFixMessage( String msgElem ) {
        String[] elem = msgElem.split( ";" );
        String fixMsg = "ID=" + elem[0] +
                "|PORT=" + elem[1] +
                "|INSTR=" + elem[2] +
                "|QUANT=" + elem[3] +
                "|PRICE=" + elem[4] + "|";
        fixMsg += "|CHECKSUM=" + createCheckSum( fixMsg );
        return fixMsg;
    }

    private static void printException( @NotNull Exception ex ) {
        System.out.println( "MARKET: client exception" );
        ex.printStackTrace();
    }
}
