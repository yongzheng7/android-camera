package com.wuwang.aavt.examples;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wuwang.aavt.av.CameraRecorder;
import com.wuwang.aavt.gl.BeautyFilter;
import com.wuwang.aavt.gl.GroupFilter;
import com.wuwang.aavt.gl.StickFigureFilter;
import com.wuwang.aavt.gl.WaterMarkFilter;

public class CameraRecorderActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private TextView mTvPreview,mTvRecord;
    private boolean isPreviewOpen=false;
    private boolean isRecordOpen=false;
    private int mCameraWidth,mCameraHeight;

    private CameraRecorder mCamera;

    private String tempPath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("CameraActivity" ,"onCreate ->");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_record);
        mSurfaceView=  findViewById(R.id.mSurfaceView);
        mTvRecord=  findViewById(R.id.mTvRec);
        mTvPreview=  findViewById(R.id.mTvShow);

        mCamera =new CameraRecorder();
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                GroupFilter filter=new GroupFilter(getResources());
                mCamera.setRenderer(filter);
                //filter.addFilter(new StickFigureFilter(getResources()));
                filter.addFilter(new BeautyFilter(getResources()).setBeautyLevel(4));
                filter.addFilter(new WaterMarkFilter().setMarkPosition(30,10,100,76).setMark(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)));
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mCamera.setOutputPath(tempPath);
                mCamera.open();
                mCamera.setSurface(holder.getSurface());
                mCamera.setPreviewSize(width, height);
                mCamera.startPreview();
                isPreviewOpen=true;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCamera.stopPreview();
                mCamera.close();
            }
        });
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.mTvShow:
                isPreviewOpen=!isPreviewOpen;
                mTvPreview.setText(isPreviewOpen?"关预览":"开预览");
                if(isPreviewOpen){
                    mCamera.startPreview();
                }else{
                    mCamera.stopPreview();
                }
                break;
            case R.id.mTvRec:
                isRecordOpen=!isRecordOpen;
                mTvRecord.setText(isRecordOpen?"关录制":"开录制");
                if(isRecordOpen){
                    mCamera.startRecord();
                }else{
                    mCamera.stopRecord();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent v=new Intent(Intent.ACTION_VIEW);
                            v.setDataAndType(Uri.parse(tempPath),"video/mp4");
                            if(v.resolveActivity(getPackageManager()) != null){
                                startActivity(v);
                            }else{
                                Toast.makeText(CameraRecorderActivity.this,
                                        "无法找到默认媒体软件打开:"+tempPath, Toast.LENGTH_SHORT).show();
                            }
                        }
                    },1000);
                }
                break;
                default:
                    break;
        }
    }

}
