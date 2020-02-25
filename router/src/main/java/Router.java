import org.jetbrains.annotations.NotNull;


import java.io.*;
import java.net.ServerSocket;

public class Router {
    private static final int brokerPort = 5000;
    private static final int marketPort = 5001;


    public static void main( String[] args ) {
        System.out.println( "ROUTER: turned on" );
        while ( true ) {
            try ( ServerSocket serverSocket = new ServerSocket( brokerPort ) ) {
                new SocketManager( serverSocket.accept() ).start();
//                System.out.println( "ROUTER: broker is here" );
            } catch ( IOException ex ) {
                printException( ex );
            }
            try ( ServerSocket serverSocket = new ServerSocket( marketPort ) ) {
                new SocketManager( serverSocket.accept() ).start();
//                System.out.println( "ROUTER: market is here" );
            } catch ( IOException ex ) {
                printException( ex );
            }
        }
    }

    static void printException( @NotNull Exception ex ) {
        System.out.println( "ROUTER: server exception" );
        ex.printStackTrace();
    }
}
