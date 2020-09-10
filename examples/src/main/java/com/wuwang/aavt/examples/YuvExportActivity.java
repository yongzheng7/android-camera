/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wuwang.aavt.examples;

import android.graphics.Bitmap;
import android.opengl.EGLSurface;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wyz.common.api.FrameDrawedListener;
import com.wyz.common.core.gl.LazyShader;
import com.wyz.common.core.gl.yuv.YuvOutputShader;
import com.wyz.common.core.processor.SurfaceProcessor;
import com.wyz.common.core.texture.consume.FrameShower;
import com.wyz.common.core.base.FrameBean;
import com.wyz.common.core.base.FrameBuffer;
import com.wyz.common.core.texture.provider.CameraProvider;

import org.jetbrains.annotations.NotNull;

/**
 * YuvExportActivity
 *
 * @author wuwang
 * @version v1.0 2017:10:28 10:27
 */
public class YuvExportActivity extends AppCompatActivity {

    private SurfaceProcessor mProcessor;
    private CameraProvider mProvider;
    private FrameShower mShower;
    private FrameBuffer mFb;
    private YuvOutputShader mOutputFilter;
    private LazyShader mCutScene;
    private byte[] tempBuffer;
    private boolean exportFlag=false;
    private ImageView mImage;
    private Bitmap mBitmap;
    private int picX=368;
    private int picY=640;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__export_yuv);
        cameraInit();
        mImage= (ImageView) findViewById(R.id.mImage);
        SurfaceView view= (SurfaceView) findViewById(R.id.mSurfaceView);
        view.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mShower.open();
                mShower.setSurface(holder.getSurface());
                mShower.setOutputSize(width, height);
                mProcessor.start();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mProcessor.stop();
                mShower.close();
            }
        });

    }

    private void cameraInit(){
        mShower=new FrameShower();
        mProvider=new CameraProvider();
        mProcessor=new SurfaceProcessor();
        mProcessor.setTextureProvider(mProvider);
        mProcessor.addObserver(mShower);
        mFb=new FrameBuffer();
        mShower.setOnDrawEndListener(new FrameDrawedListener() {
            @Override
            public void onDrawEnd( EGLSurface surface, @NotNull FrameBean bean) {
                if(exportFlag){
                    if(mOutputFilter==null){
                        mOutputFilter=new YuvOutputShader(YuvOutputShader.Companion.getEXPORT_TYPE_NV21());
                        mOutputFilter.create();
                        mOutputFilter.sizeChanged(picX,picY);
                        mOutputFilter.setInputTextureSize(bean.getSourceWidth(),bean.getSourceHeight());
                        tempBuffer=new byte[picX*picY*3/2];
                    }

                    mOutputFilter.drawToTexture(bean.getTextureId());
                    mOutputFilter.getOutput(tempBuffer,0,picX*picY*3/2);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mBitmap!=null){
                                mBitmap.recycle();
                                mBitmap=null;
                            }
                            mBitmap=rawByteArray2RGBABitmap2(tempBuffer,picX,picY);
                            mImage.setImageBitmap(mBitmap);
                            mImage.setVisibility(View.VISIBLE);
                        }
                    });
                    exportFlag=false;
                }
            }
        });
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.mTvCap:
                exportFlag=true;
                break;
            case R.id.mImage:
                mImage.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    public Bitmap rawByteArray2RGBABitmap2(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;
                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0 , width, 0, 0, width, height);
        return bmp;
    }

}
