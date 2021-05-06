import java.util.ArrayList;
import java.util.List;

public class TestEveryOneParam {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        int j=0;
        for (int i = 1; i <=1000 ; i++) {
            list.add(1);
            if (i % 100 ==0){
                j= j+1;
                System.out.println("第"+j+"次"+i);
            }
        }
    }
}