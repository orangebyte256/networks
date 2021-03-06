package server;

import common.Message;
import common.Parameters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

/**
 * Created by morsk on 10/31/2015.
 */


enum State
{
    FREE, BUSY, CALCULATED
}

class Piece
{
    State state;
    double TTL;
    Piece(State state, double ttl)
    {
        this.state = state;
        this.TTL = ttl;
    }
    void timeLeft(double time)
    {
        TTL -= time;
        if(TTL < 0)
        {
            state = State.FREE;
        }
    }
    public void busy()
    {
        state = State.BUSY;
    }
    public void calculated()
    {
        state = State.CALCULATED;
    }
    public boolean isFree()
    {
        return state == State.FREE;
    }
}

public class Server {
    ByteBuffer hash = null;
    ByteBuffer bufferNum = ByteBuffer.allocate(Parameters.MAX_SIZE);
    int port;
    private Selector selector = Selector.open();
    static private final int MAX_COUNT = 100;
    static private final int PIECE_SIZE = 100;
    static private final double TTL = 3.0;
    ArrayList<Piece> pieces;
    ServerSocketChannel server = null;
    Server(String hash, String port) throws IOException {
        this.port = Integer.parseInt(port);
        this.hash = ByteBuffer.allocate(Parameters.HASH_SIZE);
        this.hash.position(0);
        System.err.print(hash.getBytes().length);
        this.hash.put(hash.getBytes());
        this.hash.position(0);
        pieces = new ArrayList<>();
        for(int i = 0; i < MAX_COUNT; i++)
        {
            pieces.add(new Piece(State.FREE, TTL));
        }
    }

    void work() throws IOException
    {
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(port));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        Map<SocketChannel, ByteBuffer> channels = new HashMap<>();
        SocketChannel findedChannel = null;
        ByteBuffer findedBuffer = ByteBuffer.allocate(Parameters.MAX_SIZE);
        while(true) {
            int readyChannels = selector.select();
            if(readyChannels == 0) continue;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if(key.isAcceptable()) {
                    SocketChannel s = server.accept();
                    s.configureBlocking(false);
                    s.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    if(channel == findedChannel)
                    {
                        if(channel.read(findedBuffer) == -1)
                        {
                            System.out.println(Integer.parseInt(findedBuffer.toString()));
                        }
                        break;
                    }
                    if(channels.containsKey(channel))
                    {
                        if(channel.read(channels.get(channel)) == -1)
                        {
                            pieces.get(Integer.parseInt(findedBuffer.toString())).calculated();
                        }
                        break;
                    }
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1);
                    channel.read(byteBuffer);
                    switch (Message.values()[byteBuffer.get(0)])
                    {
                        case GET_HASH:
                            channel.write(hash);
                            hash.position(0);
                            break;
                        case GET_WORK:
                            bufferNum.clear();
                            bufferNum.position(0);
                            for(int i = 0; i < MAX_COUNT; i++)
                            {
                                if(pieces.get(i).isFree())
                                {
                                    Integer tmp = i;
                                    bufferNum.put(tmp.toString().getBytes());
                                    bufferNum.position(0);
                                    pieces.get(i).busy();
                                }
                            }
                            channel.write(bufferNum);
                            break;
                        case CALCULATED:
                            channels.put(channel, ByteBuffer.allocate(Parameters.MAX_SIZE));
                            break;
                        case FINDED:
                            findedChannel = channel;
                            break;
                    }
                }
                keyIterator.remove();
            }
        }
    }

/*    static public void main(String[] args)
    {
        try {
            Server server = new Server("7fc56270e7a70fa81a5935b72eacbe29", "10002");
            server.work();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    */
}
