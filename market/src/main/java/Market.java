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
    private static final Map<Integer, String> instruments = null;

    public static void main( String[] args ) {
//        while ( true ) {
            try ( Socket clientSocket = new Socket( "localhost", 5001 ) ) {
                br = new BufferedReader( new InputStreamReader( System.in ) );
                in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                out = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) );
//                System.out.println( "MARKET: enter string" );
                // msg from client typed to console
//                String readLine = br.readLine();
//                out.write( readLine + "\n" );
//                System.out.println( "MARKET: send message to server" );
//                out.flush();
                ////////////////////////
                // msg from server
                String readLine = in.readLine();
                System.out.println( "MARKET: message accepted: " + readLine + "\n" );
                // todo parse inReadLine
                // todo create buy/sell logic
//                if ( readLine.equalsIgnoreCase( "buy" ) ) {
//                    readLine = "Rejected";
//                    out.write( "MARKET: send message to server: " + readLine + "\n" );
//                    out.flush();
//                }
//                if ( readLine.equalsIgnoreCase( "sell" ) ) {
//                    readLine = "Executed";
//                    instruments.put( 1, "Pliers" );
//                    out.write( "MARKET: send message to server: " + readLine + "\n" );
//                    out.flush();
//                }
            } catch ( IOException ex ) {
                printException( ex );
            }
//        }
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
                        "|Instr=" + elem[1] +
                        "|Quant=" + elem[2] +
                        "|Market=" + elem[3] +
                        "|Price=" + elem[4] + "|";
        fixMsg += "CheckSum=" + createCheckSum( fixMsg );
        return fixMsg;
    }

    private static void printException( @NotNull Exception ex ) {
        System.out.println( "MARKET: client exception" );
        ex.printStackTrace();
    }
}
