package main.java.server;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Server {
    PipesManager pipesManager = new PipesManager();
    private Selector selector = Selector.open();
    ServerSocketChannel proxy = null;
    SocketChannel client = null;
    int lport = 0;
    int rport = 0;
    String host;
    Server(String lport, String host, String rport) throws IOException, NoSuchAlgorithmException
    {
        this.lport = Integer.parseInt(lport);
        this.rport = Integer.parseInt(rport);
        this.host = host;

        proxy = ServerSocketChannel.open();
        proxy.socket().bind(new InetSocketAddress(this.lport));
        proxy.configureBlocking(false);
        proxy.register(selector, SelectionKey.OP_ACCEPT);

    }
    void work() throws IOException
    {
        while(true) {
            int readyChannels = selector.select();
            if(readyChannels == 0) continue;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if(!key.isValid())
                {
                    continue;
                }
                if(key.isAcceptable()) {
                    SocketChannel s = proxy.accept();
                    pipesManager.add(new Pipe(s, selector));
                } else if (key.isReadable()) {
                    System.out.println("Read event");
                    SocketChannel channel = (SocketChannel) key.channel();
                    Pipe pipe = pipesManager.get(channel);
                    pipe.read(channel, key);
                }
                else if (key.isWritable())
                {
                    System.out.println("Write event");
                    SocketChannel channel = (SocketChannel) key.channel();
                    Pipe pipe = pipesManager.get(channel);
                    pipe.write(channel, key);
                }
                keyIterator.remove();
            }
        }
    }

    static public void main(String[] args)
    {
        try {
            Server server = new Server("8080", "yandex.ru", "80");
            server.work();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
}
