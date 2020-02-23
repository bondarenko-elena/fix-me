import com.sun.istack.internal.NotNull;

import java.io.*;
import java.net.Socket;

public class Broker {

    private static BufferedReader br;
    private static BufferedReader in;
    private static BufferedWriter out;

    public static void main (String[] args) {
        try (Socket clientSocket = new Socket("localhost", 5000)) {
            br = new BufferedReader( new InputStreamReader( System.in ) );
            in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
            out = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) );
            System.out.println("BROKER: enter string");
            // msg from client in console
            String readLine = br.readLine();
            out.write(readLine + "\n");
            System.out.println("BROKER: send message to server");
            out.flush();
            // msg from server
            readLine = in.readLine();
            System.out.println(readLine);
        }
        catch ( IOException ex ) {
            printException( ex );
        }
    }

    private static void printException( @NotNull Exception ex ) {
        System.out.println( "BROKER: client exception" );
        ex.printStackTrace();
    }
}
