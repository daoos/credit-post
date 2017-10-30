package bean;

import java.util.List;

/**
 * 句子对象
 * Created by hpre on 16-10-28.
 */
public class SentenceTerm
{
    private String sentence;
    private int offset;
    private List<ComNerTerm> comNerTermList;

    public SentenceTerm(String sentence, List<ComNerTerm> comNerTermList, int offset)
    {
        this.sentence = sentence;
        this.offset = offset;
        this.comNerTermList = comNerTermList;
    }

    public String getSentence()
    {
        return sentence;
    }

    public void setSentence(String sentence)
    {
        this.sentence = sentence;
    }

    public List<ComNerTerm> getComNerTermList()
    {
        return comNerTermList;
    }

    public void setComNerTermList(List<ComNerTerm> comNerTermList)

    {
        this.comNerTermList = comNerTermList;
    }

    @Override
    public String toString() {
        return sentence + ", <" + offset + "> ," + comNerTermList;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
