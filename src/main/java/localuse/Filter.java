package localuse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by hpre on 16-12-16.
 */
public class Filter {
    private static String input = "/";
    private static String out = "";
    public static void main(String[] args) {

    }
    private static void deal()
    {
        File dirPath = new File(input);
        File[] files = dirPath.listFiles();
        FileWriter fileWriter = null;
        for (File file : files)
        {
            try {
                Scanner scanner = new Scanner(file);
                fileWriter = new FileWriter(new File(input));
                while (scanner.hasNext())
                {
                    String strLine = scanner.nextLine();

                }
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
