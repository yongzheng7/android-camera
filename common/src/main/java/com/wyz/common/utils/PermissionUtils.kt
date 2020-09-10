package com.wyz.common.utils

import android.hardware.Camera
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log

class PermissionUtils private constructor() {
    companion object {
        val STATE_RECORDING = -1
        val STATE_NO_PERMISSION = -2
        val STATE_SUCCESS = 1

        fun getRecordState(): Int {
            val minBuffer = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val audioRecord  = AudioRecord(MediaRecorder.AudioSource.DEFAULT, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuffer * 100)
            val point = ShortArray(minBuffer)
            var readSize = 0
            try {
                audioRecord.startRecording() //检测是否可以进入初始化状态
            } catch (e: Exception) {
                audioRecord.release()
                return STATE_NO_PERMISSION
            }
            return if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                //6.0以下机型都会返回此状态，故使用时需要判断bulid版本
                //检测是否在录音中
                audioRecord.stop()
                audioRecord.release()
                Log.d("CheckAudioPermission", "录音机被占用")
                STATE_RECORDING
            } else {
                //检测是否可以获取录音结果
                readSize = audioRecord.read(point, 0, point.size)
                if (readSize <= 0) {
                    audioRecord.stop()
                    audioRecord.release()
                    Log.d("CheckAudioPermission", "录音的结果为空")
                    STATE_NO_PERMISSION
                } else {
                    audioRecord.stop()
                    audioRecord.release()
                    STATE_SUCCESS
                }
            }
        }


        @Synchronized
        fun isCameraUseable(cameraID: Int): Boolean {
            var canUse = true
            var mCamera: Camera? = null
            try {
                mCamera = Camera.open(cameraID)
                // setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null
                val mParameters = mCamera.parameters
                mCamera.parameters = mParameters
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                canUse = false
            } finally {
                if (mCamera != null) {
                    mCamera.release()
                } else {
                    canUse = false
                }
            }
            return canUse
        }
    }


}