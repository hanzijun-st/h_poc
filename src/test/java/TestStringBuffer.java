import com.alibaba.fastjson.JSONObject;

public class TestStringBuffer {
    public static void main(String[] args) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("我爱你中国");
        buffer.append(",");
        buffer.append("站在海南看南海");

        JSONObject obj = new JSONObject();
        String s = obj.toJSONString("爱你中国");

        String t = s.replaceAll("\"","");

        System.out.println("t---"+t);
        if (buffer.toString().contains(t)) {
            System.out.println("true");
        }else{
            System.out.println("hahah");
        }

    }
}