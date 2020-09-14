package com.wuwang.aavt.examples;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;

import com.wyz.camera.ScreenShower;
import com.wyz.camera.core.gl.GroupShader;
import com.wyz.camera.core.gl.RollShader;
import com.wyz.camera.core.gl.func.FluorescenceShader;
import com.wyz.camera.core.gl.func.StickFigureShader;
import com.wyz.camera.core.gl.mark.WaterColorStepShader;
import com.wyz.common.ui.AbsActionBarActivity;
import com.wyz.camera.view.CircularProgressView;


public class ScreenShowerActivity extends AbsActionBarActivity {

    private SurfaceView mSurfaceView;
    private CircularProgressView mTvRecord;

    private ScreenShower screenShower;

    private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testPicture.jpeg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("CameraActivity", "onCreate ->");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_record);

        mTvRecord = findViewById(R.id.mTvRec);
        mTvRecord.setTotal(20000);
        if(ScreenShower.Companion.getShower() == null){
            ScreenShower.Companion.setShower(new ScreenShower(tempPath , true));
        }
        screenShower = ScreenShower.Companion.getShower();
        mSurfaceView = findViewById(R.id.mSurfaceView);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                GroupShader filter = new GroupShader(getResources());
                screenShower.setRenderer(filter);
                //filter.addFilter(new LazyShader());
                //filter.addFilter(new StickFigureShader(getResources()));
                //filter.addFilter(new BlackMagicShader(getResources()));
                //filter.addFilter(new CandyShader(getResources()));
                //filter.addFilter(new FluorescenceShader(getResources()));
                //filter.addFilter(new WaterColorShader(getResources()));
                //filter.addFilter(new RollShader(getResources()));
                filter.addFilter(new FluorescenceShader(getResources()));
                //filter.addFilter(new WaterColorStepShader(getResources()));
                //filter.addFilter(new Faltung33Shader(getResources() , Faltung33Shader.Companion.getFILTER_BORDER()));
                //filter.addFilter(new Faltung33Shader(getResources() , Faltung33Shader.Companion.getFILTER_CAMEO()));
                //filter.addFilter(new Faltung33Shader(getResources() , Faltung33Shader.Companion.getFILTER_SHARPEN()));
                //filter.addFilter(new StickFigureShader(getResources()));
                //filter.addFilter(new BeautyShader(getResources()).setBeautyLevel(4));
                //filter.addFilter(new WaterMarkShader().setMarkPosition(150, 150, 100, 76).setMark(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)));
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                screenShower.open();
                screenShower.setSurface(holder.getSurface());
                screenShower.setPreviewSize(width, height);
                screenShower.startPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                screenShower.stopPreview();
                screenShower.close();
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mTvRec:
                screenShower.takePictures();
                break;
            case R.id.mTvShow:
                screenShower.updateFrame();
                break;
        }
    }

}
