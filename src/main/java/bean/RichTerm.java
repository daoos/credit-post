package bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hankcs.hanlp.corpus.tag.Nature;

/**
 * Created by hadoop on 17-6-7.
 */
public class RichTerm {
	// 词语
	public String word;
	// 词性
	public Nature pos;
	// 命名实体类型
	public String comTypeStr;
	@JsonCreator
	public RichTerm(@JsonProperty("word") String word,
                    @JsonProperty("pos") Nature pos,
                    @JsonProperty("comTypeStr") String comTypeStr
		) {
		this.word = word;
		this.pos = pos;
		this.comTypeStr = comTypeStr;
	}

	@Override
	public String toString() {
		return String.join(",", new String[] {word, 
				pos == null?"null":pos.toString(), 
						comTypeStr == null?"OUT":comTypeStr.toString()});
	}
}
