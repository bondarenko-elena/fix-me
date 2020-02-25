import com.sun.istack.internal.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Broker {

    private static BufferedReader br;
    private static BufferedReader in;
    private static BufferedWriter out;

    public static void main( String[] args ) {
        while ( true ) {
            try ( Socket clientSocket = new Socket( "localhost", 5000 ) ) {
                br = new BufferedReader( new InputStreamReader( System.in ) );
                in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                out = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) );
                System.out.println( "BROKER: available options: BUY, SELL" );
                System.out.println( "BROKER chose option:" );
                String readLine = br.readLine();
                while ( !( readLine.equalsIgnoreCase( "buy" ) || readLine.equalsIgnoreCase( "sell" ) ) ) {
                    // msg from client typed to console
                    System.out.println( "BROKER: available options: BUY, SELL" );
                    readLine = br.readLine();
                }
                System.out.println( "BROKER: send message to server: " + readLine );
                out.write( readLine + "\n" );
                out.flush();
                // msg from server (get FIX msg)
//                System.out.println( "BROKER: get message from server: " );
//                readLine = in.readLine();
//                System.out.println( readLine );
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
                        "|Instr=" + elem[1] +
                        "|Quant=" + elem[2] +
                        "|Market=" + elem[3] +
                        "|Price=" + elem[4] + "|";
        fixMsg += "|CHECKSUM=" + createCheckSum( fixMsg );
        return fixMsg;
    }

    private static void printException( @NotNull Exception ex ) {
        System.out.println( "BROKER: client exception" );
        ex.printStackTrace();
    }
}
