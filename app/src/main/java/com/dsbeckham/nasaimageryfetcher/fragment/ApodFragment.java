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
import com.dsbeckham.nasaimageryfetcher.adapter.ApodAdapter;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaModel;
import com.dsbeckham.nasaimageryfetcher.util.QueryUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ApodFragment extends Fragment {
    public EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;
    public FastItemAdapter<ApodAdapter> fastItemAdapter = new FastItemAdapter<>();
    public FooterAdapter<ProgressItem> footerAdapter = new FooterAdapter<>();
    public LinearLayoutManager linearLayoutManager;

    @Bind(R.id.fragment_apod_progress_bar)
    public View progressBar;
    @Bind(R.id.fragment_apod_recycler_view)
    public RecyclerView recyclerView;
    @Bind(R.id.fragment_apod_swipe_refresh_layout)
    public SwipeRefreshLayout swipeRefreshLayout;

    public List<ApodMorphIoModel> apodMorphIoModels = new ArrayList<>();
    public List<ApodNasaModel> apodNasaModels = new ArrayList<>();

    public Calendar calendar = Calendar.getInstance();
    public boolean loadingData = false;
    public int nasaApiQueryCount = QueryUtils.APOD_NASA_API_QUERIES;

    public static String EXTRA_APOD_MORPH_IO_MODELS = "com.dsbeckham.nasaimageryfetcher.extra.APOD_MORPH_IO_MODELS";
    public static String EXTRA_APOD_NASA_MODELS = "com.dsbeckham.nasaimageryfetcher.extra.APOD_NASA_MODELS";
    public static String EXTRA_APOD_CALENDAR = "com.dsbeckham.nasaimageryfetcher.extra.APOD_CALENDAR";
    public static String EXTRA_APOD_POSITION = "com.dsbeckham.nasaimageryfetcher.extra.APOD_POSITION";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apod, container, false);
        ButterKnife.bind(this, view);

        fastItemAdapter.withSavedInstanceState(savedInstanceState);
        fastItemAdapter.withOnClickListener(new FastAdapter.OnClickListener<ApodAdapter>() {
            @Override
            public boolean onClick(View view, IAdapter<ApodAdapter> iAdapter, ApodAdapter apodAdapter, int position) {
                Intent intent = new Intent(getActivity(), ViewPagerActivity.class);
                // Add a check here that determines which API should be used based
                // on the user settings. (Also, add the relevant setting.)
                intent.putExtra(EXTRA_APOD_MORPH_IO_MODELS, Parcels.wrap(apodMorphIoModels));
                // intent.putExtra(EXTRA_APOD_NASA_MODELS, Parcels.wrap(apodNasaModels));
                intent.putExtra(EXTRA_APOD_CALENDAR, calendar);
                intent.putExtra(EXTRA_APOD_POSITION, position);
                startActivityForResult(intent, 0);
                return false;
            }
        });

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(footerAdapter.wrap(fastItemAdapter));

        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore(int currentPage) {
                footerAdapter.clear();
                footerAdapter.add(new ProgressItem());
                QueryUtils.beginApodQuery(getActivity(), QueryUtils.QUERY_MODE_RECYCLERVIEW);
            }
        };

        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent);

        // This is a workaround for a bug that causes the progress bar to be hidden underneath the
        // action bar.
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);
        swipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typedValue.resourceId)
                + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                QueryUtils.clearApodData(getActivity());
                QueryUtils.beginApodQuery(getActivity(), QueryUtils.QUERY_MODE_RECYCLERVIEW);
            }
        });

        if (savedInstanceState == null) {
            QueryUtils.beginApodQuery(getActivity(), QueryUtils.QUERY_MODE_RECYCLERVIEW);
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
        QueryUtils.updateApodData(getActivity(), data);
    }
}
