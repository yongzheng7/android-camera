package com.wuwang.aavt.examples;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;

import com.atom.camera.ScreenShower;
import com.atom.camera.core.gl.GroupShader;
import com.atom.camera.core.gl.func.FluorescenceShader;
import com.atom.camera.view.CircularProgressView;


public class ScreenShowerActivity extends AbsActionBarActivity {

    private SurfaceView mSurfaceView;
    private CircularProgressView mTvRecord;

    private ScreenShower screenShower;

    private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testPicture.jpeg";

    SurfaceHolder.Callback callback=  new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.e("ScreenShower" , "ScreenShowerActivity  surfaceCreated") ;
            GroupShader filter = new GroupShader(getResources());
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
            if(screenShower !=null){
                if(!screenShower.isRunning()){
                    screenShower.setRenderer(filter);
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.e("ScreenShower" , "ScreenShowerActivity  surfaceChanged") ;
            screenShower.open();
            screenShower.setSurface(holder.getSurface());
            screenShower.setPreviewSize(width, height);
            screenShower.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    } ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("ScreenShower", "onCreate ->");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_record);

        mTvRecord = findViewById(R.id.mTvRec);
        mTvRecord.setTotal(20000);
        if (ScreenShower.Companion.getShower() == null) {
            ScreenShower.Companion.setShower(new ScreenShower(tempPath, true));
        }
        screenShower = ScreenShower.Companion.getShower();
        mSurfaceView = findViewById(R.id.mSurfaceView);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mTvRec:
                screenShower.takePictures();
                break;
            case R.id.mTvShow:
                screenShower.recyclePreview();
                Log.e("ScreenShower" , "ScreenShower-1-Activity") ;
                ScreenShower2Activity.show(ScreenShowerActivity.this);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("ScreenShower" , "ScreenShowerActivity  onStart") ;
        mSurfaceView.getHolder().addCallback(callback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("ScreenShower" , "ScreenShowerActivity  onStop") ;
        mSurfaceView.getHolder().removeCallback(callback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("ScreenShower" , "ScreenShowerActivity onDestroy") ;
        screenShower.close();
    }
}
