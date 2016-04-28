package com.dsbeckham.nasaimageryfetcher.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaGovModel;
import com.dsbeckham.nasaimageryfetcher.util.DateTimeUtils;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ApodAdapter<T> extends AbstractItem<ApodAdapter<T>, ApodAdapter.ViewHolder> {
    public T model;
    private int modelType;

    public ApodAdapter(T model, int modelType) {
        this.model = model;
        this.modelType = modelType;
    }

    @Override
    public void bindView(final ViewHolder viewHolder) {
        super.bindView(viewHolder);

        switch (modelType) {
            case ApodQueryUtils.MODEL_MORPH_IO:
                viewHolder.date.setText(DateTimeUtils.formatDate(viewHolder.date.getContext(), ((ApodMorphIoModel) model).getDate(), "yyyy-MM-dd"));
                viewHolder.title.setText(((ApodMorphIoModel) model).getTitle());
                Picasso.with(viewHolder.imageView.getContext())
                        .load(((ApodMorphIoModel) model).getPictureThumbnailUrl())
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
                break;
            case ApodQueryUtils.MODEL_NASA_GOV:
                viewHolder.date.setText(DateTimeUtils.formatDate(viewHolder.date.getContext(), ((ApodNasaGovModel) model).getDate(), "yyyy-MM-dd"));
                viewHolder.title.setText(((ApodNasaGovModel) model).getTitle());
                Picasso.with(viewHolder.imageView.getContext())
                        .load(((ApodNasaGovModel) model).getUrl())
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
                break;
        }
    }

    @Override
    public int getType() {
        return R.id.item_recyclerview;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_recylerview;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.item_recyclerview_date_textview) TextView date;
        @Bind(R.id.item_recyclerview_imageview) ImageView imageView;
        @Bind(R.id.item_recyclerview_progressbar) View progressBar;
        @Bind(R.id.item_recyclerview_title_textview) TextView title;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
