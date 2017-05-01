/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter.test.junit;

import com.atul.JavaOpenCV.ImShow;
import com.boylett.t.coincounter.EllipseDetection;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

/**
 *
 * @author tomson
 */
public class EDMorePixelsAboveTest {
    
    private static List<MatOfPoint> contours;
    private static EllipseDetection ed;
    private static Method morePixelsAbove;
    
    private static void addContour(Point[] points) {
        MatOfPoint contour = new MatOfPoint();
        contour.fromArray(points);
        contours.add(contour);
    }
    
    private static boolean morePixelsAbove(int i) {
        boolean b = false;
        try {
            b = (boolean) morePixelsAbove.invoke(ed, contours.get(i));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return b;
    }
    
    @Before
    public void setUp() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        contours = new ArrayList<>();
        ed = new EllipseDetection(new Mat());
        
        try {
            morePixelsAbove = ed.getClass().getDeclaredMethod("morePixelsAbove", MatOfPoint.class);
            morePixelsAbove.setAccessible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        addContour(new Point[]{
                new Point(200,200),
                new Point(300,400),
                new Point(300,600),
                new Point(500,800)
        });
        
        addContour(new Point[]{
                new Point(200,200),
                new Point(300,500),
                new Point(400,600),
                new Point(600,800),
                new Point(500,600)
        });
        
        addContour(new Point[]{
                new Point(153,176),
                new Point(185,190),
                new Point(235,256),
                new Point(289,240),
                new Point(300,295),
                new Point(310,350)
        });
        
        addContour(new Point[]{
                new Point(100,1000),
                new Point(200,600),
                new Point(300,500),
                new Point(500,500)
        });
        
        addContour(new Point[]{
                new Point(1000,100),
                new Point(600,200),
                new Point(500,300),
                new Point(500,500)
        });
    }
    
    @Test
    public void testContour0() {
        assertTrue(morePixelsAbove(0));
    }
    
    @Test
    public void testContour1() {
        assertTrue(morePixelsAbove(1));
    }
    
    @Test
    public void testContour2() {
        assertFalse(morePixelsAbove(2));
    }
    
    @Test
    public void testContour3() {
        assertFalse(morePixelsAbove(3));
    }

    @Test
    public void testContour4() {
        assertFalse(morePixelsAbove(4));
    }
}
