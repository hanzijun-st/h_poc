import com.qianlima.offline.util.LogUtils;

import java.io.IOException;
import java.util.List;

public class TestAdd {
    public static void main(String[] args) {
        try {
            List<String> bjDatasD = LogUtils.readRule("bjDatasD");
            int i =0;
            for (String s : bjDatasD) {
                String[] split = s.split(":");
                String s1 = split[1];
                i+=Integer.valueOf(s1);
                //System.out.println(s);
            }
            System.out.println(i);
        } catch (IOException e) {

        }
    }
}