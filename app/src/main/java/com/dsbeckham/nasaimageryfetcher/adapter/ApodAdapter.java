package com.dsbeckham.nasaimageryfetcher.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaModel;
import com.dsbeckham.nasaimageryfetcher.util.DateTimeUtils;
import com.dsbeckham.nasaimageryfetcher.util.QueryUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ApodAdapter<T> extends AbstractItem<ApodAdapter<T>, ApodAdapter.ViewHolder> {
    public T apodModel;
    private int apodModelType;

    public ApodAdapter(T apodModel, int apodModelType) {
        this.apodModel = apodModel;
        this.apodModelType = apodModelType;
    }

    @Override
    public void bindView(final ViewHolder viewHolder) {
        super.bindView(viewHolder);

        switch (apodModelType) {
            case QueryUtils.APOD_MODEL_MORPH_IO:
                viewHolder.date.setText(DateTimeUtils.formatDate(viewHolder.date.getContext(), ((ApodMorphIoModel) apodModel).getDate(), "yyyy-MM-dd"));
                viewHolder.title.setText(((ApodMorphIoModel) apodModel).getTitle());
                Picasso.with(viewHolder.image.getContext())
                        .load(((ApodMorphIoModel) apodModel).getPictureThumbnailUrl())
                        .fit()
                        .centerCrop()
                        .into(viewHolder.image, new Callback() {
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
            case QueryUtils.APOD_MODEL_NASA:
                viewHolder.date.setText(DateTimeUtils.formatDate(viewHolder.date.getContext(), ((ApodNasaModel) apodModel).getDate(), "yyyy-MM-dd"));
                viewHolder.title.setText(((ApodNasaModel) apodModel).getTitle());
                Picasso.with(viewHolder.image.getContext())
                        .load(((ApodNasaModel) apodModel).getUrl())
                        .fit()
                        .centerCrop()
                        .into(viewHolder.image, new Callback() {
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
        @Bind(R.id.item_recyclerview_imageview) ImageView image;
        @Bind(R.id.item_recyclerview_progressbar) View progressBar;
        @Bind(R.id.item_recyclerview_title_textview) TextView title;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
