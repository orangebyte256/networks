package main.java.request;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by orangebyte256 on 24.11.15.
 */
public class AnswerRequest extends BaseRequest {
    public AnswerRequest(byte[] bytes) throws UnknownHostException {
        super(bytes);
        String string = new String(bytes, StandardCharsets.UTF_8);
        line = string.substring(0, string.indexOf(LINE_TRANSLATION));
        line = line.substring(line.indexOf(SPACE));
    }
@Override
    public void fill(ByteBuffer buffer)
    {
        String temp = VERSION + line + LINE_TRANSLATION;
        buffer.put(temp.getBytes());
        super.fill(buffer);
    }
    private static final String VERSION = "HTTP/1.0";
    private static final String SPACE = " ";
    private String line;
}
