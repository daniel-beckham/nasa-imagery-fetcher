package com.dsbeckham.nasaimageryfetcher.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.fragment.ViewPagerFragment;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.UiUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageActivity extends AppCompatActivity {
    @BindView(R.id.activity_image_subsamplingscaleimageview)
    SubsamplingScaleImageView subsamplingScaleImageView;
    @BindView(R.id.activity_image_progressbar)
    ProgressBar progressBar;
    @BindView(R.id.activity_image_toolbar)
    public Toolbar toolbar;

    private boolean systemUiHidden;

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            subsamplingScaleImageView.setImage(ImageSource.bitmap(bitmap).tilingDisabled());
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onBitmapFailed(android.graphics.drawable.Drawable errorDrawable) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPrepareLoad(android.graphics.drawable.Drawable placeHolderDrawable) {}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);

        UiUtils.makeStatusBarTransparent(this);
        UiUtils.setUpToolBarForChildActivity(this, toolbar);

        UniversalImageModel universalImageModel = Parcels.unwrap(getIntent().getParcelableExtra(ViewPagerFragment.EXTRA_CURRENT_MODEL));

        Picasso.with(this)
                .load(universalImageModel.getImageThumbnailUrl())
                .resize(2048, 2048)
                .centerInside()
                .config(Bitmap.Config.RGB_565)
                .into(target);

        UiUtils.showSystemUI(ImageActivity.this);
        systemUiHidden = false;

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
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
                return true;
            }
        });

        subsamplingScaleImageView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch (View view, MotionEvent motionEvent){
                return gestureDetector.onTouchEvent(motionEvent);
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
