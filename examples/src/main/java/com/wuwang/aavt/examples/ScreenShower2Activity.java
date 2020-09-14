package com.wuwang.aavt.examples;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.wyz.camera.ScreenShower;
import com.wyz.camera.core.gl.GroupShader;
import com.wyz.camera.core.gl.func.FluorescenceShader;
import com.wyz.camera.view.CircularProgressView;
import com.wyz.common.ui.AbsActionBarActivity;


public class ScreenShower2Activity extends AbsActionBarActivity {

    private SurfaceView mSurfaceView;

    private ScreenShower screenShower;

    public static void show(Context context){
        Intent intent = new Intent();
        intent.setClass(context , ScreenShower2Activity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("CameraActivity", "onCreate ->");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_record2);
        screenShower = ScreenShower.Companion.getShower();
        mSurfaceView = findViewById(R.id.mSurfaceView2);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                GroupShader filter = new GroupShader(getResources());
                //screenShower.setRenderer(filter);
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
                Log.e("FrameShower" , "surfaceChanged 1 ") ;
                //screenShower.open();
                screenShower.setSurface(holder.getSurface());
                Log.e("FrameShower" , "surfaceChanged 2 ") ;

                screenShower.setPreviewSize(width, height);
                Log.e("FrameShower" , "surfaceChanged 3 ") ;
                screenShower.startPreview();
                Log.e("FrameShower" , "surfaceChanged 4 ") ;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                screenShower.recyclePreview();
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
                Toast.makeText(this, "ScreenShower-2-Activity ", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
