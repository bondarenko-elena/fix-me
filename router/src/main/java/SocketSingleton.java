import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;

@Getter
public class SocketSingleton {

    private BufferedReader inBroker;
    private BufferedWriter outBroker;
    private BufferedReader inMarket;
    private BufferedWriter outMarket;
    private static SocketSingleton instance;

    public static SocketSingleton getInstance() {
        if ( instance == null ) {
            instance = new SocketSingleton();
        }
        return instance;
    }

    public void setInBroker( Socket brokerSocket ) throws IOException {
        this.inBroker = getReaderFromSocket( brokerSocket );
    }

    public void setInMarket( Socket marketSocket ) throws IOException {
        this.inMarket = getReaderFromSocket( marketSocket );
    }

    public void setOutBroker( Socket brokerSocket ) throws IOException {
        this.outBroker = getWriterFromSocket( brokerSocket );
    }

    public void setOutMarket( Socket marketSocket ) throws IOException {
        this.outMarket = getWriterFromSocket( marketSocket );
    }

    @NotNull
    private BufferedReader getReaderFromSocket( Socket socket ) throws IOException {
        return new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
    }

    @NotNull
    private BufferedWriter getWriterFromSocket( Socket brokerSocket ) throws IOException {
        return new BufferedWriter( new OutputStreamWriter( brokerSocket.getOutputStream() ) );
    }
}
