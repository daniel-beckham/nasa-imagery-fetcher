package com.beckhamd.nasaimageryfetcher.item;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beckhamd.nasaimageryfetcher.R;
import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;
import com.beckhamd.nasaimageryfetcher.util.DateUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ImageItem extends AbstractItem<ImageItem, ImageItem.ViewHolder> {
    private final UniversalImageModel universalImageModel;

    public ImageItem(UniversalImageModel universalImageModel) {
        this.universalImageModel = universalImageModel;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.item_image;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_image;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<ImageItem> {
        @BindView(R.id.item_image_date_textview)
        TextView date;
        @BindView(R.id.item_image_imageview)
        ImageView imageView;
        @BindView(R.id.item_image_progressbar)
        ProgressBar progressBar;
        @BindView(R.id.item_image_title_textview)
        TextView title;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(@NonNull ImageItem item, List<Object> payloads) {
            progressBar.setVisibility(View.VISIBLE);

            date.setText(DateUtils.convertDateToLongDateFormat(date.getContext(), item.universalImageModel.getDate(), "yyyy-MM-dd"));
            title.setText(item.universalImageModel.getTitle());

            Glide.with(itemView.getContext())
                    .load(item.universalImageModel.getImageThumbnailUrl())
                    .apply(RequestOptions.centerCropTransform())
                    .transition(withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                       DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                    })
                    .into(imageView);

        }

        @Override
        public void unbindView(@NonNull ImageItem item) {}
    }
}
