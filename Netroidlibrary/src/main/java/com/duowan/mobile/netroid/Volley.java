package com.duowan.mobile.netroid;

import java.io.File;

import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.os.Build;

import com.duowan.mobile.netroid.cache.DiskCache;
import com.duowan.mobile.netroid.stack.HttpClientStack;
import com.duowan.mobile.netroid.stack.HttpStack;
import com.duowan.mobile.netroid.stack.HurlStack;
import com.duowan.mobile.netroid.toolbox.BasicNetwork;

public class Volley {

    public static final  String userAgent = "Mozilla/5.0 (compatible; MSIE 7.0; Windows NT 5.1)";
    
    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, int cacheSize) {
		int poolSize = RequestQueue.DEFAULT_NETWORK_THREAD_POOL_SIZE;
		File cacheDir = new File(context.getFilesDir(), "volley");

		HttpStack stack;
//		String userAgent = "volley/0";
//		try {
//			String packageName = context.getPackageName();
//			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
//			userAgent = packageName + "/" + info.versionCode;
//		} catch (NameNotFoundException e) {
//		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			stack = new HurlStack(userAgent, null);
		} else {
			// Prior to Gingerbread, HttpUrlConnection was unreliable.
			// See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
			stack = new HttpClientStack(userAgent);
		}
		
		DiskCache cache = null;
        if (cacheSize > 0) {
            cache = new DiskCache(cacheDir, cacheSize);
        } else {
            cache = new DiskCache(cacheDir);
        }

		Network network = new BasicNetwork(stack, HTTP.UTF_8);
		RequestQueue queue = new RequestQueue(network, poolSize, cache);
		queue.start();

        return queue;
    }
    
    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, -1);
    }
}
