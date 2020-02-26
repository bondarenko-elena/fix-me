import com.sun.istack.internal.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Market {

    private static final Map<Integer, String> instruments = new Map<Integer, String>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey( Object key ) {
            return false;
        }

        @Override
        public boolean containsValue( Object value ) {
            return false;
        }

        @Override
        public String get( Object key ) {
            return null;
        }

        @Override
        public String put( Integer key, String value ) {
            return null;
        }

        @Override
        public String remove( Object key ) {
            return null;
        }

        @Override
        public void putAll( Map<? extends Integer, ? extends String> m ) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set<Integer> keySet() {
            return null;
        }

        @Override
        public Collection<String> values() {
            return null;
        }

        @Override
        public Set<Entry<Integer, String>> entrySet() {
            return null;
        }
    };

    public static void main( String[] args ) {
        try ( Socket clientSocket = new Socket( "localhost", 5001 ) ) {
            while ( true ) {
                BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                BufferedWriter out = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) );
                System.out.println( "MARKET: waiting message from server" );
                String readLine = in.readLine();
                System.out.println( "MARKET: message accepted: " + readLine );
                String clientId = readLine.split( "\\|" )[0].split( "=" )[1];
                String port = readLine.split( "\\|" )[1].split( "=" )[1];
                String option = readLine.split( "\\|" )[2].split( "=" )[1];
                if ( option.equalsIgnoreCase( "buy" ) ) {
                    readLine = "Rejected";
                    System.out.println( "MARKET: send message to server: " + readLine );
                    out.write( createFixMessage( clientId + ";" + port + ";" + "none;" + "0;" + "0" ) + "\n" );
                    out.flush();
                }
                if ( option.equalsIgnoreCase( "sell" ) ) {
                    readLine = "Executed";
                    instruments.put( 1, "Pliers" );
                    System.out.println( "MARKET: send message to server: " + readLine + "\n" );
                    out.write( createFixMessage( clientId + ";" + port + ";" + "Pliers;" + "1;" + "150" ) + "\n" );
                    out.flush();
                }
                System.out.println( "-------------------ITERATION ENDED-------------------" );
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
            printException( ex );
        }
        if ( ( returnHash.length() < 1 ) ) {
            System.out.println( "ROUTER: unable to create check sum" );
        }
        return returnHash;
    }

    private static String createFixMessage( String msgElem ) {
        String[] elem = msgElem.split( ";" );
        String fixMsg =
                "ID=" + elem[0] +
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
