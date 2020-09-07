package com.wuwang.aavt.examples;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wuwang.aavt.av.CameraRecorder;
import com.wuwang.aavt.av.CameraRecorder2;
import com.wuwang.aavt.gl.BeautyFilter;
import com.wuwang.aavt.gl.GroupFilter;
import com.wuwang.aavt.gl.StickFigureFilter;
import com.wuwang.aavt.gl.WaterMarkFilter;

import java.io.IOException;
import java.util.List;

public class CameraRecorderOldActivity extends AppCompatActivity {

    private CameraView mSurfaceView;
    private TextView mTvPreview, mTvRecord;
    private boolean isPreviewOpen = false;
    private boolean isRecordOpen = false;

    private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_record_old);
        mSurfaceView = findViewById(R.id.mSurfaceViewtest);
        mTvRecord = findViewById(R.id.mTvRec);
        mTvPreview = findViewById(R.id.mTvShow);
        isPreviewOpen = true ;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mTvShow:
                isPreviewOpen = !isPreviewOpen;
                mTvPreview.setText(isPreviewOpen ? "关预览" : "开预览");
                if (isPreviewOpen) {
                    mSurfaceView.startPreview();
                } else {
                    mSurfaceView.stopPreview();
                }
                break;
            case R.id.mTvRec:
                isRecordOpen = !isRecordOpen;
                mTvRecord.setText(isRecordOpen ? "关录制" : "开录制");
                if (isRecordOpen) {
                    mSurfaceView.startRecord(tempPath);
                } else {
                    mSurfaceView.stopRecord();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent v = new Intent(Intent.ACTION_VIEW);
                            v.setDataAndType(Uri.parse(tempPath), "video/mp4");
                            if (v.resolveActivity(getPackageManager()) != null) {
                                startActivity(v);
                            } else {
                                Toast.makeText(CameraRecorderOldActivity.this,
                                        "无法找到默认媒体软件打开:" + tempPath, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 1000);
                }
                break;
            default:
                break;
        }
    }

}
