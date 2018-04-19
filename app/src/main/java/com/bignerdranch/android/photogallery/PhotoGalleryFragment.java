package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.photogallery.Ulite.HttpUtil;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.R.attr.bitmap;
import static android.R.attr.type;

/**
 * Created by 权 on 2018/4/15.
 */

public class PhotoGalleryFragment extends Fragment {


    private static final String TAG = "PhotoGalleryFragment";

    private List<GalleryItem> GalleryItemList = new ArrayList<>();

    private RecyclerView mPhotoRecyclerView;

    private PhotoAdapter mPhotoAdapter;

    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;


    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();

        Handler responseHandler = new Handler(); //创建Handler
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setmThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>(){

                    @Override
                    public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                        Drawable drawable = new BitmapDrawable(getResources(),thumbnail);
                        target.bindGalleryItem(drawable);
                    }
                }
        );

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();

        Log.i(TAG,"Background thread started");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));


        //  requestPhoto();
//        adapter.notifyDataSetChanged();

        setupAdapter();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();

        Log.i(TAG,"Background thread destroyed");
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {//异步问题  使用AsyncTask 在后台线程上运行代码

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            //                String result = new FlickFetchr().getUrlString("https://www.bignerdranch.com");
//                Log.i(TAG,result);
            return new FlickFetchr().fetchItems();
            //
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            GalleryItemList = items;
            setupAdapter();
        }
    }


    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mTitleImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mTitleImageView = itemView.findViewById(R.id.item_image_view);
        }

        public void bindGalleryItem(Drawable drawable) {
            mTitleImageView.setImageDrawable(drawable);
        }
    }


    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> galleryItemList;

        public PhotoAdapter(List<GalleryItem> mGalleryItemList) {
            this.galleryItemList = mGalleryItemList;
        }


        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item = galleryItemList.get(position);
        //    Drawable placeHolder = getResources().getDrawable(R.drawable.bill_up_close);
         //   holder.bindGalleryItem(placeHolder);

            mThumbnailDownloader.queueIhumbnail(holder,item.getmUrl());
        }

        @Override
        public int getItemCount() {
            return galleryItemList.size();
        }
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoAdapter = new PhotoAdapter(GalleryItemList);
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        }
    }


}
