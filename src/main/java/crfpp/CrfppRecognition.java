package crfpp;

import bean.RichTerm;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import conf.Config;
import org.apache.commons.lang.StringUtils;
import org.chasen.crfpp.Tagger;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by hpre on 16-12-16.
 */
public class CrfppRecognition
{
    private final Tagger tagger;
    private List<RichTerm> terms;
    private static Term begin = null;
    private static Term end = null;

    static
    {
        try
        {
            System.loadLibrary("CRFPP");
        } catch (UnsatisfiedLinkError e)
        {
            System.err.println("Cannot load the example native code.\n"
                    + "Make sure your LD_LIBRARY_PATH contains the path to libCRFPP.so\n" + e);
            System.exit(1);
        }
    }

    public CrfppRecognition(String modelFile)
    {
        tagger = new Tagger("-m " + modelFile);

        tagger.clear();
        terms = new LinkedList<>();

        begin = new Term("#SENT_BEG#", Nature.begin);
        end = new Term("#SENT_END#", Nature.end);
    }

    private void add(String... atts)
    {
        tagger.add(StringUtils.join(atts, "\t"));
    }

    public void addTerms(List<Term> termList)
    {
        add(begin.word, begin.nature.toString());

        for (Term term: termList)
        {
            add(term.word, term.nature.toString());
        }
        add(end.word, end.nature.toString());
    }

    public void clear()
    {
        terms.clear();
        tagger.clear();
    }

    public List<RichTerm> parse()
    {
        if (Config.isDebug())
        {
            System.out.println("column size: " + tagger.xsize());
            System.out.println("token size: " + tagger.size());
            System.out.println("tag size: " + tagger.ysize());

            System.out.println("tagset information:");
            for (int i = 0; i < tagger.ysize(); ++i)
            {
                System.out.println("tag " + i + " " + tagger.yname(i));
            }
        }

        if (!tagger.parse())
            return terms;

        for (int i = 0; i < tagger.size(); ++i)
        {
            if (Config.isDebug())
            {
                for (int j = 0; j < tagger.xsize(); ++j)
                {
                    System.out.print(tagger.x(i, j) + "\t");
                }

                System.out.print(tagger.y2(i) + "\t");
                System.out.print("\n");
            }

//            System.out.println(i+"\t"+tagger.x(i,0)+"\t"+tagger.x(i,1)+"\t"+tagger.y(i)+"\t"+tagger.y2(i));//+"\t"+tagger.x(i,2)
            String test = tagger.y2(i);
            RichTerm term = new RichTerm(tagger.x(i, 0),
                    Nature.fromString(tagger.x(i, 1)), tagger.y2(i));//ComAnnotation.fromString(tagger.y2(i))

            terms.add(term);
        }
        return terms;
    }

    public static List<String> combine(List<RichTerm> terms)
    {
        List<String> list = new LinkedList<String>();

        return list;
    }
}
