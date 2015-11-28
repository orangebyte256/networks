package main.java.request;

import java.net.UnknownHostException;

/**
 * Created by orangebyte256 on 24.11.15.
 */
public class SendRequest extends BaseRequest {
    private static final String[] VALID_METHODS = {"GET", "POST", "HEAD"};
    private static final String VALID_PROTOCOL = "http";

    public SendRequest(String string) throws UnknownHostException {
        super(string.substring(string.indexOf(BaseRequest.LINE_TRANSLATION) + BaseRequest.LINE_TRANSLATION.length()));
        String[] mainLine = string.substring(0, string.indexOf(BaseRequest.LINE_TRANSLATION)).split(" ");
        method = mainLine[0];
        adress = new Adress(mainLine[1]);
        version = mainLine[2].substring(mainLine[2].indexOf(VERSION_START) + VERSION_START.length() + 1);                                 //!!! can be space inside query
        version = "1.0";
    }
    boolean isValid()
    {
        if(!adress.protocol.equals(VALID_PROTOCOL))
            return false;
        for(String s : VALID_METHODS)
        {
            if(s.equals(method))
                return true;
        }
        return false;
    }
    public String toString()
    {
        return method + " " + adress.rest + " " + VERSION_START + "/" + version + LINE_TRANSLATION + super.toString();
    }
    private String method;
    private Adress adress;
    private String version;
}
