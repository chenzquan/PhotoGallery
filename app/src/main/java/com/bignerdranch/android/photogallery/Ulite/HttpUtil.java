package com.bignerdranch.android.photogallery.Ulite;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.R.string.ok;

/**
 * Created by 权 on 2018/4/16.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}
