package com.wyz.camera.test;

import android.graphics.SurfaceTexture;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.wyz.camera.api.Renderer;
import com.wyz.camera.core.base.FrameBuffer;
import com.wyz.camera.core.egl.EGLConfigAttrs;
import com.wyz.camera.core.egl.EGLContextAttrs;
import com.wyz.camera.core.egl.EGLHelper;
import com.wyz.camera.core.gl.AbsWrapShader;
import com.wyz.camera.core.gl.LazyShader;
import com.wyz.camera.core.gl.OesWrapShader;
import com.wyz.camera.utils.MatrixUtils;
import com.wyz.camera.utils.TextureUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

@Deprecated
class CameraRecorder {

    private final int TIME_OUT=1000;

    private MediaCodec mVideoEncoder;
    private MediaCodec mAudioEncoder;
    private AudioRecord mAudioRecord;
    private MediaMuxer mMuxer;

    private int mRecordBufferSize=0;
    private int mRecordSampleRate=48000;   //音频采样率
    private int mRecordChannelConfig= AudioFormat.CHANNEL_IN_STEREO;   //音频录制通道,默认为立体声
    private int mRecordAudioFormat=AudioFormat.ENCODING_PCM_16BIT; //音频录制格式，默认为PCM16Bit

    private SurfaceTexture mInputTexture;
    private Surface mOutputSurface;
    private Surface mEncodeSurface;
    private EGLHelper mShowEGLHelper;
    private Configuration mConfig;
    private String mOutputPath;

    private MediaCodec.BufferInfo mAudioEncodeBufferInfo;
    private MediaCodec.BufferInfo mVideoEncodeBufferInfo;
    private int mAudioTrack=-1;
    private int mVideoTrack=-1;

    private boolean mGLThreadFlag=false;
    private AbsWrapShader mRenderer;
    private Semaphore mSem;
    private Semaphore mEncodeSem;
    private boolean isMuxStarted=false;
    private int mInputTextureId;
    private EGLSurface mEGLEncodeSurface=null;

    private int mPreviewWidth=0;                //预览的宽度
    private int mPreviewHeight=0;               //预览的高度
    private int mOutputWidth=0;                 //输出的宽度
    private int mOutputHeight=0;                //输出的高度

    private boolean isRecordStarted=false;
    private boolean isRecordVideoStarted=false;
    private boolean isRecordAudioStarted=false;
    private boolean isTryStopAudio=false;

    private Thread mGLThread;
    private Thread mAudioThread;

    private final Object VIDEO_LOCK=new Object();
    private final Object REC_LOCK=new Object();

    private final static long BASE_TIME=System.currentTimeMillis();

    public CameraRecorder(){
        mShowEGLHelper=new EGLHelper();
//        mEncodeEGLHelper=new EGLHelper();
        mSem=new Semaphore(0);
        mAudioEncodeBufferInfo=new MediaCodec.BufferInfo();
        mVideoEncodeBufferInfo=new MediaCodec.BufferInfo();
    }


    public void setOutputPath(String path){
        this.mOutputPath=path;
    }

    public void setOutputSize(int width,int height){
        this.mConfig=new Configuration(width,height);
        this.mOutputWidth=width;
        this.mOutputHeight=height;
    }

    public void setPreviewSize(int width,int height){
        this.mPreviewWidth=width;
        this.mPreviewHeight=height;
    }

