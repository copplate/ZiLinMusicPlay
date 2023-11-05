package com.example.zilinmusicplay.util;

public class TimeUtil {
    public static String millToTimeFormat(int timeMills) {//毫秒值转时间
        int second = timeMills / 1000;
        int minute = second / 60;
        int lastSecond = second % 60;//时间的余数

        String secondStr = "";
        String minuteStr = "";

        if (lastSecond < 10) {
            secondStr = "0" + lastSecond;
        } else {
            secondStr = "" + lastSecond;
        }
        if (minute < 10) {
            minuteStr = "0" + minute;
        } else {
            minuteStr = "" + minute;
        }
        return minuteStr + ":" + secondStr;
    }
}
