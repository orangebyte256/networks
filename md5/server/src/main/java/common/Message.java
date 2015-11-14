package common;

/**
 * Created by morsk on 10/31/2015.
 */
public enum Message
{
    GET_HASH(0), GET_WORK(1), CALCULATED(2), FINDED(3), END(4);
    private int value;
    Message(int value)
    {
        this.value = value;
    }
    public byte toByte()
    {
        return (byte)value;
    }
}
