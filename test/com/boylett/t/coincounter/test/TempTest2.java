/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter.test;

import com.atul.JavaOpenCV.ImShow;
import com.boylett.t.coincounter.EllipseDetection;
import com.boylett.t.coincounter.QuadrantSet;
import com.boylett.t.coincounter.QuadrantSetDetection;
import java.io.IOException;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author tomson
 */
public class TempTest2 {
    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat m = Imgcodecs.imread("/home/tomson/Documents/CoinCounting/test3.jpg");
        EllipseDetection ed = new EllipseDetection.Builder().build();
        List<RotatedRect> es = ed.detect(m);
        for (RotatedRect e : es) {
            Imgproc.ellipse(m, e, new Scalar(255, 0, 0), 2);
        }
        
        ImShow im = new ImShow("Test");
        im.showImage(m);
    }
    
    private static double distance(double p1X, double p1Y, double p2X, double p2Y) {
        return Math.sqrt(Math.pow(p1X - p2X, 2) + Math.pow(p1Y - p2Y, 2));
    }
}
