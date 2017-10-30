package localuse;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.hankcs.hanlp.utility.Predefine;

/**
 *made by freedom
 * usage：
 * 1、分词
 * 2、调整训练集分词错误，即替换
 */

public class Segment
{

	private static String input = "/home/hpre/222/";
	private static String out = "/home/hpre/222-out/";

	/**
	 * 主方法
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
//		normalSeg();
		modifySegError();
	}

	/**
	 * 分词
	 * @throws IOException
	 */
	public static void normalSeg() throws IOException
	{
		Predefine.HANLP_PROPERTIES_PATH = "/home/hpre/program/cmb/model/hanlp.properties";
		File dirPath = new File(input);
		File[] files = dirPath.listFiles();
		for (File file : files)
		{
			FileWriter fileWriter = new FileWriter(new File(out+file.getAbsolutePath().substring(input.length())));
			Scanner scanner = new Scanner(file);
			System.out.println(file);
			while (scanner.hasNext())
			{
				String temStr = scanner.nextLine();
				temStr=temStr.replaceAll(" ", "");
				List<Term> segStr = StandardTokenizer.segment(temStr);
				String result = "";
				for (Term sTerm:segStr)
				{
					result = result+sTerm.word+"_"+sTerm.nature+" ";
				}
				System.out.print(result);
				System.out.println();
				fileWriter.write(result+"\n");
			}
			scanner.close();
			fileWriter.close();
		}
	}

