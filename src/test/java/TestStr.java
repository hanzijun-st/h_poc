import com.qianlima.offline.util.StrUtil;

public class TestStr {
    public static void main(String[] args) {
       /* boolean empty = StrUtil.isEmpty("    ");
        boolean empty1 = StrUtil.isEmpty("                   ");
        boolean empty2 = StrUtil.isEmpty("1                   2");
        boolean empty3 = StrUtil.isEmpty("      0         ");
        System.out.println(empty);
        System.out.println(empty1);
        System.out.println(empty2);
        System.out.println(empty3);
        String str = "      0         ";
        String str2="1                   2";
        str2 = str2.replace(" ","");
        CharSequence trim = StrUtil.trim(str2);

        String strs = "  j s   ls" +
                "sj " +
                "ljsj " +
                "              sl";

        System.out.println("=====: "+StrUtil.delAllPlace(strs));*/

      /* String str ="123:";
        String[] split = str.split(":");
        System.out.println(split.length);*/

      String s ="的房价肯定JFK的... hhdfhjdf";
      if (s.contains("...")){
          System.out.println(true);
      }
    }
}