import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

//todo refactor
public class Broker {

    public static void runBroker() {
        try ( Socket socket = new Socket( "localhost", 5000 ) ) {
            BufferedReader br = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintWriter pw = new PrintWriter( socket.getOutputStream(), true );

            Scanner scanner = new Scanner( System.in );
            String str;
            String response;

            do {
                System.out.println( "Brocker echo: enter string to be echoed:" );
                str = scanner.nextLine();

                pw.println( str );
                if ( !str.equals( "exit" ) ) {
                    response = br.readLine();
                    System.out.println( response );
                }
            } while ( !str.equals( "exit" ) );

        } catch ( IOException ex ) {
            printException( ex );
        }
    }

    private static void printException( @NotNull Exception ex ) {
        System.out.println( "\u001b[31m" + "Broker client exception" );
        ex.printStackTrace();
    }
}
