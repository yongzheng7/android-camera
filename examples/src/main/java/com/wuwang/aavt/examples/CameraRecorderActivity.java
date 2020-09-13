package com.wuwang.aavt.examples;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wyz.camera.CameraRecorder;
import com.wyz.camera.core.gl.GroupShader;
import com.wyz.camera.core.gl.mark.WaterMarkShader;
import com.wyz.camera.view.CircularProgressView;


public class CameraRecorderActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private TextView mTvPreview;
    private CircularProgressView mTvRecord;
    private boolean isPreviewOpen = false;
    private boolean isRecordOpen = false;

    private CameraRecorder mCamera;

    private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
               // filter.addFilter(new LazyShader());
               // filter.addFilter(new StickFigureShader(getResources()));
               // filter.addFilter(new BeautyShader(getResources()).setBeautyLevel(4));
                filter.addFilter(new WaterMarkShader().setMarkPosition(100, 100, 100, 76).setMark(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)));
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mCamera.setOutputPath(tempPath);
                mCamera.open();
                mCamera.setSurface(holder.getSurface());
                mCamera.setPreviewSize(width, height);
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
                isRecordOpen = !isRecordOpen;
                RecordProgress recordProgress = new RecordProgress(view, new ProgressListener() {
                    @Override
                    public void start() {
                        mTvRecord.setProcess(0);
                        mCamera.startRecord();
                    }

                    @Override
                    public void run(int progressSize) {
                        mTvRecord.setProcess(progressSize);
                    }

                    @Override
                    public void end() {
                        mCamera.stopRecord();
                        mTvRecord.setProcess(0);

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent v = new Intent(Intent.ACTION_VIEW);
                                v.setDataAndType(Uri.parse(tempPath), "video/mp4");
                                if (v.resolveActivity(getPackageManager()) != null) {
                                    startActivity(v);
                                } else {
                                    Toast.makeText(CameraRecorderActivity.this,
                                            "无法找到默认媒体软件打开:" + tempPath, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, 1000);
                    }
                });
                if (isRecordOpen) {
                    view.post(recordProgress);
                } else {
                    recordProgress.close();
                }
                break;
            default:
                break;
        }
    }

    class RecordProgress implements Runnable {
        private View view;
        private ProgressListener listener;
        private long startTime = -1;
        private boolean isRunning  ;

        RecordProgress(View view, ProgressListener listener) {
            this.view = view;
            this.listener = listener;
            this.isRunning = true ;
        }

        @Override
        public void run() {
            if(isRunning){
                if (mTvRecord.getProcess() >= 20000) {
                    view.removeCallbacks(this);
                    listener.end();
                    listener = null;
                    return;
                }
                long l = System.currentTimeMillis();
                if (startTime == -1) {
                    startTime = l;
                    listener.start();
                }
                listener.run((int) (l - startTime));
                view.postDelayed(this, 50);
            }else{
                startTime = -1 ;
                listener.end();
                view.removeCallbacks(this);
                listener = null;
            }
        }

        void close(){
            isRunning= false ;
        }
    }

    interface ProgressListener {
        void start();

        void run(int progressSize) ;

        void end();
    }
}
