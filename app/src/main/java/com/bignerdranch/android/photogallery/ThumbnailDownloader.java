package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by 权 on 2018/4/17.
 */

public class ThumbnailDownloader<T> extends HandlerThread{

    private static final String TAG = "thumbnaiilDownloader";

    private Boolean mHasQuit = false;


    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;

    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();

    private Handler mResponseHandler;

    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;







    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueIhumbnail(T target,String url){
        Log.i(TAG,"Got a URL: " + url);

        if(url==null){
            mRequestMap.remove(target);
        }else{
            mRequestMap.put(target,url);//做hash映射
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();//将 这个Message放置在Looper消息队列的尾部


        }


    }

    public ThumbnailDownloader(String name, int priority) {
        super(name, priority);
    }


    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){

                    T target = (T) msg.obj;
                    Log.i(TAG,"Got a request for URL: " + mRequestMap.get(target));

                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target){

        try {
            final String url = mRequestMap.get(target);
            if(url==null){
                return;
            }
            byte [] bitmapBytes = new FlickFetchr().getUrlBytes(url);

            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);

            Log.i(TAG,"Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target)!=url ||
                            mHasQuit){
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Error downloading image",e);
        }
    }

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }

    public void setmThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener = listener;
    }


    public ThumbnailDownloader(Handler mResponseHandler) {
        super(TAG);
        this.mResponseHandler = mResponseHandler;
    }


    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }


}
