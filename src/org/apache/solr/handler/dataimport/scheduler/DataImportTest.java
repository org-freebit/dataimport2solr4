package org.apache.solr.handler.dataimport.scheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * ClassName:DataImportTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:  TODO ADD REASON. <br/>
 * Date:     2017-12-30 14:42 <br/>
 *
 * @author zhongli
 * @see
 * @since JDK 1.8
 */
public class DataImportTest {

    public static void main(String[] args) {
        // get a calendar to set the start time (first run)
        Calendar calendar = Calendar.getInstance();

        // set the first run to now + interval (to avoid fireing while the
        // app/server is starting)
        calendar.add(Calendar.MINUTE, 1);
        Date startTime = calendar.getTime();
        long time1 = startTime.getTime();
        long time2 = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(startTime));
        System.out.println(time1);
        System.out.println(time2);
        System.out.println(time1-time2);
    }
}
