package com.gcrm.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Test {

    /**
     * One-liner on the function
     * 
     * @param args
     */
    public static void main(String[] args) {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.set(Calendar.DAY_OF_MONTH,
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        SimpleDateFormat simpleFormate = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(simpleFormate.format(calendar.getTime()));

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int mondayPlus = 0;
        if (dayOfWeek == 1) {
            mondayPlus = 0;
        } else {
            mondayPlus = 1 - dayOfWeek;
        }
        calendar.add(GregorianCalendar.DATE, mondayPlus);
        Date firstDay = calendar.getTime();

        System.out.println(simpleFormate.format(firstDay));
        calendar.add(Calendar.DATE, 35);
        Date lastDay = calendar.getTime();
        System.out.println(simpleFormate.format(lastDay));
    }

}
