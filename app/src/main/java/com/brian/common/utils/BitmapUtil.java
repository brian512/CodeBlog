/**
 * Copyright (C) 2014 Togic Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brian.common.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author jar @date 2014年6月30日
 */
public class BitmapUtil {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    public static Bitmap decodebitmap(String url, float w, float h) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int scal = 0;
        int scal1 = 0;
        int scal2 = 0;
        if (options.outHeight > options.outWidth) {
            scal1 = (int) (options.outHeight / h);
        } else {
            scal2 = (int) (options.outWidth / w);
        }
        if (scal1 >= scal2)
            scal = scal1;
        else
            scal = scal2;
        options.inSampleSize = scal;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(url, options);
    }

    public static Bitmap decodeBitmap(String url, boolean isFullScreen) {
        return decodeBitmap(url, isFullScreen, WIDTH, HEIGHT);
    }

    public static Bitmap decodeBitmap(String url, boolean isFullScreen,
            float w, float h) {
        Bitmap bitmapOrg = decodebitmap(url, w, h);
        if (bitmapOrg == null)
            return null;
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        float scaleWidth = 0;
        float scaleHeight = 0;
        if (isFullScreen) {
            scaleWidth = w / width;
            scaleHeight = h / height;
        } else {
            float widthScale = w / width;
            float heightScale = h / height;
            if (widthScale <= heightScale) {
                scaleWidth = widthScale;
                scaleHeight = widthScale;
            } else {
                scaleWidth = heightScale;
                scaleHeight = heightScale;
            }

        }
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        bitmapOrg = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix,
                true);
        if (bitmapOrg != null && !bitmapOrg.isRecycled())
            Log.i("BitmapUtil", "The bitmap width is " + bitmapOrg.getWidth()
                    + "  height is " + bitmapOrg.getHeight());
        return bitmapOrg;
    }

    public static Bitmap rotate(Bitmap bitmap, int rotate) {
        if (bitmap == null)
            return null;
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return bitmap;
    }

    public static Bitmap getImageWithRefReflect(Bitmap originalImage,
            float percent) {

        if (originalImage == null || originalImage.isRecycled() || percent < 0) {
            return null;
        }
        final int reflectionGap = 4;
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int refHeight = (int) (height * percent);

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height
                - refHeight, width, refHeight, matrix, false);
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                (height + refHeight), Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(originalImage, 0, 0, null);

        Paint defaultPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0,
                originalImage.getHeight(), 0, bitmapWithReflection.getHeight()
                        + reflectionGap, 0x66000000, 0x00000000, TileMode.MIRROR);

        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
                + reflectionGap, paint);

        return bitmapWithReflection;
    }

    public static Bitmap getReflectedImage(Bitmap originalImage, float percent) {
        if (originalImage == null || originalImage.isRecycled() || percent < 0) {
            return null;
        }
        int height = originalImage.getHeight();
        int refHeight = (int) (height * percent);
        return getReflectedImage(originalImage, refHeight, 4);
    }

    public static Bitmap getReflectedImage(Bitmap originalImage, int refHeight) {
        return getReflectedImage(originalImage, refHeight, 4);
    }

    /**
     * make sure refHeight < originalImage.getHeight()
     */
    public static Bitmap getReflectedImage(Bitmap originalImage, int refHeight, final int reflectionGap) {
        LogUtil.e("originalImage=" + originalImage);
        if (originalImage == null || originalImage.isRecycled() || refHeight < 0) {
            return null;
        }
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        if (refHeight > height) {
            refHeight = height;
        }

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height
                - refHeight, width, refHeight, matrix, false);
        Bitmap reflection = Bitmap.createBitmap(width, refHeight, Config.ARGB_8888);

        Canvas canvas = new Canvas(reflection);
        // 抗锯齿效果
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));

        Paint defaultPaint = new Paint();
        canvas.drawRect(0, 0, width, reflectionGap, defaultPaint);
        canvas.drawBitmap(reflectionImage, 0, reflectionGap, null);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        LinearGradient shader = new LinearGradient(0, 0, 0,
                refHeight, 0x66000000, 0x00000000, TileMode.CLAMP);
        paint.setShader(shader);
        // 取两层绘制交集,显示下层
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

        canvas.drawRect(0, 0, width, reflection.getHeight() + reflectionGap,
                paint);

        return reflection;
    }

    private static void changeLastModified(String path) {
        final File f = new File(path);
        if (f.exists()) {
            f.setLastModified(System.currentTimeMillis());
        }
    }

    /**
     * Load bitmap file from sd card.
     * 
     * @return The Bitmap object, the returned value may be null.
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (null == drawable) {
            return null;
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        return drawableToBitmap(drawable, width, height);
    }
    
    /**
     * Load bitmap file from sd card.
     *
     * @return The Bitmap object, the returned value may be null.
     */
    public static Bitmap drawableToBitmap(Drawable drawable, int width,
            int height) {
        if (null == drawable || width <= 0 || height <= 0) {
            return null;
        }

        Config config = (drawable.getOpacity() != PixelFormat.OPAQUE) ? Config.ARGB_8888
                : Config.RGB_565;

        Bitmap bitmap = null;

        try {
            bitmap = Bitmap.createBitmap(width, height, config);
            if (null != bitmap) {
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, width, height);
                drawable.draw(canvas);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }
    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * Get the size in bytes of a bitmap.
     * @return size in bytes
     */
    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static boolean writeBitmap(String savePath, Bitmap bt) {
        if (TextUtils.isEmpty(savePath) || bt == null || bt.isRecycled()) {
            LogUtil.e("writeBitmap failed");
            return false;
        }
        try {
            LogUtil.v("getByteCount=" + bt.getByteCount());
            File file = new File(savePath);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            FileOutputStream out = new FileOutputStream(file);
            if (bt.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                out.flush();
            } else {
                LogUtil.e("bitmap compress failed");
            }
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
