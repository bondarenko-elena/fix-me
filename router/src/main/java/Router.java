import java.io.IOException;
import java.net.ServerSocket;

public class Router {
    private static final int brokerPort = 5000;
    private static final int marketPort = 5001;

    public static void main( String[] args ) {
        while ( true ) {
            try ( ServerSocket serverSocket = new ServerSocket( brokerPort ) ) {
                startSocketManager( serverSocket, "Router echo: broker connected" );
            } catch ( IOException ex ) {
                printException( ex );
            }
            try ( ServerSocket serverSocket = new ServerSocket( marketPort ) ) {
                startSocketManager( serverSocket, "Router echo: market connected" );
            } catch ( IOException ex ) {
                printException( ex );
            }
        }
    }

    private static void startSocketManager( ServerSocket serverSocket, String msg ) throws IOException {
        new SocketManager( serverSocket.accept() ).start();
        System.out.println( "\u001b[32m" + msg );
    }

    protected static void printException( Exception ex ) {
        System.out.println( "\u001b[31m" + "Router server exception" );
        ex.printStackTrace();
    }
}
