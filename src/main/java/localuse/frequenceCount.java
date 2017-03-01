package localuse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by hpre on 16-12-19.
 */
public class frequenceCount {
    private static String input = "/home/hpre/else/文档/frequence2";

    public static void main(String[] args) {
        count1();
//        count2();
//        count3();
    }

    private static void count1()
    {
        //        List<String> strList = new ArrayList<>();
        int count = 1;
        String string = "";
        try {
            Scanner scanner = new Scanner(new File(input));
            while (scanner.hasNext())
            {
                String strLine = scanner.nextLine();
                if (string.equals(strLine))
                {
                    count++;
                }
                else
                {
                    System.out.println(string+"\t"+count);
                    count = 1;
                    string = strLine;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void count2()
    {
        String dirIn = "/home/hpre/Workspaces/MyEclipse 2015/cmbPython/redLabeled/";
        String dirOut = "/home/hpre/";
        File[] fileIns = new File(dirIn).listFiles();
        String biaozhu = "";
        for (File fileIn : fileIns) {
            try {
                Scanner scanner = new Scanner(fileIn);
                System.out.println(fileIn);
                StringBuffer sb = new StringBuffer();
                while (scanner.hasNext())
                {
                    String strLine = scanner.nextLine();
                    String[] spaceSplit = strLine.split(" ");
                    for (String eachSpaceWord : spaceSplit) {
                        if (eachSpaceWord.startsWith("#O#"))
                        {
                            if (eachSpaceWord.endsWith("#O#"))
                            {
                                String wordPos = eachSpaceWord.substring(3,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                System.out.println(slashSplit[0]);
                            }
                            else
                            {
                                biaozhu = "O";
                                String wordPos = eachSpaceWord.substring(3);
                                String[] slashSplit = wordPos.split("/");
                                sb.append(slashSplit[0]);
                            }
                        }
                        else
                        {
                            if (eachSpaceWord.endsWith("#O#"))
                            {
                                String wordPos = eachSpaceWord.substring(0,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                sb.append(slashSplit[0]);
                                System.out.println(sb.toString());
                                sb.delete(0,sb.length());
                                biaozhu = "";
                                continue;
                            }
                            if (biaozhu.equals("O"))
                            {
                                String[] slashSplit = eachSpaceWord.split("/");
                                sb.append(slashSplit[0]);
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void count3()
    {
        String dirIn = "/home/hpre/Workspaces/MyEclipse 2015/cmbPython/redLabeled/";
        String dirOut = "/home/hpre/";
        File[] fileIns = new File(dirIn).listFiles();
        for (File fileIn : fileIns) {
            try {
                Scanner scanner = new Scanner(fileIn);
//                System.out.println(fileIn);
                StringBuffer Osb = new StringBuffer();
                StringBuffer cSB = new StringBuffer();
                while (scanner.hasNext())
                {
                    String Obiaozhu = "";
                    String Cbiaozhu = "";
                    String C = "";
                    String strLine = scanner.nextLine();
                    if (!strLine.contains("#O#"))
                        continue;
                    String[] spaceSplit = strLine.split(" ");
                    for (String eachSpaceWord : spaceSplit) {
                        if (eachSpaceWord.startsWith("#C#"))
                        {
                            if (eachSpaceWord.endsWith("#C#"))
                            {
                                String wordPos = eachSpaceWord.substring(3,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                C = slashSplit[0];
                            }
                            else
                            {
                                Cbiaozhu = "C";
                                String wordPos = eachSpaceWord.substring(3);
                                String[] slashSplit = wordPos.split("/");
                                cSB.append(slashSplit[0]);
                            }
                        }
                        else
                        {
                            if (eachSpaceWord.endsWith("#C#"))
                            {
                                String wordPos = eachSpaceWord.substring(0,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                cSB.append(slashSplit[0]);
                                C = cSB.toString();
                                cSB.delete(0,cSB.length());
                                Cbiaozhu = "";
                                continue;
                            }
                            if (Cbiaozhu.equals("C"))
                            {
                                String[] slashSplit = eachSpaceWord.split("/");
                                cSB.append(slashSplit[0]);
                            }
                        }

                        if (eachSpaceWord.startsWith("#O#"))
                        {
                            if (eachSpaceWord.endsWith("#O#"))
                            {
                                String wordPos = eachSpaceWord.substring(3,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                System.out.println(C + "\t" +slashSplit[0]);
                            }
                            else
                            {
                                Obiaozhu = "O";
                                String wordPos = eachSpaceWord.substring(3);
                                String[] slashSplit = wordPos.split("/");
                                Osb.append(slashSplit[0]);
                            }
                        }
                        else
                        {
                            if (eachSpaceWord.endsWith("#O#"))
                            {
                                String wordPos = eachSpaceWord.substring(0,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                Osb.append(slashSplit[0]);
                                System.out.println(C + "\t" +Osb.toString());
                                Osb.delete(0,Osb.length());
                                Obiaozhu = "";
                                continue;
                            }
                            if (Obiaozhu.equals("O"))
                            {
                                String[] slashSplit = eachSpaceWord.split("/");
                                Osb.append(slashSplit[0]);
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
