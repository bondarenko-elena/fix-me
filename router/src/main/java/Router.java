import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Router extends Thread {
    private static final int brokerPort = 5000;
    private static final int marketPort = 5001;
    private static Socket clientSocket;
    private static BufferedReader in;
    private static BufferedWriter out;

    public static void main( String[] args ) {
        while ( true ) {
            System.out.println( "ROUTER: turned on" );
            try ( ServerSocket serverSocket = new ServerSocket( brokerPort ) ) {
                clientSocket = serverSocket.accept();
                try {
                    in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
                    out = new BufferedWriter( new OutputStreamWriter( clientSocket.getOutputStream() ) );
                    // сообщение от клиента
                    String readLine = in.readLine();
                    System.out.println(readLine);
                    out.write( "ROUTER: message accepted" );
                    out.flush();
                } catch ( IOException ex ) {
                    printException( ex );
                }
            } catch ( IOException ex ) {
                printException( ex );
            }
        }
    }

    public static void portThread(Socket socket) {

    }

    protected static void printException( @NotNull Exception ex ) {
        System.out.println( "ROUTER: server exception" );
        ex.printStackTrace();
    }
}
