import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Market {

    public static void main(String[] args) {
        runMarket();
    }

    public static void runMarket() {
        try ( Socket socket = new Socket( "localhost", 5001 ) ) {
            BufferedReader echoes = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintWriter pw = new PrintWriter( socket.getOutputStream(), true );

            Scanner scanner = new Scanner( System.in );
            String str;
            String response;

            do {
                System.out.println( "Market: enter string to be echoed: " );
                str = scanner.nextLine();

                pw.println( str );
                if ( !str.equals( "exit" ) ) {
                    response = echoes.readLine();
                    System.out.println( response );
                }
            } while ( !str.equals( "exit" ) );

        } catch ( IOException ex ) {
            printException( ex );
        }
    }

    protected static void printException( @NotNull Exception ex ) {
        System.out.println( "Market Client exception" );
        ex.printStackTrace();
    }

}
