package common;

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
        int n = 4;
        while(value / n != 0)
        {
            value -= n;
            n *= 4;
        }
        String result = "";
        while(value != 0)
        {
            result += Gen.values()[value % 4].toChar();
            value /= 4;
        }
        return result;
    }
}
