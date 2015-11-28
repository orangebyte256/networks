package main.java.request;

import java.net.UnknownHostException;

/**
 * Created by orangebyte256 on 24.11.15.
 */
public class AnswerRequest extends BaseRequest {
    public AnswerRequest(String string) throws UnknownHostException {
        super(string.substring(string.indexOf(LINE_TRANSLATION) + LINE_TRANSLATION.length()));
        String[] mainLine = string.substring(0, string.indexOf(LINE_TRANSLATION)).split(" ");
        version = mainLine[0].substring(mainLine[0].indexOf(VERSION_START) + VERSION_START.length() + 1);
        String first = string.substring(0, string.indexOf(LINE_TRANSLATION));
        rest = first.substring(first.indexOf(LINE_TRANSLATION) + LINE_TRANSLATION.length() + 1);
        version = "1.0";
    }
    public String toString()
    {
        return VERSION_START + "/" + version + " " + rest + LINE_TRANSLATION + super.toString();
    }
    public static String make(int num, String text)
    {
        return VERSION_START + "/" + "1.0" + " " + new Integer(num).toString() + " " + text + LINE_TRANSLATION + LINE_TRANSLATION;
    }
    private String version;
    private String rest;
}
