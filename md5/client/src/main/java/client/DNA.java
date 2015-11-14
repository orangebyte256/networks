package client;

/**
 * Created by morsk on 10/31/2015.
 */
public class DNA {
    static enum Gen
    {
        A('A'),G('G'),C('C'),T('T');
        private char value;
        Gen(char value)
        {
            this.value = value;
        }
        public char toChar()
        {
            return value;
        }
    }
    static public String getStringDNA(int value)
    {
        int n = Gen.values().length;
        int count = 1;
        while(value / n != 0)
        {
            value -= n;
            n *= Gen.values().length;
            count++;
        }
        String result = "";
        for(int i = 0; i < count; i++)
        {
            result += Gen.values()[value % Gen.values().length].toChar();
            value /= Gen.values().length;
        }
        return result;
    }
}
