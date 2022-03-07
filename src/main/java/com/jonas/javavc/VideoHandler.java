package com.jonas.javavc;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.*;

public class VideoHandler {

    public static void main(String[] args) {
        try {
            VideoHandler app = new VideoHandler();
            //new CameraDemo().grab();
            app.record("output.mp4", 25);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用本地摄像头
     *
     * @throws FrameGrabber.Exception
     */
    public void grab() throws FrameGrabber.Exception {
        //新建opencv抓取器，一般的电脑和移动端设备中摄像头默认序号是0，不排除其他情况
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();//开始获取摄像头数据

        CanvasFrame canvas = new CanvasFrame("摄像头预览");//新建一个预览窗口
        canvas.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //窗口是否关闭
        while (canvas.isDisplayable()) {
            /*获取摄像头图像并在窗口中显示,这里Frame frame=grabber.grab()得到是解码后的视频图像*/
            canvas.showImage(grabber.grab());
        }
        grabber.close();//停止抓取
    }

    /**
     * 按帧录制本机摄像头视频（边预览边录制，停止预览即停止录制）
     *
     * @param outputFile  录制的文件路径，也可以是rtsp或者rtmp等流媒体服务器发布地址
     * @param frameRate   视频帧率
     */
    public void record(String outputFile, double frameRate) throws FrameGrabber.Exception, FrameRecorder.Exception, InterruptedException {
        FrameGrabber grabber = FrameGrabber.createDefault(0);
        grabber.start();

        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//转换器
        IplImage grabbedImage = converter.convert(grabber.grab());//抓取一帧视频并将其转换为图像
        int width = grabbedImage.width();
        int height = grabbedImage.height();

        FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码
        recorder.setFormat("flv");//封装格式，如果是推送到rtmp就必须是flv封装格式
        recorder.setFrameRate(frameRate);
        recorder.start();//开启录制器

        long startTime = 0;
        long videoTS = 0;
        CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        Frame rotatedFrame = converter.convert(grabbedImage);

        while (frame.isVisible() && (grabbedImage = converter.convert(grabber.grab())) != null) {
            rotatedFrame = converter.convert(grabbedImage);
            frame.showImage(rotatedFrame);
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            videoTS = 1000 * (System.currentTimeMillis() - startTime);
            recorder.setTimestamp(videoTS);
            recorder.record(rotatedFrame);
            Thread.sleep(40);
        }
        frame.dispose();//关闭窗口
        recorder.close();//关闭推流录制器，close包含release和stop操作
        grabber.close();//关闭抓取器
    }
}
