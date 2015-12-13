package main.java.server;

import main.java.request.AnswerRequest;
import main.java.request.BaseRequest;
import main.java.request.SendRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by orangebyte256 on 11.12.15.
 */
public class Pipe
{
    enum State {READ_INPUT, OTHER, END};
    private State state = State.READ_INPUT;
    private static int CLOSED_SOCKET = -1;
    static private final int BUFFER_SIZE = 100000;
    private Selector selector = null;
    private Map<SocketChannel, ByteBuffer> buffers = new HashMap<>();
    private SocketChannel input = null;
    private SocketChannel output = null;

    private void processChannel(SocketChannel socketChannel, Selector selector) throws IOException {
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }
    public Pipe(SocketChannel input, Selector selector) throws IOException {
        processChannel(input, selector);
        this.selector = selector;
        buffers.put(input, ByteBuffer.allocate(BUFFER_SIZE));
        this.input = input;
    }
    public void read(SocketChannel channel, SelectionKey key) throws IOException {
        ByteBuffer buffer = buffers.get(channel);
        if(channel.read(buffer) == CLOSED_SOCKET)
        {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            buffer.flip();
            BaseRequest baseRequest = null;
            key.cancel();
            if(state == State.READ_INPUT) {
                baseRequest = new SendRequest(bytes);
                SocketChannel client = SocketChannel.open();
                client.connect(((SendRequest) baseRequest).getAdress());
                processChannel(client, selector);
                output = client;
                output.register(selector, SelectionKey.OP_WRITE);
                buffers.put(output, ByteBuffer.allocate(BUFFER_SIZE));
                state= State.OTHER;
            }
            else
            {
                baseRequest = new AnswerRequest(bytes);
                input.register(selector, SelectionKey.OP_WRITE);
            }
            ByteBuffer tmp = buffers.get(channel);
            baseRequest.fill(buffers.get(tmp));
        }
    }
    public void write(SocketChannel channel, SelectionKey key) throws IOException
    {
        ByteBuffer buffer = buffers.get(channel);
        buffer.flip();
        System.out.println(buffer.array());
        channel.write(buffer);
        if(!buffer.hasRemaining())
        {
            key.cancel();
        }
        buffer.flip();
    }
    public boolean isChannelBelong(SocketChannel channel)
    {
        return channel.equals(input) || channel.equals(output);
    }
}
