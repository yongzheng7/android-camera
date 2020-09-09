package com.wyz.common.core.base

class MediaConfig {
    private var mVideo: Video = Video()

    private var mAudio: Audio = Audio()

    fun getVideo(): Video {
        return this.mVideo
    }

    fun getAudio(): Audio {
        return this.mAudio
    }

    fun setVideo(video: Video) {
        this.mVideo = video
    }

    fun setAudio(audio: Audio) {
        this.mAudio = audio
    }


    class Video {
        var mime = "video/avc"
        var width = 368
        var height = 640
        var frameRate = 24
        var iframe = 1
        var bitrate = 1177600
        var colorFormat = 0
    }

    class Audio {
        var mime = "audio/mp4a-latm"
        var sampleRate = 48000
        var channelCount = 2
        var bitrate = 128000
        var profile = 0
    }
}