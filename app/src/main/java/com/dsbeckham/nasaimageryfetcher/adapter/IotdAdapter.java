package com.dsbeckham.nasaimageryfetcher.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;
import com.dsbeckham.nasaimageryfetcher.util.DateTimeUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class IotdAdapter extends AbstractItem<IotdAdapter, IotdAdapter.ViewHolder> {
    public IotdRssModel.Channel.Item iotdRssModelItem;

    public IotdAdapter(IotdRssModel.Channel.Item iotdRssModel) {
        this.iotdRssModelItem = iotdRssModel;
    }

    @Override
    public void bindView(final ViewHolder viewHolder) {
        super.bindView(viewHolder);

        viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.date.setText(DateTimeUtils.formatDate(viewHolder.date.getContext(), iotdRssModelItem.getPubDate(), "EEE, dd MMM yyyy HH:mm zzz"));
        viewHolder.title.setText(iotdRssModelItem.getTitle());
        Picasso.with(viewHolder.image.getContext())
                .load(iotdRssModelItem.getEnclosure().getUrl())
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
    }

    @Override
    public int getType() {
        return R.id.item_image;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_recylerview;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.item_recyclerview_date) TextView date;
        @Bind(R.id.item_recyclerview_image) ImageView image;
        @Bind(R.id.item_recyclerview_progress_bar) View progressBar;
        @Bind(R.id.item_recyclerview_title) TextView title;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
