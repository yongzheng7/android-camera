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
package com.wuwang.aavt.media;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLSurface;


import com.wyz.common.api.IHardStore;
import com.wyz.common.core.base.HardMediaData;
import com.wyz.common.utils.CodecUtil;

import java.io.IOException;

/**
 * SurfaceEncoder 从surface上进行硬编码，通过{@link #setStore(IHardStore)}来设置存储器进行存储
 *
 * @author wuwang
 * @version v1.0 2017:10:27 08:29
 */
public class SurfaceEncoder extends SurfaceShower{

    private static final int TIME_OUT=1000;
    private MediaConfig mConfig=new MediaConfig();
    private MediaCodec mVideoEncoder;
    private boolean isEncodeStarted=false;


    private IHardStore mStore;
    private int mVideoTrack=-1;

    private OnDrawEndListener mListener;
    private long startTime=-1;

    public SurfaceEncoder(){
        super.setOnDrawEndListener(new OnDrawEndListener() {
            @Override
            public void onDrawEnd(EGLSurface surface, FrameBean bean) {
                if(bean.timeStamp!=-1){
                    bean.egl.setPresentationTime(surface,bean.timeStamp*1000);
                }else{
                    if(startTime==-1){
                        startTime=bean.textureTime;
                    }
                    bean.egl.setPresentationTime(surface,bean.textureTime-startTime);
                }
                videoEncodeStep(false);
                if(mListener!=null){
                    mListener.onDrawEnd(surface,bean);
                }
            }
        });
    }

    @Override
    public void run(FrameBean rb) {
        if (rb.endFlag){
            videoEncodeStep(true);
        }
        super.run(rb);
    }

    public void setConfig(MediaConfig config){
        this.mConfig=config;
    }

    public void setStore(IHardStore store){
        this.mStore=store;
    }

    @Override
    public void setOutputSize(int width, int height) {
        super.setOutputSize(width, height);
        mConfig.mVideo.width=width;
        mConfig.mVideo.height=height;
    }

    protected MediaFormat convertVideoConfigToFormat(MediaConfig.Video config){
        MediaFormat format=MediaFormat.createVideoFormat(config.mime,config.width,config.height);
        format.setInteger(MediaFormat.KEY_BIT_RATE,config.bitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE,config.frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,config.iframe);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        return format;
    }

    private void openVideoEncoder(){
        if(mVideoEncoder==null){
            try {
                MediaFormat format=convertVideoConfigToFormat(mConfig.mVideo);
                mVideoEncoder= MediaCodec.createEncoderByType(mConfig.mVideo.mime);
                mVideoEncoder.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
                super.setSurface(mVideoEncoder.createInputSurface());
                super.setOutputSize(mConfig.mVideo.width,mConfig.mVideo.height);
                mVideoEncoder.start();
                isEncodeStarted=true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeVideoEncoder(){
        if(mVideoEncoder!=null){
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder=null;
        }
    }



    private synchronized boolean videoEncodeStep(boolean isEnd){
        if(isEncodeStarted){
            if(isEnd){
                mVideoEncoder.signalEndOfInputStream();
            }
            MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
            while (true){
                int mOutputIndex=mVideoEncoder.dequeueOutputBuffer(info,TIME_OUT);
                if(mOutputIndex>=0){
                    if((info.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0){
                        info.size=0;
                    }
                    if(mStore!=null){
                        mStore.addData(mVideoTrack,new HardMediaData(CodecUtil.Companion.getOutputBuffer(mVideoEncoder,mOutputIndex),info));
                    }
                    mVideoEncoder.releaseOutputBuffer(mOutputIndex,false);
                    if((info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0){
                        closeVideoEncoder();
                        isEncodeStarted=false;
                        break;
                    }
                }else if(mOutputIndex== MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                    MediaFormat format=mVideoEncoder.getOutputFormat();
                    if(mStore!=null){
                        mVideoTrack=mStore.addTrack(format);
                    }
                }else if(mOutputIndex== MediaCodec.INFO_TRY_AGAIN_LATER&&!isEnd){
                    break;
                }
            }
        }
        return false;
    }


    @Override
    public void open() {
        openVideoEncoder();
        super.open();
    }

    @Override
    public void close() {
        super.close();
        videoEncodeStep(true);
        startTime=-1;
    }

    @Override
    public void setOnDrawEndListener(OnDrawEndListener listener) {
        this.mListener=listener;
    }

    @Override
    public void setSurface(Object surface) {}


}

