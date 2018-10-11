import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

    private Socket socket = new Socket();
    private String ip;
    private String user;
    private InputStream input;
    private OutputStream output;
    private int secondsSinceIMAV;

    public Client() {
    }

    public Socket getSocket() {
        return socket;
    }

    public synchronized void setSocket(Socket socket) {
        this.socket = socket;
    }


    public synchronized void setIp(String ip) {
        this.ip = ip;
    }

    public String getUser() {
        return user;
    }

    public synchronized void setUser(String user) {
        this.user = user;
    }

    public InputStream getInput() {
        return input;
    }

    public void setInput(InputStream input) {
        this.input = input;
    }

    public OutputStream getOutput() {
        return output;
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public int getSecondsSinceIMAV() {
        return secondsSinceIMAV;
    }

    public void setSecondsSinceIMAV(int secondsSinceIMAV) {
        this.secondsSinceIMAV = secondsSinceIMAV;
    }
}