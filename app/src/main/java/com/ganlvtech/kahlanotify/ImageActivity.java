package com.ganlvtech.kahlanotify;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ImageActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_NAME_IMAGE_URL = "imageUrl";
    private String mImageUrl;
    private SubsamplingScaleImageView mImageViewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // Get intent extras
        mImageUrl = getIntent().getStringExtra(INTENT_EXTRA_NAME_IMAGE_URL);
        if (mImageUrl == null) {
            finish();
            return;
        }

        mImageViewImage = findViewById(R.id.imageViewImage);

        Picasso.get()
                .load(mImageUrl)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mImageViewImage.setImage(ImageSource.bitmap(bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        finish();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
    }
}
