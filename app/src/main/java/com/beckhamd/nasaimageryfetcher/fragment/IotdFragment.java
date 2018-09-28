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
import com.beckhamd.nasaimageryfetcher.util.IotdQueryUtils;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.mikepenz.fastadapter.adapters.ItemAdapter.items;

public class IotdFragment extends Fragment {
    private final String IOTD_MODELS = "iotdModels";

    @BindView(R.id.fragment_iotd_progressbar_layout)
    public FrameLayout progressBarLayout;
    @BindView(R.id.fragment_iotd_recyclerview)
    public RecyclerView recyclerView;
    @BindView(R.id.fragment_iotd_swiperefreshlayout)
    public SwipeRefreshLayout swipeRefreshLayout;
    private Unbinder unbinder;

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
        View view = inflater.inflate(R.layout.fragment_iotd, container, false);
        unbinder = ButterKnife.bind(this, view);

        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);

        footerAdapter = items();
        fastItemAdapter.addAdapter(1, footerAdapter);

        fastItemAdapter.withOnClickListener(new OnClickListener<ImageItem>() {
            @Override
            public boolean onClick(View view, IAdapter<ImageItem> iAdapter, ImageItem imageItem, int position) {
                Intent intent = new Intent(hostActivity, InformationActivity.class);
                intent.putExtra(InformationActivity.EXTRA_POSITION, position);
                intent.putExtra(InformationActivity.EXTRA_TYPE, InformationActivity.EXTRA_TYPE_IOTD);
                startActivityForResult(intent, 0);
                return false;
            }
        });

        linearLayoutManager = new LinearLayoutManager(hostActivity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fastItemAdapter);

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
                IotdQueryUtils.clearData(hostActivity);
                IotdQueryUtils.beginFetch(hostActivity, IotdQueryUtils.TYPE_RECYCLERVIEW);
            }
        });

        if (savedInstanceState == null) {
            IotdQueryUtils.clearData(hostActivity);
            IotdQueryUtils.beginFetch(hostActivity, IotdQueryUtils.TYPE_RECYCLERVIEW);
        } else {
            ((MainApplication) hostActivity.getApplication()).setIotdModels(savedInstanceState.<UniversalImageModel>getParcelableArrayList(IOTD_MODELS));

            for (UniversalImageModel universalImageModel : ((MainApplication) hostActivity.getApplication()).getIotdModels()) {
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
                new ArrayList<>(((MainApplication) hostActivity.getApplication()).getIotdModels());

        savedInstanceState = fastItemAdapter.saveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(IOTD_MODELS, models);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IotdQueryUtils.updateData(hostActivity, data);
    }
}
