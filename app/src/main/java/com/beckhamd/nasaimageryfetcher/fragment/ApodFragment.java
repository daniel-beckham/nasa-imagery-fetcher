package com.beckhamd.nasaimageryfetcher.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.beckhamd.nasaimageryfetcher.R;
import com.beckhamd.nasaimageryfetcher.activity.InformationActivity;
import com.beckhamd.nasaimageryfetcher.application.MainApplication;
import com.beckhamd.nasaimageryfetcher.item.ImageItem;
import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;
import com.beckhamd.nasaimageryfetcher.util.ApodQueryUtils;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.mikepenz.fastadapter.adapters.ItemAdapter.items;

public class ApodFragment extends Fragment {
    private final String APOD_MODELS = "apodModels";

    @BindView(R.id.fragment_apod_progressbar_layout)
    public FrameLayout progressBarLayout;
    @BindView(R.id.fragment_apodrecyclerview)
    public RecyclerView recyclerView;
    @BindView(R.id.fragment_apod_swiperefreshlayout)
    public SwipeRefreshLayout swipeRefreshLayout;
    private Unbinder unbinder;

    public EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;
    public FastItemAdapter<ImageItem> fastItemAdapter;
    public ItemAdapter footerAdapter;
    public LinearLayoutManager linearLayoutManager;

    private Activity hostActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            hostActivity = (Activity) context;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            hostActivity = activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apod, container, false);
        unbinder = ButterKnife.bind(this, view);

        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);

        footerAdapter = items();
        fastItemAdapter.addAdapter(1, footerAdapter);

        fastItemAdapter.withSavedInstanceState(savedInstanceState);
        fastItemAdapter.withOnClickListener(new OnClickListener<ImageItem>() {
            @Override
            public boolean onClick(View view, IAdapter<ImageItem> iAdapter, ImageItem imageItem, int position) {
                Intent intent = new Intent(hostActivity, InformationActivity.class);
                intent.putExtra(InformationActivity.EXTRA_POSITION, position);
                intent.putExtra(InformationActivity.EXTRA_TYPE, InformationActivity.EXTRA_TYPE_APOD);
                startActivityForResult(intent, 0);
                return false;
            }
        });

        linearLayoutManager = new LinearLayoutManager(hostActivity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fastItemAdapter);

        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(footerAdapter) {
            @Override
            public void onLoadMore(final int currentPage) {
                recyclerView.post(new Runnable() {
                    public void run() {
                        ApodQueryUtils.beginQuery(hostActivity, ApodQueryUtils.TYPE_RECYCLERVIEW, false);
                    }
                });
            }
        };

        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent);

        TypedValue typedValue = new TypedValue();
        hostActivity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);
        swipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typedValue.resourceId)
                + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ApodQueryUtils.clearData(hostActivity);
                ApodQueryUtils.beginQuery(hostActivity, ApodQueryUtils.TYPE_RECYCLERVIEW, false);
            }
        });

        if (savedInstanceState == null) {
            ApodQueryUtils.clearData(hostActivity);
            ApodQueryUtils.beginQuery(hostActivity, ApodQueryUtils.TYPE_RECYCLERVIEW, false);
        } else {
            ((MainApplication) hostActivity.getApplication()).setApodModels(savedInstanceState.<UniversalImageModel>getParcelableArrayList(APOD_MODELS));

            for (UniversalImageModel universalImageModel : ((MainApplication) hostActivity.getApplication()).getApodModels()) {
                fastItemAdapter.add(fastItemAdapter.getAdapterItemCount(), new ImageItem(universalImageModel));
            }

            fastItemAdapter.withSavedInstanceState(savedInstanceState);
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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        ArrayList<UniversalImageModel> models =
                new ArrayList<>(((MainApplication) hostActivity.getApplication()).getApodModels());

        savedInstanceState = fastItemAdapter.saveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(APOD_MODELS, models);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ApodQueryUtils.updateData(hostActivity, data);
    }
}
