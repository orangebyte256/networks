package common;

/**
 * Created by morsk on 10/31/2015.
 */
public enum Message
{
    INIT(0), GET_WORK(1), CALCULATED(2), FINDED(3);
    private int value;
    Message(int value)
    {
        this.value = value;
    }
    public byte[] toByte()
    {
        return new Integer(value).toString().getBytes();
    }
}
