package com.wuwang.aavt.examples;

import org.junit.Test;

import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private Semaphore  mSem = new Semaphore(0);
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("other Thread start");
                try {
                    mSem.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("other Thread end");
            }
        }).start();
        System.out.println("Main Thread start");
        Thread.sleep(2000);
        System.out.println("Main Thread 1");
        mSem.release();
        Thread.sleep(2000);
        System.out.println("Main Thread end");
    }
}