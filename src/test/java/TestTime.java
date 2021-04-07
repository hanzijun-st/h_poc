import com.qianlima.offline.util.DateUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TestTime {
    public static void main(String[] args){

       /* //方法 一
        long l = System.currentTimeMillis();
        //方法 二
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        //方法 三
        long time = new Date().getTime();

        System.out.println(getTime(l));
        System.out.println(getTime(timeInMillis));
        System.out.println(getTime(time));*/
        /*try {
            System.out.println(getTimesmorning());
            System.out.println(getTimesnight());
        } catch (Exception e) {
            e.getMessage();
        }*/
        Date afterDate = getAfterDate(30);//30分钟过期)
        System.out.println(afterDate);
    }

    //获得当天0点时间

    public static int getTimesmorning() throws Exception{
        Calendar cal = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = sdf.parse("2021-01-01");
        cal.setTime(parse);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        cal.set(Calendar.SECOND, 0);

        cal.set(Calendar.MINUTE, 0);

        cal.set(Calendar.MILLISECOND, 0);

        return (int) (cal.getTimeInMillis()/1000);

    }

//获得当天24点时间

    public static long getTimesnight() throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = sdf.parse("2021-01-01");
        Calendar cal = Calendar.getInstance();
        cal.setTime(parse);

        cal.set(Calendar.HOUR_OF_DAY, 23);

        cal.set(Calendar.SECOND, 59);

        cal.set(Calendar.MINUTE, 59);

        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTimeInMillis()/1000;

    }

    public static Date getAfterDate(int minute){

        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date());

        if(minute != 0){
            cal.add(Calendar.MINUTE, minute);
        }

        return cal.getTime();
    }
}