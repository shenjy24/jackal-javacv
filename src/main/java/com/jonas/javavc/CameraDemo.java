package com.jonas.javavc;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.swing.*;

public class CameraDemo {

    public static void main(String[] args) {
        try {
            new CameraDemo().grab();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

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
}
