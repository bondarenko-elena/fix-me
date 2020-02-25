import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.BufferedWriter;

@Getter
@Setter
public class SocketSingleton {

    private  BufferedReader inBroker;
    private  BufferedWriter outBroker;
    private  BufferedReader inMarket;
    private  BufferedWriter outMarket;
    private static SocketSingleton instance;

    public static SocketSingleton getInstance() {
        if ( instance == null ) {
            instance = new SocketSingleton();
        }
        return instance;
    }
}
