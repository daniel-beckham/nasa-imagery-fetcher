package com.dsbeckham.nasaimageryfetcher.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.activity.ViewPagerActivity;
import com.dsbeckham.nasaimageryfetcher.adapter.IotdAdapter;
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;
import com.dsbeckham.nasaimageryfetcher.util.IotdQueryUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class IotdFragment extends Fragment {
    public FastItemAdapter<IotdAdapter> fastItemAdapter = new FastItemAdapter<>();
    public FooterAdapter<ProgressItem> footerAdapter = new FooterAdapter<>();
    public LinearLayoutManager linearLayoutManager;

    @Bind(R.id.fragment_iotd_progressbar)
    public View progressBar;
    @Bind(R.id.fragment_iotd_recyclerview)
    public RecyclerView recyclerView;
    @Bind(R.id.fragment_iotd_swiperefreshlayout)
    public SwipeRefreshLayout swipeRefreshLayout;

    public List<IotdRssModel.Channel.Item> iotdRssModels = new ArrayList<>();

    public boolean loadingData = false;

    public static String EXTRA_IOTD_RSS_MODELS = "com.dsbeckham.nasaimageryfetcher.extra.IOTD_RSS_MODELS";
    public static String EXTRA_IOTD_POSITION = "com.dsbeckham.nasaimageryfetcher.extra.IOTD_POSITION";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iotd, container, false);
        ButterKnife.bind(this, view);

        fastItemAdapter.withSavedInstanceState(savedInstanceState);
        fastItemAdapter.withOnClickListener(new FastAdapter.OnClickListener<IotdAdapter>() {
            @Override
            public boolean onClick(View view, IAdapter<IotdAdapter> iAdapter, IotdAdapter iotdAdapter, int position) {
                Intent intent = new Intent(getActivity(), ViewPagerActivity.class);
                intent.putExtra(EXTRA_IOTD_RSS_MODELS, Parcels.wrap(iotdRssModels));
                intent.putExtra(EXTRA_IOTD_POSITION, position);
                startActivityForResult(intent, 0);
                return false;
            }
        });

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(footerAdapter.wrap(fastItemAdapter));

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent);

        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);
        swipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typedValue.resourceId)
                + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                IotdQueryUtils.clearData(getActivity());
                IotdQueryUtils.beginFetch(getActivity());
            }
        });

        if (savedInstanceState == null) {
            IotdQueryUtils.beginFetch(getActivity());
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState = fastItemAdapter.saveInstanceState(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IotdQueryUtils.updateData(getActivity(), data);
    }
}
