package server;

import common.Message;
import javafx.util.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by morsk on 10/31/2015.
 */


enum State
{
    FREE, BUSY, CALCULATED
}

class Piece
{
    int start;
    int end;
    State state;
    double TTL;
    Piece(int start, int end, State state,double ttl)
    {
        this.start = start;
        this.end = end;
        this.state = state;
        this.TTL = ttl;
    }
}

public class Server {
    String hash = null;
    int port;
    static private final int MAX_LENGTH = 10000;
    static private final int PIECE_SIZE = 100;
    static private final double TTL = 3.0;
    ServerSocket serverSocket = null;
    Server(String hash, String port) throws IOException {
        this.hash = hash;
        this.port = Integer.parseInt(port);
        serverSocket = new ServerSocket(this.port);
        ArrayList<Piece> pieces = new ArrayList<>();
        for(int i = 0; i < MAX_LENGTH / PIECE_SIZE - 1; i++)
        {
            pieces.add(new Piece(i * PIECE_SIZE, (i + 1) * PIECE_SIZE, State.FREE, TTL));
        }
    }
    void work() throws IOException {

        Selector selector = Selector.open();
        serverSocket.getChannel().configureBlocking(false);
        SelectionKey key = serverSocket.getChannel().register(selector, SelectionKey.OP_ACCEPT);
        while(true) {

            int readyChannels = selector.select();
            if(readyChannels == 0) continue;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while(keyIterator.hasNext()) {

                key = keyIterator.next();
                if(key.isAcceptable()) {
                    Socket s = serverSocket.accept();
                    s.getChannel().register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    ReadableByteChannel channel = (ReadableByteChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1);
                    channel.read(byteBuffer);
                    switch (Message.values()[byteBuffer.get(0)])
                    {
                        case INIT:
                            break;
                        case CALCULATED:
                            break;
                        case FINDED:
                            break;
                    }
                }
                keyIterator.remove();
            }
        }
    }
}
