package Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by morsk on 10/29/2015.
 */
public class FileProcessor {
    Path path = null;
    public FileProcessor(Path thePath)
    {
        path = thePath;
    }
    public byte[] getName()
    {
        return path.getFileName().toString().getBytes();
    }
    public byte[] getData() throws IOException
    {
        return Files.readAllBytes(path);
    }
}
