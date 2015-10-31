package Client;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.FileSystems;

/**
 * Created by morsk on 10/28/2015.
 */


public class Client {
    Socket socket;
    Client(String host, String port) throws IOException
    {
        socket = new Socket(host, Integer.valueOf(port));
    }
    public Socket getSocket()
    {
        return socket;
    }
    public void close() throws IOException
    {
        socket.close();
    }
    static void main(String args[])
    {
        try {
            Client client = new Client(args[0], args[1]);
            FileProcessor fileProcessor = new FileProcessor(FileSystems.getDefault().getPath(args[2]));
            Sender.send(client.getSocket(), fileProcessor.getName(), fileProcessor.getData());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
