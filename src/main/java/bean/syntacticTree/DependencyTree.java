package bean.syntacticTree;

/**
 * Created by hadoop on 17-4-26.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;
import com.hankcs.hanlp.corpus.tag.Nature;

public class DependencyTree {
    public static class TreeNode {
        // 与父节点的依存关系
        public DepTag tag;
        public String word;
        public Nature pos;

        public int id;

        public TreeNode parent;
        public List<TreeNode> children;

        boolean negative = false;

        @Override
        public String toString() {
            return String.format("%s_%s --%s--> %s; %s", word, pos, tag, parent==null? null: parent.word, children==null? null: children.size());
        }
    }

    public Map<Integer, TreeNode> nodes = null;
    public TreeNode root = null;

    /**
     * 生成依存树
     * @param sentence
     */
    public DependencyTree(CoNLLSentence sentence) {
        nodes = new HashMap<Integer, TreeNode>();

        // 简单处理：做两次遍历，第一次生成所有节点对象，第二次补全节点的parent,children信息
        for (CoNLLWord word: sentence) {
            TreeNode node = new TreeNode();

            node.tag = DepTag.valueOf(word.DEPREL);
            node.word = word.LEMMA;
            node.pos = Nature.valueOf(word.POSTAG);
            node.id = word.ID;

            nodes.put(word.ID, node);
        }

        for (CoNLLWord word: sentence) {
            if (DepTag.valueOf(word.DEPREL) == DepTag.HED)
                root = nodes.get(word.ID);
            if (!nodes.containsKey(word.HEAD.ID))
                continue;

            TreeNode node = nodes.get(word.ID);

            node.parent = nodes.get(word.HEAD.ID);
            if (node.parent.children == null) {
                node.parent.children = new ArrayList<TreeNode>();
            }
            node.parent.children.add(node);
        }
    }
}
