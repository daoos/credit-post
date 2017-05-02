package parse;
import bean.HttpClient;
import bean.syntacticTree.DepTag;
import bean.syntacticTree.DependencyTree;
import bean.syntacticTree.DependencyTree.TreeNode;
import bean.syntacticTree.PosBase;
import bean.syntacticTree.Tuple2;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNllLine;
import com.hankcs.hanlp.dependency.IDependencyParser;
import com.hankcs.hanlp.dependency.nnparser.NeuralNetworkDependencyParser;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.hanlp.utility.Predefine;
import org.apache.http.client.ClientProtocolException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class OpinionMining {
    // 带负面信息的词汇
    private Set<String> negativeInfos = null;
    // 否定词
    private Set<String> negativeWords = null;
    // 表“积极意义”的动词
    private Set<String> definitiveDic = null;

    private HttpClient client = null;

 //   private Segment segment = null;
    private IDependencyParser parser = null;

    public static enum State {
        positive,
        negative,
        inverse,
        definitive
    }

    public static void loadDictionary(Set<String> lexicon, File path)
            throws FileNotFoundException {
        Scanner input = new Scanner(path);

        while (input.hasNextLine()) {
            lexicon.add(input.nextLine());
        }

        input.close();
    }

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

        // 句法依存API
        String url = "http://h127:7777/api/dp";
        client = new HttpClient(url);
    }

    public static void main(String[] args) throws ClientProtocolException, IOException {

//		loadSentimentDictionary(positiveDic, args[1]);

        OpinionMining opinion = new OpinionMining("model");

    //    Scanner input = new Scanner(new File(args[0]));

        int negative = 0;
        int positive = 0;
   //     while (input.hasNextLine())
        {
     //       String line = input.nextLine();
			String line = "企业当前银行负债规模过高";
            State s = opinion.expansion(line);

            switch (s) {
                case negative:
                    negative ++;
                    break;
                case positive:
                    positive ++;
                    System.out.println(line);
                    break;
                default: break;
            }
        }

        System.out.printf("Positive: %d, Negative: %d\n", positive, negative);

//		input.close();
    }

    private String cleanNoise(String text) {
        return text.replaceAll("《.*?》", "");
    }

    private CoNLLSentence parse(String text)
            throws ClientProtocolException, IOException {
        String response = client.query(text);
        CoNLLSentence conll = null;
        List<CoNllLine> lines = new LinkedList<>();

        for (String line: response.split("\n")) {
            String[] tokens = line.split("\t");
            if (tokens.length != 10) {
                break;
            }

            CoNllLine cl = new CoNllLine(tokens);
            lines.add(cl);
        }

        if (lines.size() != 0)
            try {
                conll = new CoNLLSentence(lines);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(text);
            }

        return conll;
    }

    public State expansion(String text) throws ClientProtocolException, IOException {
        CoNLLSentence deps = null;
//		try {
//			deps = parse(text);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
        // 语言云接口出现问题，使用本地接口
        if (deps == null) {
            List<Term> terms = StandardTokenizer.segment(cleanNoise(text));
            deps = parser.parse(terms);
        }

        DependencyTree tree = new DependencyTree(deps);
        State state = polarityJudge(tree.root);

        return state;
    }


    private State polarityJudge(DependencyTree.TreeNode root) {
        // 叶节点，判断自身是否为负面词
        if (root.children == null) {
            if (negativeInfos.contains(root.word)) {
//				System.out.println(root.word);
                return State.negative;
            } else if (negativeWords.contains(root.word)) {
//				System.out.println(root.word);
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
//			System.out.println(root.word);
            negative ++;
        } else if (negativeWords.contains(root.word)) {
//			System.out.println(root.word);
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

    private static void expansion2(Set<String> sentimentDic, Set<String> negativeWords, DependencyTree tree) {
        for (TreeNode node: tree.nodes.values()) {
            System.out.println(node);
            int negWordsCount = 0;

            if (node.children == null)
                continue;

            for (TreeNode child: node.children) {
                if (isModifier(child.tag) && sentimentDic.contains(child.word)) {
                    negWordsCount++;
                }
            }

            if (node.parent != null &&
                    isModifier(node.tag) &&
                    negativeWords.contains(node.parent.word)) {
                negWordsCount++;
            }

            if (negWordsCount % 2 != 0) {
                System.out.println("negative！！！");
            }
            System.out.println(negWordsCount);
        }
    }

    private static boolean isModifier(DepTag tag) {
        switch (tag) {
            case SBV:
            case VOB:
            case IOB:
            case ATT:
                return true;
            default: break;
        }

        return false;
    }

    private static boolean relationCheck(String reln) {
        switch (DepTag.valueOf(reln)) {
            case SBV:
            case VOB:
            case COO:
            case IOB:
            case ATT:
                return true;
            default: break;
        }
        return false;
    }

    private static boolean sentimentPOSCheck(String tag) {
        switch (PosBase.valueOf(tag.substring(0, 1))) {
            case a:
            case n:
            case d:
            case v:
            case i: // 成语
                return true;
            default: break;
        }
        return false;
    }

    private static boolean targetPOSCheck(String tag) {
        switch (PosBase.valueOf(tag.substring(0, 1))) {
            case n:
            case v:
            case i: // 成语
                return true;
            default: break;
        }
        return false;
    }

    private static Tuple2<String, String> propagate(CoNLLWord leftDirection,
                                                    CoNLLWord rightDirection) {
        String sentiment = leftDirection.LEMMA;
        String target = rightDirection.LEMMA;
        return Tuple2.create(sentiment, target);
    }


    private static void expansion(Set<String> sentimentDic, CoNLLSentence sentence) {
        List<Tuple2<String, String>> couples = new LinkedList<Tuple2<String, String>>();
        Set<String> expandedDic = new HashSet<String>();
        Set<String> sentimentWords = new HashSet<String>();
        Set<String> target = new HashSet<String>();
        Multimap<String, CoNLLWord> sentimentDeps = HashMultimap.create();
        Multimap<String, CoNLLWord> targetDeps = HashMultimap.create();

        expandedDic.addAll(sentimentDic);
        for (CoNLLWord word: sentence) {
            String dep = word.LEMMA;
            String depTag = word.POSTAG;
            String gov = word.HEAD.LEMMA;
            String govTag = word.HEAD.POSTAG;
            String reln = word.DEPREL;

            if (expandedDic.contains(dep) &&
                    relationCheck(reln) &&
                    sentimentPOSCheck(depTag)) {
                // R1_1 O->Dep_O->T
                if (targetPOSCheck(govTag)) {
                    // 找到目标对象
                    couples.add(Tuple2.create(dep, gov));
                    System.out.println(dep + "->" + gov);
                }

                // R1_2 O->Dep_O->H
                if (!targetDeps.containsKey(gov)) {
                    sentimentDeps.put(gov, word);
                } else {
                    Collection<CoNLLWord> c = targetDeps.get(gov);
                    for (CoNLLWord w: c) {
                        couples.add(propagate(word, w));
                    }
                }
            }

            // R1_2 H<-Dep_T<-T
            else if (!expandedDic.contains(dep) &&
                    relationCheck(reln) &&
                    targetPOSCheck(govTag)) {

                if (!sentimentDeps.containsKey(gov)) {
                    targetDeps.put(gov, word);
                } else {
                    Collection<CoNLLWord> c = sentimentDeps.get(gov);
                    for (CoNLLWord w: c) {
                        couples.add(propagate(w, word));
                    }
                }
            }

            //TODO:扩展情感词
        }

        // 检查负面信息
        System.out.println(couples);
    }
}
