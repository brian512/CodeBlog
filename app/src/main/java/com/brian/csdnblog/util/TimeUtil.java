
package com.brian.csdnblog.util;

import android.annotation.SuppressLint;

import com.brian.csdnblog.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 与业务相关的时间工具类
 */
@SuppressLint("SimpleDateFormat")
public class TimeUtil {
    /**
     * 每天的毫秒数
     */
    public static final long MILSEC_PER_DAY = 24 * 3600 * 1000;

    /**
     * 转换时间格式，格式yyyy-MM-dd HH:mm:ss
     */
    public static String convTimeDetail(long timeMS) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = new Date(timeMS);
        String sDateTime = sdf.format(dt);
        return sDateTime;
    }

    /**
     * 转换时间格式，格式yyyy-MM-dd HH:mm
     */
    public static String convTimeYMDHM(long timeMS) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dt = new Date(timeMS);
        String sDateTime = sdf.format(dt);
        return sDateTime;
    }

    /**
     * 转换时间格式，格式MM-dd HH:mm
     */
    public static String convTimeMDHM(long timeMS) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        Date dt = new Date(timeMS);
        String sDateTime = sdf.format(dt);
        return sDateTime;
    }

    /**
     * 转换时间格式，格式 HH:mm
     */
    public static String convTimeHM(long timeMS) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date dt = new Date(timeMS);
        String sDateTime = sdf.format(dt);
        return sDateTime;
    }

    /**
     * 剩余时间转换时间格式，格式HH:mm
     */
    public static String convTimeHourMin(long timeMS) {
        if (timeMS <= 0) {
            return "00:00";
        } else {
            int hour = (int) (timeMS / (60 * 60 * 1000));
            int minute = (int) ((timeMS / (60 * 1000)) % 60);

            String sHour;
            if (hour <= 0) {
                sHour = "00";
            } else if (hour < 10) {
                sHour = "0" + hour;
            } else {
                sHour = "" + hour;
            }

            String sMin;
            if (minute <= 0) {
                sMin = "00";
            } else if (minute < 10) {
                sMin = "0" + minute;
            } else {
                sMin = "" + minute;
            }

            return String.format("%s:%s", sHour, sMin);
        }
    }

    /**
     * 转换时间格式，格式yyyy-MM-dd
     */
    public static String convTime(long timeMS) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = new Date(timeMS);
        String sDateTime = sdf.format(dt);
        return sDateTime;
    }


    /**
     * 获得当前时间 HH:mm:ss
     *
     * @return
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * 转换时间格式，格式yyyy/MM/dd
     */
    public static String convTimeYMD(long timeMS) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date dt = new Date(timeMS);
        String sDateTime = sdf.format(dt);
        return sDateTime;
    }

    /**
     * 将指定格式(yyyy-MM-dd HH:mm:ss)的时间转换为Long
     *
     * @return Long 时间
     */
    public static long convTimeYmdhmsToLong(String dateTime) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lTime = 0;

        try {
            Date date = sdf.parse(dateTime);

            lTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return lTime;
    }

    /**
     * 获取两个日期相隔的天数
     * PS：oldDate的时间应该要比newDate早，否则会得出负数
     */
    public static int daysBetween(Date oldDate, Date newDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            oldDate = sdf.parse(sdf.format(oldDate));
            newDate = sdf.parse(sdf.format(newDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(oldDate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(newDate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 获取时间已经过去的天数,如timeMS距当前时间在一天内则返回1，在两天内则返回2，以此类推
     */
    public static int getPassDay(long timeMS) {
        long passTime = System.currentTimeMillis() - timeMS;
        final long SECOND_PER_DAY = 24 * 3600 * 1000;

        if (passTime > 0) {
            // 默认临界值处直接算多一天
            return (int) (passTime / SECOND_PER_DAY + 1);
        } else {
            return 0;
        }
    }

    /**
     * 根据生日时间字符串获取年龄
     *
     * @param timeString yyyy-MM-dd 格式的时间字符串
     */
    public static String getAge(String timeString) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = dateFormat.parse(timeString);
        } catch (Exception e) {
            date = null;
        }

        Date curDate = new Date(System.currentTimeMillis());

        if (date == null) {
            // 永远的18岁
            return "18";
        } else {
            int num = curDate.getYear() - date.getYear();

            return num + "";
        }
    }

    /**
     * 转换时间格式，pubTime以毫秒为单位 时间显示规则 t<5分钟：刚刚 5分钟≤t＜60分钟：t分钟前 60分钟≤t＜24小时：t小时前
     * 1天≤t＜3小时：t天前 t>=3天：系统时间 PS：瓶子loading页展示时间：以成功发送时间为准
     * PS：聊天页面的信息展示时间：以成功发送时间为准
     */
    public static String convTimeEx(long pubTimeMS) {
        // 特殊处理：如果时间小于0，则返回空
        if (pubTimeMS <= 0) {
            return ResourceUtil.getString(R.string.time_current);
        }

        long current = System.currentTimeMillis();
        long duration = current - pubTimeMS;
        int minute = (int) (duration / (60 * 1000));
        int hour = (int) (duration / (60 * 60 * 1000));
        int day = (int) (duration / (24 * 60 * 60 * 1000));

        // 如果当前时间错乱，或者pubTime非法导致当前时间比pubTime值小，则直接显示系统时间
        if (minute < 0) {
            return convTimeYMD(pubTimeMS);
        }

        // t<5分钟：刚刚
        if (minute < 5) {
            return ResourceUtil.getString(R.string.time_current);
        }

        // 5分钟≤t＜60分钟：t分钟前
        if (minute < 60) {
            return ResourceUtil.getString(R.string.time_before_minute);
        }

        // 60分钟≤t＜24小时：t小时前
        if (hour < 24) {
            return ResourceUtil.getString(R.string.time_before_hours);
        }

        // 1天≤t＜7天：t天前
        if (day < 7) {
            return ResourceUtil.getString(R.string.time_before_days);
        }

        // t>=7天：系统时间
        return convTimeYMD(pubTimeMS);
    }

    /**
     * 聊天索引时间转换
     *
     * @return
     */
    public static String convTimeForChatIndex(long curItemTime) {
        long current = System.currentTimeMillis();
        final long DAY = 1 * 24 * 60 * 60 * 1000;
        final long ONE_WEEK = 7;
        final long ONE_DAY = 1;

        long curDay = current / DAY;
        long curItemDay = curItemTime / DAY;

        // 间隔天数
        long spaceDay = curDay - curItemDay;

        String timeStr = "";
        if (spaceDay >= ONE_WEEK) {
            // 1周之外，显⽰时间格式为2015/1/27
            timeStr = TimeUtil.convTimeYMD(curItemTime);
        } else if (spaceDay > ONE_DAY) {
            // 1周之内，显⽰时间格式为 x days ago

            timeStr = ResourceUtil.getString(R.string.time_before_days, spaceDay);
        } else if (spaceDay > 0) {
            // 昨天消息，显示时间格式为 昨天15:23
            String hourMin = TimeUtil.convTimeHM(curItemTime);
            timeStr = ResourceUtil.getString(R.string.time_yesterday, hourMin);
        } else {
            // 今天消息 ，显示格式为 15:23
            String hourMin = TimeUtil.convTimeHM(curItemTime);
            timeStr = hourMin;
        }

        return timeStr;
    }

    /**
     * XX:XX：           当天
     * 昨天 XX:XX：      日期过了一天
     * 星期X XX:XX：     日期为一周之内，昨天之后
     * XXXX-XX-XX XX:XX： 一周之后
     */
    public static String convTimeForChat(long curItemTime) {
        long current = System.currentTimeMillis();
        final long DAY = 1 * 24 * 60 * 60 * 1000;
        final long ONE_WEEK = 7;
        final long ONE_DAY = 1;

        long curDay = current / DAY;
        long curItemDay = curItemTime / DAY;

        // 间隔天数
        long spaceDay = curDay - curItemDay;

        String timeStr = "";
        if (spaceDay > ONE_WEEK) {
            // 1周之外，显⽰时间格式为2015/1/27 15:23

            timeStr = TimeUtil.convTimeYMDHM(curItemTime);
        } else if (spaceDay > ONE_DAY) {
            // 1周之内，显⽰时间格式为 星期三15:23            

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            calendar.setTimeInMillis(curItemTime);
            final String[] WEEK_ARRAY = ResourceUtil.getStringArray(R.array.time_week_array);
            int index = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (index < 0) {
                index = 0;
            }
            index = index % 7;

            String week = WEEK_ARRAY[index];
            String hourMin = TimeUtil.convTimeHM(curItemTime);
            timeStr = week + " " + hourMin;
        } else if (spaceDay > 0) {
            // 昨天消息，显示时间格式为 昨天15:23

            String hourMin = TimeUtil.convTimeHM(curItemTime);
            timeStr = ResourceUtil.getString(R.string.time_yesterday, hourMin);
        } else {
            // 今天消息 ，显示格式为 15:23

            String hourMin = TimeUtil.convTimeHM(curItemTime);
            timeStr = hourMin;
        }

        return timeStr;
    }

    /**
     * N秒前：    Time < 1min
     * N分钟前：  1min <= Time < 1h
     * N小时前：  1h <= Time < 24h
     * N天前：    24h <= Time < 1星期
     * XXXX-XX-XX：Time > 1星期
     */
    public static String convTimeForPraise(long pubTime) {

        // 特殊处理：如果时间小于0，则返回空

        final int SEC = 1000;
        final int MIN = 60 * 1000;
        final int HOUR = 60 * 60 * 1000;
        final int DAY = 24 * 60 * 60 * 1000;
        long current = System.currentTimeMillis();

        int dSec = (int) (current / SEC - pubTime / SEC);
        if (dSec < 1) {
            return ResourceUtil.getString(R.string.time_current);
        }

        if (dSec >= 1 && dSec < 60) {
            return ResourceUtil.getString(R.string.time_before_second, dSec);
        }

        int dMin = (int) (current / MIN - pubTime / MIN);
        if (dMin >= 1 && dMin < 60) {
            return ResourceUtil.getString(R.string.time_before_minute, dMin);
        }

        int dHour = (int) (current / HOUR - pubTime / HOUR);
        if (dHour >= 1 && dHour < 24) {
            return ResourceUtil.getString(R.string.time_before_hours, dHour);
        }

        int dDay = daysBetween(new Date(pubTime), new Date(current));
        if (dDay >= 1 && dDay < 7) {
            return ResourceUtil.getString(R.string.time_before_days, dDay);
        }

        return convTimeYMD(pubTime);

    }

    /**
     * 时长转化为HH:MM:SS格式时间
     */
    public static String convTimeHMS(long timeMS) {
        long timeSec = timeMS / 1000;

        int hour = 0;
        int minute = 0;
        int second = 0;

        if (timeSec <= 0) {
            return String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
        }

        minute = (int) (timeSec / 60);
        hour = minute / 60;
        minute = minute - hour * 60;
        second = (int) (timeSec - hour * 60 * 60 - minute * 60);

        return String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
    }

    public static String convCountTime(long timeMS) {
        int timeSec = (int)(timeMS / 1000);

        if (timeSec <= 0) {
            return "0秒";
        }

        int minute, second, hour;

        if (timeSec < 60) {
            return String.format(Locale.CHINA, "%02d秒", timeSec);
        } else if (timeSec < 3600) {
            minute = timeSec / 60;
            second = timeSec % 60;
            return String.format(Locale.CHINA, "%02d分%02d秒", minute, second);
        } else {
            minute = timeSec / 60;
            hour = minute / 60;
            return String.format(Locale.CHINA, "%02小时%02d分", hour, minute);
        }
    }
}