    public SurfaceTexture createInputSurfaceTexture(){
        mInputTextureId= TextureUtils.Companion.createTextureID(true);
        mInputTexture=new SurfaceTexture(mInputTextureId);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mInputTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        mSem.release();
                    }
                });
            }
        });
        return mInputTexture;
    }

    public void setConfiguration(Configuration config){
        this.mConfig=config;
    }

    public void setOutputSurface(Surface surface){
        this.mOutputSurface=surface;
    }

    public void setRenderer(Renderer renderer){
        mRenderer=new OesWrapShader(renderer);
    }

    public void startPreview(){
        synchronized (REC_LOCK){
            mSem.drainPermits();
            mGLThreadFlag=true;
            mGLThread=new Thread(mGLRunnable);
            mGLThread.start();
        }
    }

    public void stopPreview() throws InterruptedException {
        synchronized (REC_LOCK){
            mGLThreadFlag=false;
            mSem.release();
            if(mGLThread!=null&&mGLThread.isAlive()){
                mGLThread.join();
                mGLThread=null;
            }
        }
    }

    public void startRecord() throws IOException {
        synchronized (REC_LOCK){
            isRecordStarted=true;
            MediaFormat audioFormat=mConfig.getAudioFormat();
            mAudioEncoder=MediaCodec.createEncoderByType(audioFormat.getString(MediaFormat.KEY_MIME));
            mAudioEncoder.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            MediaFormat videoFormat=mConfig.getVideoFormat();
            mVideoEncoder=MediaCodec.createEncoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
            //此处不能用mOutputSurface，会configure失败
            mVideoEncoder.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncodeSurface=mVideoEncoder.createInputSurface();
            mAudioEncoder.start();
            mVideoEncoder.start();
            mMuxer=new MediaMuxer(mOutputPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mRecordBufferSize = AudioRecord.getMinBufferSize(mRecordSampleRate, mRecordChannelConfig, mRecordAudioFormat)*2;
//        buffer=new byte[bufferSize];
            mAudioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,mRecordSampleRate,mRecordChannelConfig, mRecordAudioFormat,mRecordBufferSize);

            mAudioThread=new Thread(new Runnable() {
                @Override
                public void run() {
                    mAudioRecord.startRecording();
                    while (!audioEncodeStep(isTryStopAudio)){};
                    mAudioRecord.stop();
                }
            });
            mAudioThread.start();
            isRecordAudioStarted=true;
        }
    }

    public void stopRecord() throws InterruptedException {
        synchronized (REC_LOCK){
            if(isRecordStarted){
                isTryStopAudio=true;
                if(isRecordAudioStarted){
                    mAudioThread.join();
                    isRecordAudioStarted=false;
                }
                synchronized (VIDEO_LOCK){
                    if(isRecordVideoStarted){
                        mEGLEncodeSurface=null;
                        videoEncodeStep(true);
                    }
                    isRecordVideoStarted=false;
                }
                mAudioEncoder.stop();
                mAudioEncoder.release();
                mVideoEncoder.stop();
                mVideoEncoder.release();
                try {
                    if(isMuxStarted){
                        isMuxStarted=false;
                        mMuxer.stop();
                        mMuxer.release();
                    }
                }catch (IllegalStateException e){
                    e.printStackTrace();
                    File file=new File(mOutputPath);
                    if(file.exists()&&file.delete()){
                        Log.e("com_wyz_CameraRecorder" ,"delete error file :"+mOutputPath);
                    }
                }

                mAudioEncoder=null;
                mVideoEncoder=null;
                mMuxer=null;

                mAudioTrack=-1;
                mVideoTrack=-1;

                isRecordStarted=false;
            }
        }
    }

    private Runnable mGLRunnable=new Runnable() {
        @Override
        public void run() {
            if(mOutputSurface==null){
                return;
            }
            if(mPreviewWidth<=0||mPreviewHeight<=0){
                return;
            }
            boolean ret=mShowEGLHelper.createGLESWithSurface(new EGLConfigAttrs(),new EGLContextAttrs(),mOutputSurface);
            if(!ret){
                return;
            }
            if(mRenderer==null){
                mRenderer=new OesWrapShader(null);
            }
            mRenderer.setFlag(AbsWrapShader.Companion.getTYPE_1379());
            mRenderer.create();
            int[] t=new int[1];
            GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING,t,0);
            mRenderer.sizeChanged(mPreviewWidth,mPreviewHeight);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,t[0]);

            LazyShader mShowFilter=new LazyShader();

            MatrixUtils.Companion.getMatrix(
                    mShowFilter.getVertexMatrix()
                    ,MatrixUtils.Companion.getTYPE_CENTERCROP()
                    ,mPreviewWidth,mPreviewHeight,
                    mOutputWidth,mOutputHeight);
            MatrixUtils.Companion.flip(mShowFilter.getVertexMatrix(),false,false);
            mShowFilter.create();
            mShowFilter.sizeChanged(mPreviewWidth,mPreviewHeight);

            LazyShader mRecFilter=new LazyShader();
            MatrixUtils.Companion.getMatrix(
                    mRecFilter.getVertexMatrix(),
                    MatrixUtils.Companion.getTYPE_CENTERCROP(),
                    mPreviewWidth,mPreviewHeight,
                    mOutputWidth,mOutputHeight);
            MatrixUtils.Companion.flip(mRecFilter.getVertexMatrix(),false,false);
            mRecFilter.create();
            mRecFilter.sizeChanged(mOutputWidth,mOutputHeight);
            FrameBuffer mEncodeFrameBuffer=new FrameBuffer();
            while (mGLThreadFlag){
                try {
                    mSem.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("com_wyz_Runnable" , "CameraRecorder GLThread mGLThreadFlag ="+mGLThreadFlag);
                if(mGLThreadFlag){
                    long time=(System.currentTimeMillis()-BASE_TIME)*1000;
                    mInputTexture.updateTexImage();
                    mInputTexture.getTransformMatrix(mRenderer.getTextureMatrix());
                    Log.e("com_wyz_Runnable" , "CameraRecorder updateTexImage ="+mInputTexture);
                    synchronized (VIDEO_LOCK){
                        if(isRecordVideoStarted){
                            Log.e("com_wyz_Runnable" , "CameraRecorder isRecordVideoStarted 10="+isRecordVideoStarted);
                            if(mEGLEncodeSurface==null){
                                mEGLEncodeSurface=mShowEGLHelper.createWindowSurface(mEncodeSurface);
                            }
                            mShowEGLHelper.makeCurrent(mEGLEncodeSurface);
                            mEncodeFrameBuffer.bindFrameBuffer(mPreviewWidth,mPreviewHeight);
                            mRenderer.draw(mInputTextureId);
                            mEncodeFrameBuffer.unBindFrameBuffer();
                            GLES20.glViewport(0,0,mConfig.getVideoFormat().getInteger(MediaFormat.KEY_WIDTH),
                                    mConfig.getVideoFormat().getInteger(MediaFormat.KEY_HEIGHT));
                            mRecFilter.draw(mEncodeFrameBuffer.getCacheTextureId());
                            mShowEGLHelper.setPresentationTime(mEGLEncodeSurface,time*1000);
                            videoEncodeStep(false);
                            mShowEGLHelper.swapBuffers(mEGLEncodeSurface);

                            mShowEGLHelper.makeCurrent();
                            GLES20.glViewport(0,0,mPreviewWidth,mPreviewHeight);
                            mShowFilter.draw(mEncodeFrameBuffer.getCacheTextureId());
                            mShowEGLHelper.setPresentationTime(mShowEGLHelper.getDefaultSurface(),0);
                            mShowEGLHelper.swapBuffers(mShowEGLHelper.getDefaultSurface());
                            Log.e("com_wyz_Runnable" , "CameraRecorder isRecordVideoStarted 11="+isRecordVideoStarted);
                        }else{
                            Log.e("com_wyz_Runnable" , "CameraRecorder isRecordVideoStarted 20="+isRecordVideoStarted);
                            GLES20.glViewport(0,0,mPreviewWidth,mPreviewHeight);
                            mRenderer.draw(mInputTextureId);
                            mShowEGLHelper.swapBuffers(mShowEGLHelper.getDefaultSurface());
                        }
                    }
                }
            }
            mShowEGLHelper.destroyGLES(mShowEGLHelper.getDefaultSurface(),mShowEGLHelper.getDefaultContext());
        }
    };

    private boolean videoEncodeStep(boolean isEnd){
        if(isEnd){
            mVideoEncoder.signalEndOfInputStream();
        }
        while (true){
            int outputIndex=mVideoEncoder.dequeueOutputBuffer(mVideoEncodeBufferInfo,TIME_OUT);
            if(outputIndex>=0){
                if(isMuxStarted&&mVideoEncodeBufferInfo.size>0&&mVideoEncodeBufferInfo.presentationTimeUs>0){
                    mMuxer.writeSampleData(mVideoTrack,getOutputBuffer(mVideoEncoder,outputIndex),mVideoEncodeBufferInfo);
                }
                mVideoEncoder.releaseOutputBuffer(outputIndex,false);
                if(mVideoEncodeBufferInfo.flags==MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                    Log.e("com_wyz_CameraRecorder" ,"CameraRecorder get video encode end of stream");
                    return true;
                }
            }else if(outputIndex==MediaCodec.INFO_TRY_AGAIN_LATER){
                break;
            }else if(outputIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                Log.e("com_wyz_CameraRecorder" ,"get video output format changed ->"+mVideoEncoder.getOutputFormat().toString());
                mVideoTrack=mMuxer.addTrack(mVideoEncoder.getOutputFormat());
                mMuxer.start();
                isMuxStarted=true;
            }
        }
        return false;
    }

    private boolean audioEncodeStep(boolean isEnd){
        if(isRecordAudioStarted){
            int inputIndex=mAudioEncoder.dequeueInputBuffer(TIME_OUT);
            if(inputIndex>=0){
                ByteBuffer buffer=getInputBuffer(mAudioEncoder,inputIndex);
                buffer.clear();
                long time=(System.currentTimeMillis()-BASE_TIME)*1000;
                int length=mAudioRecord.read(buffer,mRecordBufferSize);
                if(length>=0){
                    mAudioEncoder.queueInputBuffer(inputIndex,0,length,time,
                            isEnd?MediaCodec.BUFFER_FLAG_END_OF_STREAM:0);
                }
            }
            while (true){
                int outputIndex=mAudioEncoder.dequeueOutputBuffer(mAudioEncodeBufferInfo,TIME_OUT);
                if(outputIndex>=0){
                    //todo 第一帧音频时间戳为0的问题
                    if(isMuxStarted&&mAudioEncodeBufferInfo.size>0&&mAudioEncodeBufferInfo.presentationTimeUs>0){
                        mMuxer.writeSampleData(mAudioTrack,getOutputBuffer(mAudioEncoder,outputIndex),mAudioEncodeBufferInfo);
                    }
                    mAudioEncoder.releaseOutputBuffer(outputIndex,false);
                    if(mAudioEncodeBufferInfo.flags==MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                        Log.e("com_wyz_CameraRecorder" ,"CameraRecorder get audio encode end of stream");
                        isTryStopAudio=false;
                        isRecordAudioStarted=false;
                        return true;
                    }
                }else if(outputIndex==MediaCodec.INFO_TRY_AGAIN_LATER){
                    break;
                }else if(outputIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                    Log.e("com_wyz_CameraRecorder" ,"get audio output format changed ->"+mAudioEncoder.getOutputFormat().toString());
                    synchronized (VIDEO_LOCK){
                        mAudioTrack=mMuxer.addTrack(mAudioEncoder.getOutputFormat());
                        isRecordVideoStarted=true;
                    }
                }
            }
        }
        return false;
    }

    public static class Configuration{

        private MediaFormat mAudioFormat;
        private MediaFormat mVideoFormat;

        public Configuration(int width,int height){
            mAudioFormat=MediaFormat.createAudioFormat("audio/mp4a-latm",48000,2);
            mAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE,128000);
            mAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

            mVideoFormat=MediaFormat.createVideoFormat("video/avc",width,height);
            mVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,24);
            mVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);
            mVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE,width*height*5);
            mVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        }

        public MediaFormat getAudioFormat(){
            return mAudioFormat;
        }

        public MediaFormat getVideoFormat(){
            return mVideoFormat;
        }

    }

    private ByteBuffer getInputBuffer(MediaCodec codec, int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getInputBuffer(index);
        }else{
            return codec.getInputBuffers()[index];
        }
    }

    private ByteBuffer getOutputBuffer(MediaCodec codec, int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getOutputBuffer(index);
        }else{
            return codec.getOutputBuffers()[index];
        }
    }

}
