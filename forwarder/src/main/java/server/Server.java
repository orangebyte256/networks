package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by morsk on 10/31/2015.
 */

class Pipe
{
    static private final int BUFFER_SIZE = 100000000;
    private Selector selector = null;
    private Map<SocketChannel, ByteBuffer> buffers = new HashMap<>();
    private Map<SocketChannel, SocketChannel> oppositeChannel = new HashMap<>();

    private void processChannel(SocketChannel socketChannel, Selector selector) throws IOException {
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }
    Pipe(SocketChannel first, SocketChannel second, Selector selector) throws IOException {
        processChannel(first, selector);
        processChannel(second, selector);
        this.selector = selector;
        buffers.put(first, ByteBuffer.allocate(BUFFER_SIZE));
        buffers.put(second, ByteBuffer.allocate(BUFFER_SIZE));
        oppositeChannel.put(first, second);
        oppositeChannel.put(second, first);
    }
    void read(SocketChannel channel) throws IOException {
        ByteBuffer buffer = buffers.get(channel);
        channel.read(buffer);
        SocketChannel opposite = oppositeChannel.get(channel);
        buffer.flip();
        opposite.write(buffer);
        if(buffer.hasRemaining())
            channel.register(selector, SelectionKey.OP_WRITE);
        buffer.flip();
    }
    void write(SocketChannel channel, SelectionKey key) throws IOException {
        SocketChannel op = oppositeChannel.get(channel);
        ByteBuffer buffer = buffers.get(op);
        buffer.flip();
        channel.write(buffer);
        if(!buffer.hasRemaining())
        {
            key.cancel();
        }
        buffer.flip();
    }
    boolean isChannelBelong(SocketChannel channel)
    {
        return oppositeChannel.keySet().contains(channel);
    }
}

class PipesManager
{
    ArrayList<Pipe> pipes = new ArrayList<>();
    void add(Pipe pipe)
    {
        pipes.add(pipe);
    }
    Pipe get(SocketChannel socketChannel)
    {
        for(Pipe pipe : pipes)
        {
            if(pipe.isChannelBelong(socketChannel))
                return pipe;
        }
        return null;
    }
}

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
                if(key.isAcceptable()) {
                    SocketChannel s = proxy.accept();
                    SocketChannel client = SocketChannel.open();
                    client.connect(new InetSocketAddress(host, rport));
                    pipesManager.add(new Pipe(s, client, selector));
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    Pipe pipe = pipesManager.get(channel);
                    pipe.read(channel);
                }
                else if (key.isWritable())
                {
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
            Server server = new Server("2001", "yandex.ru", "80");
            server.work();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
}
