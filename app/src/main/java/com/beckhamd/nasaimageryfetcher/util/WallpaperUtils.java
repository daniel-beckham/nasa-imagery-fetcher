package com.beckhamd.nasaimageryfetcher.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class WallpaperUtils {
    private static WallpaperManager wallpaperManager;

    public static void setWallpaper(Context context, String uri, boolean background) {
        wallpaperManager = WallpaperManager.getInstance(context);

        if (background) {
            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(uri)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            try {
                                wallpaperManager.setBitmap(resource);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    });
        } else {
            try {
                FutureTarget<Bitmap> bitmap = Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(uri)
                        .submit();

                wallpaperManager.setBitmap(bitmap.get());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
