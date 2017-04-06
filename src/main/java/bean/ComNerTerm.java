package bean;

import java.io.Serializable;

/**
 * Created by hpre on 16-10-21.
 */
public class ComNerTerm implements Serializable
{
    private static final long serialVersionUID = 1L;

    public String word;
    public String typeStr;
    public int offset;

    public ComNerTerm()
    {
    }

    public ComNerTerm(String typeStr, String word)
    {
        this.typeStr = typeStr;
        this.word = word;
    }

    public ComNerTerm(String word, String typeStr, int offset)
    {
        this.word = word;
        this.typeStr = typeStr;
        this.offset = offset;
    }

    @Override
    public String toString()
    {
        return String.join(",", new String[] {word, typeStr.toString(),""+offset});
    }
}
