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
package com.wuwang.aavt.media.hard;

import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.wyz.common.api.IHardStore;
import com.wyz.common.core.base.HardMediaData;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * StrengthenMp4MuxStore
 *
 * @author wuwang
 * @version v1.0 2017:11:08 17:15
 */
public class StrengthenMp4MuxStore implements IHardStore {

    private MediaMuxer mMuxer;
    private final boolean av;
    private String path;
    private int audioTrack = -1;
    private int videoTrack = -1;
    private final Object Lock = new Object();
    private boolean muxStarted = false;
    private LinkedBlockingQueue<HardMediaData> cache;
    private Recycler<HardMediaData> recycler;
    private ExecutorService exec;

    public StrengthenMp4MuxStore(boolean av) {
        this.av = av;
        cache = new LinkedBlockingQueue<>(30);
        recycler = new Recycler<>();
        exec = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>(16), Executors.defaultThreadFactory());
    }

    @Override
    public void close() throws Exception {
        synchronized (Lock) {
            if (muxStarted) {
                audioTrack = -1;
                videoTrack = -1;
                muxStarted = false;
            }
        }
    }

    private void muxRun() {
        while (muxStarted) {
            try {
                HardMediaData data = cache.poll(1, TimeUnit.SECONDS);
                synchronized (Lock) {
                    if (muxStarted && data != null) {
                        mMuxer.writeSampleData(data.getIndex(), data.getData(), data.getInfo());
                        recycler.put(data.getIndex(), data);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            mMuxer.stop();
            mMuxer.release();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        mMuxer = null;
        cache.clear();
        recycler.clear();
    }

    @Override
    public int addTrack(MediaFormat mediaFormat) {
        int ret = -1;
        synchronized (Lock) {
            if (!muxStarted) {
                if (audioTrack == -1 && videoTrack == -1) {
                    try {
                        mMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {
                    audioTrack = mMuxer.addTrack(mediaFormat);
                    ret = audioTrack;
                } else if (mime.startsWith("video")) {
                    videoTrack = mMuxer.addTrack(mediaFormat);
                    ret = videoTrack;
                }
                startMux();
            }
        }
        return ret;
    }

    private void startMux() {
        boolean canMux = !av || (audioTrack != -1 && videoTrack != -1);
        if (canMux) {
            mMuxer.start();
            muxStarted = true;
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    muxRun();
                }
            });
        }
    }

    @Override
    public int addData(int track, HardMediaData hardMediaData) {
        if (track >= 0) {
            hardMediaData.setIndex(track);
            if (track == audioTrack || track == videoTrack) {
                HardMediaData d = recycler.poll(track);
                if (d == null) {
                    d = hardMediaData.copy();
                } else {
                    hardMediaData.copyTo(d);
                }
                while (!cache.offer(d)) {
                    HardMediaData c = cache.poll();
                    recycler.put(c.getIndex(), c);
                }
            }
        }
        return 0;
    }

    @Override
    public void setOutputPath(String path) {
        this.path = path;
    }

}
