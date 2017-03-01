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

    public ComNerTerm(String s, String w)
    {
        this.typeStr = s;
        this.word = w;
    }

    public ComNerTerm(String w, String s, int offset)
    {
        this.word = w;
        this.typeStr = s;
        this.offset = offset;
    }

    @Override
    public String toString()
    {
        return String.join(",", new String[] {word, typeStr.toString(),""+offset});
    }
}
