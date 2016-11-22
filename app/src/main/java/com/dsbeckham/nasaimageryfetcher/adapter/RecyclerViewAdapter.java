package com.dsbeckham.nasaimageryfetcher.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.DateUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecyclerViewAdapter extends AbstractItem<RecyclerViewAdapter, RecyclerViewAdapter.ViewHolder> {
    private final UniversalImageModel universalImageModel;

    public RecyclerViewAdapter(UniversalImageModel universalImageModel) {
        this.universalImageModel = universalImageModel;
    }

    @Override
    public void bindView(final ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.progressBar.setVisibility(View.VISIBLE);

        viewHolder.date.setText(DateUtils.convertDateToLongDateFormat(viewHolder.date.getContext(), universalImageModel.getDate(), "yyyy-MM-dd"));
        viewHolder.title.setText(universalImageModel.getTitle());

        Picasso.with(viewHolder.imageView.getContext())
                .load(universalImageModel.getImageThumbnailUrl())
                .config(Bitmap.Config.RGB_565)
                .fit()
                .centerCrop()
                .into(viewHolder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        viewHolder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        viewHolder.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
    }

    @Override
    public int getType() {
        return R.id.item_recyclerview;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_recylerview;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_recyclerview_date_textview)
        TextView date;
        @BindView(R.id.item_recyclerview_imageview)
        ImageView imageView;
        @BindView(R.id.item_recyclerview_progressbar)
        ProgressBar progressBar;
        @BindView(R.id.item_recyclerview_title_textview)
        TextView title;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
