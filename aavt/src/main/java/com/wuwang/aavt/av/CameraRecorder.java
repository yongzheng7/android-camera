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
package com.wuwang.aavt.av;



import com.wuwang.aavt.media.SurfaceEncoder;
import com.wuwang.aavt.media.SoundRecorder;
import com.wuwang.aavt.media.SurfaceShower;
import com.wuwang.aavt.media.VideoSurfaceProcessor;


import com.wyz.common.api.IHardStore;
import com.wyz.common.api.Renderer;
import com.wyz.common.core.camera.CameraProvider;
import com.wyz.common.core.mux.StrengthenMp4MuxStore;

/**
 * CameraRecorder2 相机预览及录制工具类
 */
public class CameraRecorder {

    private VideoSurfaceProcessor mTextureProcessor;

    private IHardStore mMuxer;

    private SurfaceShower mShower;
    private SurfaceEncoder mSurfaceStore;
    private SoundRecorder mSoundRecord;

    public CameraRecorder() {
        //用于视频混流和存储
        mMuxer = new StrengthenMp4MuxStore(true); // 存储消费中进行编码的工具
        //用于预览图像
        mShower = new SurfaceShower(); // 展示消费
        mShower.setOutputSize(720, 1280); // 设置输出大小 可以后期调整为自定义获取机器的大小
        //用于编码图像
        mSurfaceStore = new SurfaceEncoder(); // 保存消费
        mSurfaceStore.setStore(mMuxer); // 视频设置编码工具
        //用于音频
        mSoundRecord = new SoundRecorder(mMuxer);// 音频设置编码工具
        //用于处理视频图像
        VideoSurfaceProcessor videoSurfaceProcessor = new VideoSurfaceProcessor();
        videoSurfaceProcessor.setTextureProvider(new CameraProvider());// 摄像头提供者
        videoSurfaceProcessor.addObserver(mShower);
        videoSurfaceProcessor.addObserver(mSurfaceStore);
        mTextureProcessor = videoSurfaceProcessor;
    }

    public void setRenderer(Renderer renderer) {
        mTextureProcessor.setRenderer(renderer);
    }

    /**
     * 设置预览对象，必须是{@link android.view.Surface}、{@link android.graphics.SurfaceTexture}或者
     * {@link android.view.TextureView}
     *
     * @param surface 预览对象
     */
    public void setSurface(Object surface) {
        mShower.setSurface(surface);
    }

    /**
     * 设置录制的输出路径
     *
     * @param path 输出路径
     */
    public void setOutputPath(String path) {
        mMuxer.setOutputPath(path);
    }

    /**
     * 设置预览大小
     *
     * @param width  预览区域宽度
     * @param height 预览区域高度
     */
    public void setPreviewSize(int width, int height) {
        mShower.setOutputSize(width, height);
    }

    /**
     * 打开数据源
     */
    public void open() {
        mTextureProcessor.start();
    }

    /**
     * 关闭数据源
     */
    public void close() {
        mTextureProcessor.stop();
        stopRecord();
    }

    /**
     * 打开预览
     */
    public void startPreview() {
        mShower.open();
    }

    /**
     * 关闭预览
     */
    public void stopPreview() {
        mShower.close();
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        mSurfaceStore.open();
        mSoundRecord.start();
    }

    /**
     * 关闭录制
     */
    public void stopRecord() {
        mSoundRecord.stop();
        mSurfaceStore.close();
        try {
            mMuxer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
