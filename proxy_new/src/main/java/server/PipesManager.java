package main.java.server;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * Created by morsk on 10/31/2015.
 */


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
