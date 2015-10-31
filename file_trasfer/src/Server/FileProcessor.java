package Server;

import java.io.*;
import java.nio.file.FileSystem;

/**
 * Created by morsk on 10/29/2015.
 */
public class FileProcessor {
    static public final String path = "downloads" + File.separator;
    OutputStream outputStream = null;
    FileProcessor(String name) throws FileNotFoundException
    {
        File file = new File(path + name);
        outputStream = new FileOutputStream(file);
    }
    public void write(byte[] bytes, int size) throws IOException
    {
        outputStream.write(bytes, 0, size);
    }
}
