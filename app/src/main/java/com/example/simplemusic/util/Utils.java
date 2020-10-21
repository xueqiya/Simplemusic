package com.example.simplemusic.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    // 获取本地音乐封面图片
    public static Bitmap getLocalMusicBmp(ContentResolver res, String musicPic) {
        InputStream in;
        Bitmap bmp = null;
        try {
            Uri uri = Uri.parse(musicPic);
            in = res.openInputStream(uri);
            BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
            bmp = BitmapFactory.decodeStream(in, null, sBitmapOptions);
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    //格式化歌曲时间
    public static String formatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        Date data = new Date(time);
        return dateFormat.format(data);
    }
}