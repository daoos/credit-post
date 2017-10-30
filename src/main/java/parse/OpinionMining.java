package parse;

import bean.syntacticTree.DepTag;
import bean.syntacticTree.DependencyTree;
import bean.syntacticTree.DependencyTree.TreeNode;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.dependency.IDependencyParser;
import com.hankcs.hanlp.dependency.nnparser.NeuralNetworkDependencyParser;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.hanlp.utility.Predefine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * 采用简单的依存句法树做负面信息的判断
 */
public class OpinionMining {
    // 带负面信息的词汇
    private Set<String> negativeInfos = null;
    // 否定词
    private Set<String> negativeWords = null;
    // 表“积极意义”的动词
    private Set<String> definitiveDic = null;


    private IDependencyParser parser = null;

    public static enum State {
        positive,
        negative,
        inverse,
        definitive
    }

    /**
     * 加载相关词典
     * @param lexicon
     * @param path
     * @throws FileNotFoundException
     */
    public static void loadDictionary(Set<String> lexicon, File path)
            throws FileNotFoundException {
        Scanner input = new Scanner(path);

        while (input.hasNextLine()) {
            lexicon.add(input.nextLine());
        }

        input.close();
    }

    /**
     * 初始化
     * @param confBaseDir
     * @throws FileNotFoundException
     */
    public OpinionMining(String confBaseDir) throws FileNotFoundException {
        negativeInfos = new HashSet<String>();
        negativeWords = new HashSet<String>();
        definitiveDic = new HashSet<String>();
        Predefine.HANLP_PROPERTIES_PATH =confBaseDir+"/hanlp.properties";
        parser = new NeuralNetworkDependencyParser().setSegment(StandardTokenizer.SEGMENT);
        parser = new NeuralNetworkDependencyParser().enableDeprelTranslator(false);

        // 读入情感词典
        loadDictionary(negativeInfos, new File(confBaseDir, "negative-infos.txt"));
        loadDictionary(negativeWords, new File(confBaseDir, "negative-words.txt"));
        loadDictionary(definitiveDic, new File(confBaseDir, "definitive.txt"));

    }


    private String cleanNoise(String text) {
        return text.replaceAll("《.*?》", "");
    }


    /**
     * 接口调用入口
     * @param text
     * @return
     * @throws IOException
     */
    public State expansion(String text) throws IOException {
        CoNLLSentence deps = null;

        if (deps == null) {
            List<Term> terms = StandardTokenizer.segment(cleanNoise(text));
            deps = parser.parse(terms);

        }

        DependencyTree tree = new DependencyTree(deps);
        State state = polarityJudge(tree.root);

        return state;
    }

    /**
     * 遍历依存树判断
     * @param root
     * @return
     */
    private State polarityJudge(DependencyTree.TreeNode root) {
        // 叶节点，判断自身是否为负面词
        if (root.children == null) {
            if (negativeInfos.contains(root.word)) {
                return State.negative;
            } else if (negativeWords.contains(root.word)) {
                return State.inverse;
            } else
                return State.positive;
        }

        // 非叶节点
        int negative = 0;
        int inverse = 0;
        for (TreeNode child: root.children) {
            State state = polarityJudge(child);

            switch (state) {
                case negative:
                    // 如果节点为“处理”，“解决”，“完成”等一些词，则其宾语部分的负面信息被消除
                    if (definitiveDic.contains(root.word) &&
                            // 动宾或间宾
                            (child.tag == DepTag.VOB ||
                                    child.tag == DepTag.IOB ||
                                    child.tag == DepTag.FOB ||
                                    child.tag == DepTag.IOB ||
                                    // 主谓
                                    child.tag == DepTag.SBV))
                        break;

                    negative++;
                    break;
                case inverse:
                    inverse++;
                    break;
                default: break;
            }
        }

        if (negativeInfos.contains(root.word)) {
            negative ++;
        } else if (negativeWords.contains(root.word)) {
            inverse ++;
        }

        boolean isNeg = negative > 0;
        boolean isInv = inverse % 2 != 0;

        if (isNeg && !isInv)
            return State.negative;
        else if (isNeg && isInv)
            return State.positive;
        else if (!isNeg && !isInv)
            return State.positive;
        else
            return State.negative;
    }

}