	/**
	 * 修正训练集中分词错误， 此处训练集指分词、标注后的训练集 格式为 "#AC#xx#AC#"
	 * @throws IOException
	 */
	private static void modifySegError() throws IOException {
		String[] repBeforeArr = new String[]{"经办_vn 行_ng", "套_q 现在_t",
				"不少_mq 于_p", "流_v 贷_v", "贷_v 后_f", "出_vf 账_ng", "短_a 贷_v 长_a 用_p",
				"年前_t", "月报表_nz", "不_d 再续_d 做_v", "他_rr 行_ng", "排_v 摸_v"
				, "控制_vnn 人_n", "与其_c", "我_rr 行_ng", "商_vg 票_n", "他_rr 项_q 额度_n"
				, "中列明_ns", "第_mq 三_m 方_q", "补仓_nz 线_n", "盯_v 市_n", "占_v 比_p"
				, "订单_n 贷_v", "相关_vn 方_q", "借_v 新_a 还_d 旧_a", "回笼_vi 款_q", "审_v 贷_v 会_v"
				, "信管_nz 部_q", "个贷_n 部_q", "保_v 贴_v", "把_pba 控_v", "叙_b 做_v"
				, "招_v 银信_nz 部_q", "承诺_vn 函_n", "他_rr 项_q 权利_n 证_n", "实_ad 贷_v 实付_nz", "申报_vn 行_ng"
				, "定_v 增_v 价_n", "如不_c", "并以_c", "不以_c", "如有_c"
				, "应付_v 账款_n", "不_d 再续_d 作_v", "银_ng 承_vg", "须经_nz", "本_rz 笔_n"
				, "及其_cc", "有_vyou 息_ng", "不保_v 付_v", "先向_n", "上_f 报经_v"
				, "股_q 权_n", "可不_dl", "中的_v", "当_p 期_qv", "还_d 款_q"
				, "信_n 保_v", "流通股_nz 票_n", "订单_n 量_n", "不_d 利_n", "匹配_vi 性_ng"
				, "经营风险_nz", "融资性担保公司_nt", "开发_vn 贷_v", "用_p 信_n", "担保_vn 圈_qv"
				, "应收_v 帐_ng 款_q", "挪_v 用于_v", "所_usuo 在_p", "大_a 于_p", "发_v 债_n"
				, "注意到_v 期_qv", "经_p 营_n", "不_d 再提_nz 用_p", "叙_b 做_v", "当_p 期_qv"
				, "与其_c 他_rr", "中_f 止_v", "须由红_nr", "其_rz 中流_n 贷_v","股份公司_nis"
				, "资金_n 流_v", "应收_v 账款_nz", "二套房_nz 产_v", "其_rz 中流_n 贷_v", "发_v 放流_v 贷_v"
				, "可分_v 笔_n", "可分_v 次_qv", "广告_n 费_n", "资产负债_n", "用_p 于_p"
				, "提_v 用时_n", "财务费用_nz", "对本_r", "帐_ng", "在_p 建_v"
				, "获_v 批_q", "短_a 借_v 长_a 用_p", "后_f 期_qv", "周转资金_nz", "归_v 行_ng"
				, "代开_v 证_n", "我_rr 行为_n", "银行借款_nz", "投资总额_nz", "长期借款_nz"
				, "商业模式_nz", "行业动态_nz", "煤炭行业_nz", "高度重视_l", "贷款额_n 度_qv"
				, "持续上升_l", "服装服饰_nz", "北京华_nz", "贷_v 前_f", "开_v 立_v"
				, "贷_v 中_f", "我_rr 行时_n", "可调_nz 剂_q", "书面报告_nz", "授_vg 信_n"
				, "不到_v 位_q", "审_v 贷_v 官_n", "工程质量_nz", "商品销售_nz", "继续加强_n"
				, "现金流量_nz", " 受到影响_v", "出现异常_l", "担保责任_n", "资质审查_n"
				, "企业简介_nz", "公司章程_nz", "保险条款_nz", "其他_rzv 行_ng", "土地补偿_i"
				, "控制_vn 人_n", "开发进展_nz", "园林施工_n", "投资收益_v", "控制_vn 人名_n 下_f"
				, "控制能力_n", "半_mq 年内_t", "不_d 得以_v", "开发进展_nz", "供货渠道_n"
				, "不_d 继而_c", "调查报告_nz", "信用等级_nz", "根据上述_n", "不符_v 合流_vi 贷_v"
				, "有_vyou 限_v", "产品价格_nz", "商品交易_nz", "不良贷款_n", "到_v 期_qv"
				, "市场份额_nz", "续_v 做_v", "产品出口_nz", "产品价格_nz", "销售市场_nz"
				, "汇率变动_nz", "优先购买_l", "发行股票_n", "后视_n", "报审_nz 贷_v 会_v"
				, "市场需求_nz", "投资规模_nz", "用_p 途_ng", "劳资纠纷_nz", "汽车行业_nz"
				, "资金平衡_n", "管理工作_n", "成本上升_nz", "历史沿革_nz", "经营范围_nz"
				, "家庭财产_nz", "承受能力_nz", "无瑕_z 疵_n", "出口贸易_v", "转口贸易_nz"
				, "石油化工_nz", "最新进展_nt", "用信_vn 人_n", "最新动向_n", "其_rz 中原_ns 因_p"
				, "个人信用_nz", "长期投资_nz", "在此期间_l", "顺利开展_nz", "额_n 度_qv"
				, "机械制造_nz", "精密机械_n", "发现异常_l"};
		String[] repLaterArr = new String[]{"经办行_n", "套现_nz 在_p", "不_d 少于_v",
				"流贷_n", "贷后_qt", "出账_n", "短贷长用_n", "年_qt 前_f",
				"月_qt 报表_nz", "不再_d 续做_nz", "他行_n", "排摸_v", "控制人_n"
				, "与_cc 其_rz", "我行_n", "商票_n", "他项额度_n", "中_f 列明_nz"
				, "第三方_b", "补仓线_n", "盯市_n", "占比_n", "订单贷_n"
				, "相关方_n", "借新还旧_n", "回笼款_n", "审贷会_n", "信管部_n"
				, "个贷部_n", "保贴_n", "把控_v", "叙做_v", "招银信部_n"
				, "承诺函_n", "他项权利证_n", "实贷实付_n", "申报行_n", "定增价_n"
				, "如_v 不_d", "并_cc 以_p", "不_d 以_p", "如_v 有_vyou", "应付账款_n"
				, "不再_d 续作_nz", "银承_n", "须_d 经_p", "本笔_nz", "及_cc 其_rz"
				, "有息_nz", "不_d 保付_v", "先_d 向_p", "上报_vi 经_p", "股权_n"
				, "可_v 不_d", "中_f 的_ude1", "当期_f", "还款_vn", "信保_vn"
				, "流通_vn 股票_n", "订单量_n", "不利_a", "匹配性_n", "经营_vn 风险_n"
				, "融资性_b 担保_vn 公司_nis", "开发贷_n", "用信_vn", "担保圈_n", "应收账款_n"
				, "挪用_v 于_p", "所在_n", "大于_v", "发债_vn", "注意_v 到期_vi"
				, "经营_vn", "不再_d 提用_v", "叙做_v", "当期_f", "与_cc 其他_rzv"
				, "中止_v", "须_d 由_p 红_a", "其中_rz 流贷_n", "股份_n 公司_nis", "资金流_nz"
				, "应收账款_n", "二套_mq 房产_n", "其中_rz 流贷_n", "发放_v 流贷_n", "可_v 分_qt 笔_n"
				, "可_v 分_qt 次_qv", "广告费_n", "资产_n 负债_vn", "用于_v", "提用_v 时_qt"
				, "财务_n 费用_n", "对_p 本_rz", "账_n", "在建_v", "获批_v"
				, "短借长用_n", "后期_f", "周转_vn 资金_n", "归行_v", "代_q 开证_vi"
				, "我行_n 为_p", "银行_nis 借款_n", "投资_vn 总额_n", "长期_d 借款_n", "商业_n 模式_n"
				, "行业_n 动态_n", "煤炭_n 行业_n", "高度_d 重视_v", "贷款_n 额度_n", "持续_vd 上升_vi"
				, "服装_n 服饰_n", "北京_ns 华_b", "贷前_qt", "开立_v", "贷中_qt"
				, "我行_n 时_qt", "可_v 调剂_vn", "书面_b 报告_n", "授信_vn", "不_d 到位_vi"
				, "审贷官_n", "工程_n 质量_n", "商品_n 销售_vn", "继续_v 加强_v", "现金_n 流量_n"
				, "受到_v 影响_vn", "出现_v 异常_a", "担保_vn 责任_n", "资质_n 审查_vn", "企业_n 简介_n"
				, "公司_n 章程_n", "保险_n 条款_n", "其_rz 他行_n", "土地_n 补偿_vn", "控制人_n"
				, "开发_vn 进展_vn", "园林_n 施工_vn", "投资_vn 收益_n", "控制人_n 名下_n", "控制_vn 能力_n"
				, "半年_t 内_f", "不得_vi 以_p", "开发_vn 进展_vn", "供货_vn 渠道_n", "不继_an 而_cc"
				, "调查_vn 报告_n", "信用_n 等级_n", "根据_p 上述_b", "不_d 符合_v 流贷_n", "有限_a"
				, "产品_n 价格_n", "商品_n 交易_vn", "不良_a 贷款_n", "到期_vi", "市场_n 份额_n"
				, "续做_nz", "产品_n 出口_vn", "产品_n 价格_n", "销售_vn 市场_n", "汇率_n 变动_vn"
				, "优先_ad 购买_v", "发行_v 股票_n", "后_f 视_vg", "报_n 审贷会_n", "市场_n 需求_n"
				, "投资_n 规模_n", "用途_n", "劳资_n 纠纷_n", "汽车_n 行业_n", "资金_n 平衡_a"
				, "管理_vn 工作_vn", "成本_n 上升_vi", "历史_n 沿革_n", "经营_vn 范围_n", "家庭_n 财产_n"
				, "承受_v 能力_n", "无_v 瑕疵_n", "出口_vn 贸易_vn", "转口_vn 贸易_vn", "石油_n 化工_n"
				, "最新_a 进展_vn", "用信人_nz", "最新_a 动向_n", "其中_rz 原因_n", "个人_n 信用_n"
				, "长期_d 投资_vn", "在_p 此_rzs 期间_f", "顺利_ad 开展_v", "额度_n", "机械_n 制造_vn"
				, "精密_a 机械_n", "发现_v 异常_a"};

		String dirIn = "/home/hpre/program/cmb/cmbCom";
		String dirOut = "/home/hpre/program/cmb/cmbCom2";
		File[] files = new File(dirIn).listFiles();
		for (File file : files) {
			String fileName = file.getName();
			FileWriter fileWriter = new FileWriter(dirOut + "/" + fileName);
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				String strLine = scanner.nextLine();
				for (int i = 0; i < repBeforeArr.length; i++) {
					if (strLine.contains(repBeforeArr[i])) {
						strLine = strLine.replace(" " + repBeforeArr[i] + " ", " " + repLaterArr[i] + " ")
								.replace(" " + repBeforeArr[i] + "#", " " + repLaterArr[i] + "#")
								.replace("#" + repBeforeArr[i] + " ", "#" + repLaterArr[i] + " ")
								.replace("#" + repBeforeArr[i] + "#", "#" + repLaterArr[i] + "#");
					}
					if (strLine.startsWith(repBeforeArr[i])) {

					}
				}
				fileWriter.write(strLine + "\n");
			}
			scanner.close();
			fileWriter.close();
		}
	}

}
