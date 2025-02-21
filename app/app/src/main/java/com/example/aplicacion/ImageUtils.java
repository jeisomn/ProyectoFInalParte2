package com.example.aplicacion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils {
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}