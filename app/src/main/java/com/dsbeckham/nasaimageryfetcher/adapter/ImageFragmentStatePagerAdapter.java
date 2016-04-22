package com.dsbeckham.nasaimageryfetcher.adapter;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.dsbeckham.nasaimageryfetcher.fragment.ImageFragment;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaModel;
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;
import com.dsbeckham.nasaimageryfetcher.util.QueryUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ImageFragmentStatePagerAdapter extends SmartFragmentStatePagerAdapter {
    private Activity activity;
    private String type;

    public List<ApodMorphIoModel> apodMorphIoModels = new ArrayList<>();
    public List<ApodNasaModel> apodNasaModels = new ArrayList<>();
    public List<IotdRssModel.Channel.Item> iotdRssModels = new ArrayList<>();

    public Calendar calendar = Calendar.getInstance();
    public boolean loadingData = false;
    public int nasaApiQueryCount = QueryUtils.APOD_NASA_API_QUERIES;

    public ImageFragmentStatePagerAdapter(Activity activity, FragmentManager fragmentManager) {
        super(fragmentManager);

        this.activity = activity;
        type = PreferenceManager.getDefaultSharedPreferences(activity).getString("current_fragment", "iotd");

        switch (type) {
            case "iotd":
                iotdRssModels = Parcels.unwrap(activity.getIntent().getParcelableExtra("iotd_rss_models"));
                break;
            case "apod":
                // Add a check here that determines which API should be used based
                // on the user settings. (Also, add the relevant setting.)
                apodMorphIoModels = Parcels.unwrap(activity.getIntent().getParcelableExtra("apod_morph_io_models"));
                // apodNasaModels = Parcels.unwrap(activity.getIntent().getParcelableExtra("apod_nasa_models"));
                calendar = (Calendar) activity.getIntent().getSerializableExtra("apod_calendar");
                break;
        }
    }

    @Override
    public int getCount() {
        switch (type) {
            case "iotd":
                return iotdRssModels.size();
            case "apod":
                // Add a check here that determines which API should be used based
                // on the user settings. (Also, add the relevant setting.)
                return apodMorphIoModels.size();
                // return apodNasaModels.size();
        }
        return 0;
    }

    @Override
    public Fragment getItem(int position) {
        if (type.equals("apod")) {
            if (position == apodMorphIoModels.size() - 1) {
                QueryUtils.beginApodQuery(activity, QueryUtils.QUERY_MODE_VIEWPAGER);
            }
        }
        return ImageFragment.newInstance(position, type);
    }
}
