package com.wuwang.aavt.examples;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wyz.camera.CameraRecorder;
import com.wyz.camera.core.gl.GroupShader;
import com.wyz.camera.core.gl.func.unit.BrightnessShadser;
import com.wyz.camera.core.gl.func.unit.CameoShader;
import com.wyz.camera.core.gl.func.unit.ColorShader;
import com.wyz.camera.core.gl.func.unit.ContrastShader;
import com.wyz.camera.core.gl.func.unit.ConvolutionShader;
import com.wyz.camera.core.gl.func.unit.InvertShader;
import com.wyz.camera.core.gl.func.unit.SaturationShader;
import com.wyz.camera.core.gl.func.unit.SepiaShader;
import com.wyz.camera.core.gl.func.unit.SketchShader;
import com.wyz.camera.core.gl.func.unit.ToonShader;
import com.wyz.camera.core.gl.func.unit.VignetteShader;
import com.wyz.camera.core.gl.mark.WaterMarkShader;
import com.wyz.camera.utils.DensityUtils;
import com.wyz.camera.view.CircularProgressView;


public class CameraTakePictureActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private TextView mTvPreview;
    private CircularProgressView mTvRecord;
    private boolean isPreviewOpen = false;
    private boolean isRecordOpen = false;
    private int mCameraWidth, mCameraHeight;

    private CameraRecorder mCamera;

    private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("CameraActivity", "onCreate ->");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_record);
        mSurfaceView = findViewById(R.id.mSurfaceView);
        mTvRecord = findViewById(R.id.mTvRec);
        mTvPreview = findViewById(R.id.mTvShow);
        mTvRecord.setTotal(20000);
        mCamera = new CameraRecorder();
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                GroupShader filter = new GroupShader(getResources());
                mCamera.setRenderer(filter);
                //filter.addFilter(new ColorShader(getResources() , new float[]{.0f , .5f , .0f}));
                // filter.addFilter(new CameoShader(getResources() , new float[]{1920f, 1080f}));
                // filter.addFilter(new InvertShader(getResources() ));
//                filter.addFilter(new SepiaShader(getResources() ));
                //           filter.addFilter(new ToonShader(getResources()));
             //   filter.addFilter(new ConvolutionShader(getResources()));
             //   filter.addFilter(new VignetteShader(getResources()));
              //  filter.addFilter(new SaturationShader(getResources()));
//                filter.addFilter(new ContrastShader(getResources()));
                filter.addFilter(new BrightnessShadser(getResources()));
                //         filter.addFilter(new SketchShader(getResources()));
                //filter.addFilter(new StickFigureFilter(getResources()));
                //filter.addFilter(new BeautyFilter(getResources()).setBeautyLevel(4));
                filter.addFilter(new WaterMarkShader().setMarkPosition(150, 150, 100, 76).setMark(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)));
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mCamera.setOutputPath(tempPath);
                mCamera.open();
                mCamera.setSurface(holder.getSurface());
                int[] screenWidth = DensityUtils.Companion.getScreenWidth(getApplicationContext());
                mCamera.setPreviewSize(screenWidth[0], screenWidth[1]);
                mCamera.startPreview();
                isPreviewOpen = true;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCamera.stopPreview();
                mCamera.close();
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mTvShow:
                isPreviewOpen = !isPreviewOpen;
                mTvPreview.setText(isPreviewOpen ? "关预览" : "开预览");
                if (isPreviewOpen) {
                    mCamera.startPreview();
                } else {
                    mCamera.stopPreview();
                }
                break;
            case R.id.mTvRec:
                mCamera.takePictures();
                break;
            default:
                break;
        }
    }

}
