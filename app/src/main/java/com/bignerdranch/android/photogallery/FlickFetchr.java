package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by 权 on 2018/4/15.
 */

public class FlickFetchr {

    private static final String TAG = "FlickFetchr";
    private static final String API_KEY = "d2c6a4fa1b4e81e88d34aca532093bd3";

    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";

    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key",API_KEY)
            .appendQueryParameter("format","json")
            .appendQueryParameter("nojsoncallback","1")
            .appendQueryParameter("extras","url_s")
            .build();



        //连接网络
    public byte [] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();//读

            if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;

            byte [] buffer = new byte[1024];

            while((bytesRead=in.read(buffer))>0){ //用read 方法读取网络数据
                out.write(buffer,0,bytesRead); //把数据写进buffer数组
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }

    }


    public String getUrlString(String urlsqec) throws IOException{
        return new String(getUrlBytes(urlsqec));
    }


//    public List<GalleryItem> fetchItems()

    private List<GalleryItem> downloadGalleryItems(String url){

        List<GalleryItem> items = new ArrayList<>();

        try{

            Log.i(TAG,url);
            String jsonString = getUrlString(url);

            Log.i(TAG,jsonString);

            JSONObject jsonBody = new JSONObject(jsonString); //对返回来的数据进行解析

            parseItem(items,jsonBody);

        }catch (IOException e){
            Log.e(TAG,"Failed to getch items",e);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }


    //解析Json数据
    private void parseItem(List<GalleryItem> items,JSONObject jsonBody) throws JSONException {

        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");

        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for(int i=0; i<photoJsonArray.length(); i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();

            item.setmId(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));

            if(!photoJsonObject.has("url_s")){
                continue;
            }


            item.setmUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }

    }


    private String buildUrl(String method,String query){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method",method);

        if(method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text",query);
        }

        return uriBuilder.build().toString();
    }

    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl(FETCH_RECENTS_METHOD,null);
        return downloadGalleryItems(url);
    }


    public List<GalleryItem> searchPhotos(String query){
        String url = buildUrl(SEARCH_METHOD,query);
        return downloadGalleryItems(url);
    }


}
