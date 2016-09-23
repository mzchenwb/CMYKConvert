package com.chenwb.cmykconvert;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chenwb.cmykconvert.ScaleAnimationImageView.PointColorListener;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements PointColorListener, SeekBar.OnSeekBarChangeListener {
    public static final int REQUEST_CODE_CAPTURE_CAMERA = 1;
    @Bind(R.id.capture)
    View mCapture;
    @Bind(R.id.curr_color)
    View mCurrColor;
    @Bind(R.id.color_value)
    TextView mColorValue;

    @Bind(R.id.curr_picture)
    ScaleAnimationImageView mCurrPicture;

    @Bind(R.id.seek_c)
    SeekBar mSeekC;
    @Bind(R.id.seek_m)
    SeekBar mSeekM;
    @Bind(R.id.seek_y)
    SeekBar mSeekY;
    @Bind(R.id.seek_k)
    SeekBar mSeekK;

    @Bind(R.id.c_value)
    TextView mCValue;
    @Bind(R.id.m_value)
    TextView mMValue;
    @Bind(R.id.y_value)
    TextView mYValue;
    @Bind(R.id.k_value)
    TextView mKValue;


    private File mPictureFile;
    private int mScreenW, mScreenH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File out_file_path = getExternalCacheDir();
        mPictureFile = new File(out_file_path, "tmp.jpg");
        ButterKnife.bind(this);

        mCurrPicture.setPointColorListener(this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mScreenW = displayMetrics.widthPixels;
        mScreenH = displayMetrics.heightPixels;

        loadImage();

        mSeekC.setOnSeekBarChangeListener(this);
        mSeekM.setOnSeekBarChangeListener(this);
        mSeekY.setOnSeekBarChangeListener(this);
        mSeekK.setOnSeekBarChangeListener(this);
    }

    @OnClick(R.id.capture)
    void onCapture() {
        Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
        if (mPictureFile.exists())
            mPictureFile.delete();

        getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPictureFile));
        getImageByCamera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(getImageByCamera, REQUEST_CODE_CAPTURE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CODE_CAPTURE_CAMERA) {
            loadImage();
        }
    }

    private void loadImage() {
        if (mPictureFile != null && mPictureFile.exists()) {
            mCurrPicture.setImageDrawable(resizeImage2(mPictureFile.getAbsolutePath(),
                    mScreenW, mScreenH));
        }
    }

    public static Drawable resizeImage2(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不加载bitmap到内存中
        BitmapFactory.decodeFile(path, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            options.inSampleSize = sampleSize;
        }

        options.inJustDecodeBounds = false;
        return new BitmapDrawable(Resources.getSystem(), BitmapFactory.decodeFile(path, options));
    }

    @Override
    public void update(int color) {
        mCurrColor.setBackgroundColor(color);

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        String value = String.format("#%02x%02x%02x", r, g, b);
        mColorValue.setText(value.toUpperCase());

        float[] cmyk = ColorConvert.rgb2cmyk2(r, g, b);

        mSeekC.setMax(100);
        mSeekM.setMax(100);
        mSeekY.setMax(100);
        mSeekK.setMax(100);

        mSeekC.setProgress(Math.round(cmyk[0] * 100));
        mSeekM.setProgress(Math.round(cmyk[1] * 100));
        mSeekY.setProgress(Math.round(cmyk[2] * 100));
        mSeekK.setProgress(Math.round(cmyk[3] * 100));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int c = mSeekC.getProgress();
            int m = mSeekM.getProgress();
            int y = mSeekY.getProgress();
            int k = mSeekK.getProgress();
            int rgb = ColorConvert.cmyk2rgb(c / 100f, m / 100f, y / 100f, k / 100f);
            int argb = 0xff << 24 | rgb;

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            String value = String.format("#%02x%02x%02x", r, g, b);
            mColorValue.setText(value.toUpperCase());

            mCurrColor.setBackgroundColor(argb);
        }
        if (seekBar == mSeekC) {
            mCValue.setText(String.format("%02d%%", progress));
        } else if (seekBar == mSeekM) {
            mMValue.setText(String.format("%02d%%", progress));
        } else if (seekBar == mSeekY) {
            mYValue.setText(String.format("%02d%%", progress));
        } else if (seekBar == mSeekK) {
            mKValue.setText(String.format("%02d%%", progress));
        }
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
