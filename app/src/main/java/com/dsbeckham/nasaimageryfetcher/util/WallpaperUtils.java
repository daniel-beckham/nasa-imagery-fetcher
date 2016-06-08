package com.dsbeckham.nasaimageryfetcher.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class WallpaperUtils {
    public static Context context;

    private static Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);

            try {
                wallpaperManager.setBitmap(bitmap);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        @Override
        public void onBitmapFailed(android.graphics.drawable.Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(android.graphics.drawable.Drawable placeHolderDrawable) {}
    };

    public static void setWallpaper(Context context, String uri) {
        WallpaperUtils.context = context;
        Picasso.with(context)
                .load(uri)
                .into(target);
    }
}
