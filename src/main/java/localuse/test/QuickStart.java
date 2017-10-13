package localuse.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class QuickStart {

    public static enum Type {
        biomedicine_query,
        onlybiomedicine_query,
        biomedicine_feedback,
        finance,
        nlp,
        ctpost,
        biomedTest,
        cmbTest
    }

    private static void biomedRequest() throws Exception
    {

        // 医疗测试
                String bioUrl = "http://218.77.58.169:8081/biomedicine-web/test";
//        String bioUrl = "http://localhost:8082/biomedicine-web/test";
        bioBatchQuery("//home//hpre//北大数据DRUG_NAME", bioUrl); //北大数据DRUG_NAME
        long query1_end = System.currentTimeMillis();

        long query2_st = System.currentTimeMillis();
        JSONObject obj = new JSONObject();
        obj.put("personid", "100232");
        obj.put("orderid", "2541234");
        obj.put("doctorid", "6345");
        obj.put("deptid", "0098");
        obj.put("ordertime", "20160123120909");
        obj.put("content", "||肝功六项[急]||白蛋白TP[急诊]/项");//今日禁食外阴血肿切开引流术  //CIN@III
        obj.put("specimen", "");
        obj.put("type", "order");
        query(obj.toString(), bioUrl);
        long query2_end = System.currentTimeMillis();
        System.out.print("query用时：");
        System.out.println(query2_end - query2_st);
    }

    static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Type t = Type.cmbTest;//biomedicine_query  //nlp  //biomedTest //ctpost //cmbTest

//        FileWriter fileWriter2 = new FileWriter("/home/hpre/program/xiangya/222.txt",true);

        switch (t) {
            case biomedicine_query: {
                for (int i = 0; i < 3; i++) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                biomedRequest();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
            }

            case onlybiomedicine_query: {
                // 医疗测试
                String bioUrl = "http://218.77.58.169:8081/biomedicine-web/test";
//        String bioUrl = "http://localhost:8082/biomedicine-web/test";
                bioBatchQuery("//home//hpre//testdata", bioUrl); //北大数据DRUG_NAME
                long query1_end = System.currentTimeMillis();

                long query2_st = System.currentTimeMillis();
                JSONObject obj = new JSONObject();
                obj.put("personid", "100232");
                obj.put("orderid", "2541234");
                obj.put("doctorid", "6345");
                obj.put("deptid", "0098");
                obj.put("ordertime", "20160123120909");
                obj.put("content", "");//今日禁食外阴血肿切开引流术  //CIN@III
                obj.put("specimen", "");
                obj.put("type", "order");
                query(obj.toString(), bioUrl);
                long query2_end = System.currentTimeMillis();
                System.out.print("query用时：");
                System.out.println(query2_end - query2_st);
                break;
            }

//            case biomedicine_feedback: {
//                String url = "http://localhost:8080/BiomedicineWeb/api/feedback";
//                FeedbackRecord record = new FeedbackRecord();
//
//                record.content = "[嘱托]定于明日于硬膜外麻醉下行痔切除术";
//                record.addItem("手术", "痔切除术");
//                record.addItem("麻醉类型", "硬膜外麻醉");
//
//                query(mapper.writeValueAsString(record), url);
//                break;
//            }

            case finance:
                // 金融测试
                String finaUrl = "http://218.77.58.183:8081/finance-web/api";
//			String finaUrl = "http://localhost:8080/finance-web/api";
//			finBatchQuery("C:\\Users\\Ring\\Desktop\\b.txt", finaUrl);

                JSONObject obj = new JSONObject();
                String name = "潜山县第五建筑有限责任公司";
                obj.put("auditBodyName", createJSONArray("安徽同盛会计师事务所有限公司", "海口佳衡会计师事务所", "珠海市华诚会计师事务有限公司"));
                obj.put("dishonest_executor", "李文军");
                obj.put("dishonest_executor_ID", "450321197505172072");
                obj.put("companyName", name);
                obj.put("projName", "某某楼盘");

                query(obj.toString(), finaUrl);

                break;

            case ctpost: {
                // 金融测试
                String url = "http://0.0.0.0:9009/api/ctpost"; //Com  jd

                String s = "（一）肺部：双肺血管-支气管束增多，左上肺及双下肺可见斑片状高密度灶，边界欠清，余肺未见明显主质性病灶，气管-叶段支气管通畅，纵膈内未见明显肿大淋巴结，双侧胸腔可见明显积液，心包腔内可见少量积液。\n" +
                        "（二）腹部：肝脏体积缩小，边缘欠光整，肝裂增宽，各叶比例失调，肝实质内未见明显异常密度灶及强化灶，肝内外胆管未见明显扩张，肝门静脉不宽，其内未见明显充盈缺损影。\n" +
                        "胆囊较大，囊壁均匀稍增厚，强化较明显，胆囊腔内未见明显异常密度灶及强化灶。\n" +
                        "脾脏及胰腺大小、形态、密度正常。\n" +
                        "双肾、双侧输尿管未见明显异常。\n" +
                        "膀胱充盈欠佳，膀胱腔内未见明显异常密度灶及强化灶。\n" +
                        "直肠、结肠各段管壁可见条状、环状高密度影，边界欠清，CT值约161HU，增强扫描无强化。\n" +
                        "腹腔内可见大量积液，盆腔区域可见多发淋巴结钙化影，其余未见明显肿大淋巴结。睾丸鞘膜积液。";
//                Scanner sca = new Scanner(new File("/home/hpre/xiangya/views/20"));
//                FileWriter fileWriter = new FileWriter("/home/hpre/xiangya/规则记录",true);
//                while(sca.hasNext()) {
//                    s = s+sca.nextLine();
//                }
//                sca.close();

                String query = query(s, url);
                System.out.println(query);
//                fileWriter.write(query);
//                fileWriter.flush();
//                fileWriter.close();

                break;
            }

            case cmbTest: {
                // 招行授信报告测试
//                String url = "http://218.77.58.173:23333/api/cmb";
<<<<<<< HEAD
                String url = "http://h133:12000/api/exchange";
//                String url = "http://0.0.0.0:22222/api/cmb";
=======
//                String url = "http://0.0.0.0:12000/api/exchange";
                String url = "http://0.0.0.0:23333/api/cmb";
>>>>>>> 为拉代码而提交
//                String url = "http://0.0.0.0:5002/cmb"; // 218.77.58.173
//                String url = "http://192.168.2.6:5002/cmb";
                int tag = 1;

                if (tag == 1)
                {
<<<<<<< HEAD
                    String s =
                            "人民共和国国家赔偿法";
                    // "经审议，同意续做北京旅游商品集团总公司授信，金额全折人民币约135955万元，期限1年，其中：北京欧亚高科新能源科技有限公司人民币9955万元，哈尔滨市阿明对青牧业有限公司有限公司全折人民币12.2亿元，天津天美味国际贸易有限公司人民币0.4亿元。要求：\n" +
=======
                    String s = "\t根据总行2012年6月15日对中华控股投资公司集团授信审批意见，我行给予中华控股投资公司集团授信额度350亿元。目前已经切分境内分行承办额度187.614亿元。\n" +
                            "该项目为原有项目授信续期，额度不变，分行单户权限。请执行分行审批意见。\n" +
                            "附分行审批意见如下：\n" +
                            "\t同意给予中国神州设备进出口（集团）总公司综合授信额度人民币4亿元，期限1年，信用方式，可用于办理流动资金贷款、银行承兑汇票、国内信用证、贸易融资、境内外履约类保函业务，其中：流动资金贷款、银行承兑汇票和国内信用证合计使用不超过人民币2.5亿元；下属文成国际贸易公司及北京文成海运进出口有限公司可占用该额度，下属公司占用时由中国神州设备进出口（集团）总公司提供连带担保责任。\n" +
                            "放款必要前提条件： \n" +
                            "\t1、文成国际贸易公司占用时流贷、承兑和国内信用证合计使用不超过人民币1000万元，北京文成海运进出口有限公司占用时流贷、承兑和国内信用证合计使用不超过人民币500万元；下属子公司若占用额度提款，需密切关注企业经营及财务情况，加强贷后检查；  \n" +
                            "\t2、北京文成海运进出口有限公司办理贸易融资业务及境内外履约类保函业务须严格审查业务背景，仅限用于进口消防设备及消防车等；  \n" +
                            "\t3、对办理大型成套设备出口项目及对外承包工程项目需要使用我行授信前，需严格按照国家四部委联合下发的《关于进一步加强大型成套设备出口项目及对外承包工程项目有关融资管理的通知》（商产发〈2007〉92号）的要求，对额度内出帐的业务进行逐笔审核，落实相关要求后方可办理融资业务；  \n" +
                            "\t4、关注国际政治经济区域形势的变化，额度项下业务不得用于总行明文禁止介入的国家或地区；  \n" +
                            "\t5、流动资金贷款仅限用于流动资金周转，不得用于股权投资、固定资产投资及进入资本市场，不得短贷长用；  \n" +
                            "\t6、原有授信项下余额纳入该额度管理。 \n" +
                            "贷后风险管理要求：\n" +
                            "\t1、加强贷后管理，关注企业经营情况及宏观经济境变化、人民币升值等因素对申请人及其下属公司的影响，若有异常，及时上报分行，调整我行授信方案。  \n" +
                            "\t2、本授信额度纳入国家开发投资公司在总行战略类集团授信，统一管理。\n" +
                            "\t\t\t\t\t\t\t\t"; // "经审议，同意续做北京旅游商品集团总公司授信，金额全折人民币约135955万元，期限1年，其中：北京欧亚高科新能源科技有限公司人民币9955万元，哈尔滨市阿明对青牧业有限公司有限公司全折人民币12.2亿元，天津天美味国际贸易有限公司人民币0.4亿元。要求：\n" +
>>>>>>> 为拉代码而提交
//                            "1、分行须严格落实新增特批人民币1.7亿元授信的9项要求，不得有误，确保专款专用，两条船出售后及时收回贷款。\n" +
//                            "2、密切关注借款人经营、管理和资本市场变化，一旦有不利于我行信贷权益的情况发生，及时停止授信提用，采取法律措施。"+
//                            "重点关注公司与下游客户合作情况、股票投资及对外股权投资情况变动。";
//                    String s = "同意给予南通中岐时装有限公司及其成员单位集团授信全折人民币200亿元，期限2年，其中贷款类额度（含固定资产贷款、流动资金贷款，风险程度系数等同于流动资金贷款的其他业务品种以及其他需要占用贷款额度的业务品种）合计不超过人民币150亿元，余额用于非融资类保函及国际贸易融资。\n" +
//                            "要求：\n" +
//                            "1、本次总行战略客户部统计的总、分行已批复生效的各类授信额度，均按原审批条件纳入本集团授信管理（原有额度的续期须另行履行审批程序）；并由总行战略客户部根据授信主体的收益贡献度及下述要求进行额度压缩。\n" +
//                            "2、本授信批复后，如增加其他应纳入集团授信管理的已批复生效额度，报经总行战略客户部进行额度切分后纳入集团授信管理。\n" +
//                            "3、本授信期内，在集团授信总额及贷款类额度范围内，原有授信主体的续期及增额、新增授信主体、其他纳入集团授信管理的业务，按以下要求办理：\n" +
//                            "（1）原有单一授信主体的额度续期，符合简单续期条件的，由分行自行审批。\n" +
//                            "（2）新增授信主体及原有授信主体增额，如导致分行承办的集团所属各级企业可循环使用的授信额度合计超过该分行地区性集团客户统一授信总量授权额度2倍的，报总行授信审批部审批；水泥板块管理控股平台企业（上年度本部报表收入规模低于合并报表收入规模10%的）增加授信额度（含新增客户），报总行授信审批部审批；其余由分行在单户权限内审批。\n" +
//                            "（3）分行权限内终批的授信额度，须优选授信主体，合理确定贷款额度及期限，审慎对待中长期流贷需求及信用方式授信；涉及水泥、玻璃等产能过剩行业的授信主体，须加强对区域行业市场状况的分析，遵循行业相关规定和我行准入要求。\n" +
//                            "（4）分行权限内终批的授信额度（含简单续期业务），应先由分行完成授信审批程序，再报总行战略客户部核准同意后生效，并同时报备总行授信审批部；报总行授信审批部审批的业务，须先报总行战略客户部进行额度管理。\n" +
//                            "4、严格办理备用额度的启用核准，控制虚增授信额度；老客户原授信使用率未达50％的，原则上不得扩大授信额度。\n" +
//                            "5、分行须加强对单一授信主体的管理，适度压缩过剩行业原有的授信额度，严格监督贷款额度合规使用，关注企业合理资金需求及短借长用现象；期限超过1年的可循环授信额度，须由分行逐年剑ê笪囊呀马凌云";
                    query(s.toString(), url);
                }
                else
                {
                    String dir = "/home/hpre/program/cmb/280份全文/";
                    File fDir = new File(dir);
                    File[] files = fDir.listFiles();
                    for (File file : files) {
                        System.out.println(file.getAbsolutePath());
                        StringBuffer s = new StringBuffer();
//                Scanner sca = new Scanner(new File("/home/hpre/program/cmb/200份授信报告红色部分/7"));
                        Scanner sca = new Scanner(file);
                        while(sca.hasNext()) {
                            if (s.toString().equals(""))
                                s.append(sca.nextLine());
                            else
                                s.append("\n"+sca.nextLine());
                        }
                        query(s.toString(), url);
                        sca.close();
                    }
                }

                break;
            }

            case biomedTest: {
                // 金融测试
			String url = "http://localhost:5110/legalNotice";
            JSONObject jsonObject = new JSONObject();
            String text = "岳爱英、牛日林：本院受理的河南诺德投资担保股份有限公司申请执行河南恒升房地产开发有限公司、石保岗、牛海红、你们一案，本院拟评估、拍卖你们名下的房产，现向你们公告送达本院(2015)郑执一字第884号执行裁定书。自公告之日起60日内来本院领取该执行裁定书，并办理评估、拍卖的相关手续，逾期则视为送达。";
                String case_num = "(2015)郑执一字第884号";
            jsonObject.put("text", text);
            jsonObject.put("case_num", case_num);
			query(jsonObject.toString(), url);

            }
            default:
                break;
        }
    }

    public static JSONArray createJSONArray(Object... objs) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < objs.length; i++) {
            jsonArray.put(i, objs[i]);
        }

        return jsonArray;
    }

    public static void printFile(String s, String outPath, Boolean append) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outPath, append);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(s);
            bufferedWriter.close();
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void finBatchQuery(String path, String url) throws JSONException, ClientProtocolException, IOException, InterruptedException {
        File[] files = null;
        File file = new File(path);

        if (file.isDirectory()) {
            files = file.listFiles();
        } else {
            files = new File[1];
            files[0] = file;
        }
        int count = 0;
        for (File f : files) {
            Scanner scanner = new Scanner(f);
            System.out.println(f);

            while (scanner.hasNextLine()) {
                JSONObject obj = new JSONObject();
                String name = scanner.nextLine().trim();

                System.out.println(name);
                obj.put("auditBodyName", createJSONArray("安徽同盛会计师事务所有限公司", "海口佳衡会计师事务所", "珠海市华诚会计师事务有限公司"));
                obj.put("dishonest_executor", "李文军");
                obj.put("dishonest_executor_ID", "450321197505172072");
                obj.put("companyName", name);
                obj.put("projName", "某某楼盘");

                count++;
                System.out.println(count);

                query(obj.toString(), url);
//    			Thread.sleep(1000);
            }

            scanner.close();
        }
    }

    public static void bioBatchQuery(String path, String url)
            throws JSONException, IOException {
        File[] files = null;
        File file = new File(path);

        if (file.isDirectory()) {
            files = file.listFiles();
        } else {
            files = new File[1];
            files[0] = file;
        }
        FileWriter fileWriter3 = new FileWriter("/home/hpre/222.txt",false);
        List<String > list = new ArrayList<>();
        int count = 0;
        for (File f : files) {
            Scanner scanner = new Scanner(f);
//        	System.out.println(f);
            while (scanner.hasNextLine()) {
                JSONObject obj = new JSONObject();
                String line = scanner.nextLine();
                obj.put("personid", "100232");
                obj.put("orderid", "2541234");
                obj.put("content", " "+line+" ");
                obj.put("doctorid", "6345");
                obj.put("deptid", "0098");
                obj.put("ordertime", "20160123120909");
                obj.put("specimen", "");
                obj.put("type", "order");
//    			obj.put("insno", "22");

                count++;
//                System.out.println(count);

                try {
//					System.out.println("obj.toString()="+obj.toString());
//					System.out.println("query(obj.toString(), url)="+query(obj.toString(), url));

//                    Advice advice = mapper.readValue(query(obj.toString(), url), Advice.class);

//                    System.out.print(line + "\t");
//                    System.out.println(advice.getNames());
//                    fileWriter3.write("请求："+line+"\n");
//                    fileWriter3.write("结果："+advice+"\n");
//                    list.add(advice.getNames()+"\t"+line);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//    			String str = advice.getOriginContent() + "\t\t" + advice.getNames() + "\n";
//    			printFile(str, "/home/ring/Desktop/compare.txt", true);
            }

            scanner.close();
//            Collections.sort(list);
//            for (String s : list)
//            {
//                fileWriter3.write(s+"\n");
//            }
            fileWriter3.flush();
            fileWriter3.close();
        }
    }

    public static String query(String content, String url) throws JSONException, ClientProtocolException, IOException {
        long qTest_st = System.currentTimeMillis();


        CloseableHttpClient httpclient = HttpClients.createDefault();

        long qTest_end = System.currentTimeMillis();

        StringBuffer sb = new StringBuffer();
//        FileWriter fileWriter=new FileWriter(new File("/home/hadoop/wnd/usr/cmb/招行程序运行结果.txt"),true);
        try {
            HttpPost httpPost = new HttpPost(url);
            HttpEntity entity = new ByteArrayEntity(content.getBytes());

            httpPost.setEntity(entity);
//            httpPost.setHeader("type","0");

            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {
//                System.out.println("提交返回的状态:"+response.getStatusLine());
                HttpEntity entity2 = response.getEntity();

                BufferedReader reader = new BufferedReader(new InputStreamReader(entity2.getContent()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
//                	fileWriter.write(line+"\n");
                    sb.append(line+"\n");
                }
                EntityUtils.consume(entity2);
            } finally {
//            	fileWriter.flush();
//            	fileWriter.close();
                response.close();
            }
        } finally {
            httpclient.close();
        }
//        System.out.print("qTest:");
//        System.out.println(qTest_end - qTest_st);

        return sb.toString();
    }

}
