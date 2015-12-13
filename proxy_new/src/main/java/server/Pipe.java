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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by orangebyte256 on 11.12.15.
 */
public class Pipe
{
    enum State {READ_INPUT_HEADER, READ_OUTPUT_HEADER, OTHER};
    boolean isOutputClose = false;
    private State state = State.READ_INPUT_HEADER;
    private static int CLOSED_SOCKET = -1;
    static private final int BUFFER_SIZE = 10000;
    private Selector selector = null;
    private Map<SocketChannel, ByteBuffer> buffers_read = new HashMap<>();
    private Map<SocketChannel, ByteBuffer> buffers_write = new HashMap<>();
    private SocketChannel input = null;
    private SocketChannel output = null;
    private ByteBuffer right = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuffer left = ByteBuffer.allocate(BUFFER_SIZE);

    private void processChannel(SocketChannel socketChannel, Selector selector) throws IOException {
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }
    public Pipe(SocketChannel input, Selector selector) throws IOException {
        processChannel(input, selector);
        this.selector = selector;
        buffers_read.put(input, right);
        buffers_write.put(input, left);
        this.input = input;
    }
    public byte[] ByteBufferToBytes(ByteBuffer byteBuffer)
    {
        ByteBuffer tmp = byteBuffer.duplicate();
        tmp.flip();
        byte[] bytes = new byte[tmp.remaining()];
        tmp.get(bytes);
        return bytes;
    }
    public void read(SocketChannel channel, SelectionKey key) throws IOException {
        ByteBuffer buffer = buffers_read.get(channel);
        buffer.limit(BUFFER_SIZE - 100);
        int value = channel.read(buffer);
        System.out.println(value);
        if(value == 0)
        {
            buffers_write.get(channel).flip();
            if(buffers_write.get(channel).hasRemaining())
            {
                input.register(selector, SelectionKey.OP_WRITE);
            }
            else
            {
//                key.cancel();
            }
            buffers_write.get(channel).flip();
            return;
        }
        if(value == CLOSED_SOCKET)
        {
            isOutputClose = true;
            if(output != null && output.isOpen())
                output.close();
        }
        if(BaseRequest.isRead(ByteBufferToBytes(buffer)) && state != State.OTHER)
        {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            buffer.flip();
            buffer.clear();
            BaseRequest baseRequest = null;
            if(state == State.READ_INPUT_HEADER) {
                baseRequest = new SendRequest(bytes);
                if(!((SendRequest)baseRequest).isValid())
                {
                    channel.close();
                    key.cancel();
                    return;
                }
                SocketChannel client = SocketChannel.open();
                client.connect(((SendRequest) baseRequest).getAdress());
                processChannel(client, selector);
                output = client;
                output.register(selector, SelectionKey.OP_WRITE);
                buffers_write.put(output, right);
                buffers_read.put(output, left);
                state= State.READ_OUTPUT_HEADER;
            }
            else if(state == State.READ_OUTPUT_HEADER)
            {
                baseRequest = new AnswerRequest(bytes);
                input.register(selector, SelectionKey.OP_WRITE);
                state= State.OTHER;
            }
            baseRequest.fill(buffers_read.get(channel));
            System.out.println(state);
        }
        else
        {
            System.out.println(value);
            if(state == State.READ_INPUT_HEADER) {
                if(output != null)
                    output.register(selector, SelectionKey.OP_WRITE);
            }
            else
                input.register(selector, SelectionKey.OP_WRITE);
        }
    }
    public void write(SocketChannel channel, SelectionKey key) throws IOException
    {
        ByteBuffer buffer = buffers_write.get(channel);
        String a = new String(ByteBufferToBytes(buffer), StandardCharsets.UTF_8);
        System.out.println(a);
        buffer.flip();
        System.out.println(channel.write(buffer));
        if(!buffer.hasRemaining())
        {
            buffer.flip();
            if(isOutputClose)
            {
                input.close();
            }
            else
            {
                channel.register(selector, SelectionKey.OP_READ);
            }
            buffer.clear();
        }
        else
        {
            buffer.flip();
            channel.register(selector, SelectionKey.OP_WRITE);
        }
    }
    public boolean isChannelBelong(SocketChannel channel)
    {
        return channel.equals(input) || channel.equals(output);
    }
}
