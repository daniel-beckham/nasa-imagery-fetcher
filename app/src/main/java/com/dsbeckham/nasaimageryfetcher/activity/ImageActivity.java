package com.dsbeckham.nasaimageryfetcher.activity;

import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.fragment.ViewPagerFragment;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.UiUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;

public class ImageActivity extends AppCompatActivity {
    @BindView(R.id.activity_image_photoview)
    PhotoView photoView;
    @BindView(R.id.activity_image_progressbar)
    ProgressBar progressBar;
    @BindView(R.id.activity_image_toolbar)
    public Toolbar toolbar;

    private GestureDetectorCompat simpleOnGestureListener;
    private boolean systemUiHidden;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);

        UiUtils.makeStatusBarTransparent(this);
        UiUtils.setUpToolBarForChildActivity(this, toolbar);

        UniversalImageModel universalImageModel = Parcels.unwrap(getIntent().getParcelableExtra(ViewPagerFragment.EXTRA_CURRENT_MODEL));

        Picasso.with(this)
                .load(universalImageModel.getImageUrl())
                .into(photoView, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                progressBar.setVisibility(View.GONE);
            }
        });

        UiUtils.showSystemUI(ImageActivity.this);
        systemUiHidden = false;

        photoView.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                if (systemUiHidden) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().show();
                    }

                    UiUtils.showSystemUI(ImageActivity.this);
                    systemUiHidden = false;
                } else {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().hide();
                    }

                    UiUtils.hideSystemUI(ImageActivity.this);
                    systemUiHidden = true;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
