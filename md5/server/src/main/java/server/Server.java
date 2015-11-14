package server;

import common.Message;
import common.Parameters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    double time = 0;
    Date oldTime = new Date();
    Piece(State state, double ttl)
    {
        this.state = state;
        this.TTL = ttl;
    }
    public void busy()
    {
        time = TTL;
        oldTime = new Date();
        state = State.BUSY;
    }
    public void calculated()
    {
        state = State.CALCULATED;
    }
    public boolean isFree()
    {
        if(state == State.CALCULATED)
            return false;
        time -= (double)(new Date().getTime() - oldTime.getTime()) / 1000.0;
        if(time <= 0)
            state = State.FREE;
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
    Server(String hash, String port) throws IOException, NoSuchAlgorithmException {
        this.port = Integer.parseInt(port);
        this.hash = ByteBuffer.allocate(Parameters.HASH_SIZE);
        this.hash.position(0);
        MessageDigest md = MessageDigest.getInstance("MD5");
        System.err.print("Server start");
        this.hash.put(md.digest(hash.getBytes()));
        this.hash.position(0);
        pieces = new ArrayList<>();
        for(int i = 0; i < MAX_COUNT; i++)
        {
            pieces.add(new Piece(State.FREE, TTL));
        }
    }
    String bytebufferToString(ByteBuffer buffer)
    {
        buffer.flip();
        byte[] array = new byte[buffer.remaining()];
        buffer.get(array);
        buffer.flip();
        return new String(array);
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
                        int count = 0;
                        try {
                            if ((count = channel.read(findedBuffer)) == -1)
                            {
                                System.out.println(bytebufferToString(findedBuffer));
                                channel.close();
                            }
                            keyIterator.remove();
                            continue;
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    if(channels.containsKey(channel))
                    {
                        if(channel.read(channels.get(channel)) == -1)
                        {
                            String s = bytebufferToString(channels.get(channel));
                            pieces.get(Integer.parseInt(s)).calculated();
                            System.err.println(s);
                            channel.close();
                        }
                        keyIterator.remove();
                        continue;
                    }
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1);
                    if(channel.read(byteBuffer) == -1)
                    {
                        channel.close();
                        continue;
                    }
                    byteBuffer.flip();
                    switch (Message.values()[byteBuffer.get()])
                    {
                        case GET_HASH:
                            channel.write(hash);
                            hash.position(0);
                            break;
                        case GET_WORK:
                            bufferNum.clear();
                            bufferNum.position(0);
                            boolean isFind = false;
                            for(int i = 0; i < MAX_COUNT; i++)
                            {
                                if(pieces.get(i).isFree())
                                {
                                    Integer tmp = i;
                                    bufferNum.put(tmp.toString().getBytes());
                                    bufferNum.flip();
                                    pieces.get(i).busy();
                                    isFind = true;
                                    break;
                                }
                            }
                            if(!isFind)
                            {
                                Integer tmp = -1;
                                bufferNum.put(tmp.toString().getBytes());
                                bufferNum.flip();
                            }
                            channel.write(bufferNum);
                            channel.close();
                            break;
                        case CALCULATED:
                            channels.put(channel, ByteBuffer.allocate(Parameters.MAX_SIZE));
                            break;
                        case FINDED:
                            findedBuffer.clear();
                            findedChannel = channel;
                            break;
                    }
                }
                keyIterator.remove();
            }
        }
    }

    static public void main(String[] args)
    {
        try {
            Server server = new Server(args[0], args[1]);
            server.work();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
}
