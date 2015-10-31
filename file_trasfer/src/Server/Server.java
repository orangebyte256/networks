package Server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by morsk on 10/29/2015.
 */
public class Server {
    ServerSocket socket = null;
    static final int size = 1000;
    Server(String port) throws IOException
    {
        socket = new ServerSocket(Integer.valueOf(port));
    }
    public void run() throws IOException {
        while(true)
        {
            Socket client = socket.accept();
            InputStream in = client.getInputStream();
            byte[] bytes = new byte[size];
            int count = 0;
            String name = Parser.readName(in);
            FileProcessor fileProcessor = new FileProcessor(name);
            while((count = in.read(bytes)) > 0)
            {
                fileProcessor.write(bytes, count);
            }
        }
    }
}
