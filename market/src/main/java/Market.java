import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Market {
    public static final String ANSI_BLUE = "\u001B[34m";
    public static void runMarket() {
        try ( Socket socket = new Socket( "localhost", 5001)) {
            BufferedReader echoes = new BufferedReader(
                    new InputStreamReader( socket.getInputStream()));
            PrintWriter stringToEcho = new PrintWriter( socket.getOutputStream(), true);

            Scanner scanner = new Scanner( System.in);
            String echoString;
            String response;

            do {
                System.out.println(ANSI_BLUE + "Enter string to be echoed: ");
                echoString = scanner.nextLine();

                stringToEcho.println(echoString);
                if (!echoString.equals("exit")) {
                    response = echoes.readLine();
                    System.out.println(response);
                }
            } while (!echoString.equals("exit"));

        } catch ( IOException e) {
            System.out.println(ANSI_BLUE + "Market Client Error: " + e.getMessage());

        }
    }
}
