import com.qianlima.offline.util.LogUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TestBijiao {
    public static void main(String[] args) {
        try {
            List<String> noHave = new ArrayList<>();
            List<String> no = new ArrayList<>();//数据少的单独出来的数据
            List<String> bjDatasD = LogUtils.readRule("bjDatasD");//数据多的
            List<String> bjDatass = LogUtils.readRule("bjDatasS");//数据少的
            for (String datadd : bjDatasD) {
                if (bjDatass.contains(datadd)){
                    continue;
                }
                noHave.add(datadd);
            }
            System.out.println(noHave.toString());

            readFile(noHave);
            for (String datass : bjDatass) {
                if (bjDatasD.contains(datass)){
                    continue;
                }
                System.out.println("存在不同的数据："+no);
            }
        } catch (IOException e) {

        }
    }

    public static void readFile(List<String> strList) {
        File file = new File("C:/Users/Administrator/Desktop/wenjian/test.txt");

        if(file.exists()) {
            System.err.println("如果已存在test.txt的文件，将更新文件内容");
            file.delete();
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file),"GB2312");
                BufferedWriter bw = new BufferedWriter(osw);
                for (String s : strList) {
                    bw.write(s+"\r\n");
                }
                System.out.println("已完成100%，导入结束！");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}