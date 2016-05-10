package com.dsbeckham.nasaimageryfetcher.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.activity.InformationActivity;
import com.dsbeckham.nasaimageryfetcher.adapter.RecyclerViewAdapter;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.IotdQueryUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class IotdFragment extends Fragment {
    @BindView(R.id.fragment_iotd_progressbar_layout)
    public FrameLayout progressBarLayout;
    @BindView(R.id.fragment_iotd_recyclerview)
    public RecyclerView recyclerView;
    @BindView(R.id.fragment_iotd_swiperefreshlayout)
    public SwipeRefreshLayout swipeRefreshLayout;
    private Unbinder unbinder;

    public FastItemAdapter<RecyclerViewAdapter> fastItemAdapter = new FastItemAdapter<>();
    public FooterAdapter<ProgressItem> footerAdapter = new FooterAdapter<>();
    public LinearLayoutManager linearLayoutManager;

    public List<UniversalImageModel> models = new ArrayList<>();

    public boolean loadingData = false;

    public static String EXTRA_IOTD_MODELS = "com.dsbeckham.nasaimageryfetcher.extra.IOTD_MODELS";
    public static String EXTRA_IOTD_POSITION = "com.dsbeckham.nasaimageryfetcher.extra.IOTD_POSITION";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iotd, container, false);
        unbinder = ButterKnife.bind(this, view);

        fastItemAdapter.withSavedInstanceState(savedInstanceState);
        fastItemAdapter.withOnClickListener(new FastAdapter.OnClickListener<RecyclerViewAdapter>() {
            @Override
            public boolean onClick(View view, IAdapter<RecyclerViewAdapter> iAdapter, RecyclerViewAdapter recyclerViewAdapter, int position) {
                Intent intent = new Intent(getActivity(), InformationActivity.class);
                intent.putExtra(EXTRA_IOTD_MODELS, Parcels.wrap(models));
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
        unbinder.unbind();
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
