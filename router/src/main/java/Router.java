import org.jetbrains.annotations.NotNull;


import java.io.*;
import java.net.ServerSocket;

public class Router {
    private static final int brokerPort = 5000;
    private static final int marketPort = 5001;
    private static SocketManager brokerManager;
    private static SocketManager marketManager;

    public static void main( String[] args ) {
        System.out.println( "ROUTER: turned on" );
        try (
                ServerSocket brokerSocket = new ServerSocket( brokerPort );
                ServerSocket marketSocket = new ServerSocket( marketPort )
        ) {
            while ( true ) {
                brokerManager = new SocketManager( brokerSocket.accept() );
                marketManager = new SocketManager( marketSocket.accept() );
                brokerManager.start();
                marketManager.start();
            }
        } catch ( IOException ex ) {
            printException( ex );
        }
    }

    static void printException( @NotNull Exception ex ) {
        System.out.println( "ROUTER: server exception" );
        ex.printStackTrace();
    }
}
