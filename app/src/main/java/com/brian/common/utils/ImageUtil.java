package com.brian.common.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/**
 * ImageUtil
 * <ul>
 * convert between Bitmap, byte array, Drawable
 * <li>{@link #bitmapToByte(Bitmap)}</li>
 * <li>{@link #bitmapToDrawable(Bitmap)}</li>
 * <li>{@link #byteToBitmap(byte[])}</li>
 * <li>{@link #byteToDrawable(byte[])}</li>
 * <li>{@link #drawableToBitmap(Drawable)}</li>
 * <li>{@link #drawableToByte(Drawable)}</li>
 * </ul>
 * <ul>
 * get image
 * scale image
 * <li>{@link #scaleImageTo(Bitmap, int, int)}</li>
 * <li>{@link #scaleImage(Bitmap, float, float)}</li>
 * </ul>
 * 
 * @author Trinea 2012-6-27
 */
public class ImageUtil {

    /**
     * convert Bitmap to byte array
     * 
     * @param b
     * @return
     */
    public static byte[] bitmapToByte(Bitmap b) {
        if (b == null) {
            return null;
        }

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }

    /**
     * convert byte array to Bitmap
     * 
     * @param b
     * @return
     */
    public static Bitmap byteToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    /**
     * convert Drawable to Bitmap
     * 
     * @param d
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable d) {
        return d == null ? null : ((BitmapDrawable)d).getBitmap();
    }

    /**
     * convert Bitmap to Drawable
     * 
     * @param b
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Drawable bitmapToDrawable(Bitmap b) {
        return b == null ? null : new BitmapDrawable(b);
    }

    /**
     * convert Drawable to byte array
     * 
     * @param d
     * @return
     */
    public static byte[] drawableToByte(Drawable d) {
        return bitmapToByte(drawableToBitmap(d));
    }

    /**
     * convert byte array to Drawable
     * 
     * @param b
     * @return
     */
    public static Drawable byteToDrawable(byte[] b) {
        return bitmapToDrawable(byteToBitmap(b));
    }

    /**
     * scale image
     * 
     * @param org
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
        return scaleImage(org, (float)newWidth / org.getWidth(), (float)newHeight / org.getHeight());
    }

    /**
     * scale image
     * 
     * @param org
     * @param scaleWidth sacle of width
     * @param scaleHeight scale of height
     * @return
     */
    public static Bitmap scaleImage(Bitmap org, float scaleWidth, float scaleHeight) {
        if (org == null) {
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(), matrix, true);
    }

    public static final class InputStreamAndLength {
        public InputStream stream;
        public int length;
    }

    public static boolean checkImageFileExist(File imageFile) {
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()
                || imageFile.length() <= 0) {
            return false;
        }
        Options opts = new Options();
        opts.inSampleSize = 4;
        opts.inPreferredConfig = Config.ALPHA_8;
        Bitmap b = null;
        for (int i = 0; i < 3; i++) {
            b = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opts);
            if (b != null) {
                b.recycle();
                return true;
            } else {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }// end for
        return false;
    }

    public static boolean checkImageFileExist(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return false;
        }
        return checkImageFileExist(new File(imagePath));
    }

    public static Params getImageParams(File imageFile) {
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()
                || imageFile.length() <= 0) {
            LogUtil.v("imageFile is not exist");
            return null;
        }
        Options opts = new Options();
        Bitmap sourceBp = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),
                opts);
        return getImageParams(sourceBp);
    }

    public static Params getImageParams(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return null;
        }
        return getImageParams(new File(imagePath));
    }

    public static Params getImageParams(Bitmap bitmap) {
        if (bitmap == null) {
            LogUtil.v("bitmap=null");
            return null;
        }
        Params params = new Params();
        params.width = bitmap.getWidth();
        params.height = bitmap.getHeight();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return params;
    }
    static class Params {
        public int width;
        public int height;
    }
}


