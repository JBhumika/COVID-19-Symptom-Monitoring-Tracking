package com.bhumi.assignment.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;


//Method for Frame extraction and heart rate calculation
public class HeartSenseService extends Service {
    public HeartSenseService() {
    }

    @Override
    public void onCreate(){
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myvideo.mp4");
            AndroidFrameConverter converterToBitmap = new AndroidFrameConverter();
            grabber.start();
            int frameCount = 0;
            for(frameCount = 0; frameCount <grabber.getLengthInFrames(); frameCount++) {
                Frame nthFrame = grabber.grabImage();
                Bitmap bitmap = converterToBitmap.convert(nthFrame);

            }
            Toast.makeText(this,"No of frames from video: " + String.valueOf(frameCount),Toast.LENGTH_LONG).show();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
