package com.example.simplemusic.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    //格式化歌曲时间
    public static String formatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        Date data = new Date(time);
        return dateFormat.format(data);
    }
}