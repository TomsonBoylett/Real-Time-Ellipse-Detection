/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter.test.junit;

import com.boylett.t.coincounter.EllipseDetection;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import static org.junit.Assert.*;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

/**
 *
 * @author tomson
 */
public class EDGetPointsTest {
    private EllipseDetection ed;
    private Method getPoints;
    private static final int UNCERTAINTY = 2;
    MatOfPoint[][] arcs = null;
    
    private Point[] getPoints(MatOfPoint arc1, MatOfPoint arc2) {
        Point[] a = null;
        try {
            a = (Point[]) getPoints.invoke(ed, arc1, arc2);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return a;
    }
    
    @Before
    public void setUp() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ed = new EllipseDetection(new Mat());
        try {
            getPoints = ed.getClass().getDeclaredMethod("getPoints", MatOfPoint.class, MatOfPoint.class);
            getPoints.setAccessible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        arcs = new MatOfPoint[][]{
            {
                new MatOfPoint(
                        new Point(15, 269),
                        new Point(34, 204),
                        new Point(85, 168)
                ),
                new MatOfPoint(
                        new Point(145, 165),
                        new Point(225, 177),
                        new Point(278, 222)
                )
            }
        };
    }
    
    @Test
    public void test1() {
        Point[] actual = getPoints(arcs[0][0], arcs[0][1]);
        Point[] expected = {
            new Point(79, 54),
            new Point(44, 170),
            new Point(207, 162),
        };
        assertEquals(actual.length, expected.length);
        for (int i = 0; i < actual.length; i++) {
            assertTrue("Point:" + i + " Dim:x Expected:" + expected[i].x + " Actual:" + actual[i].x,
                    Math.abs(expected[i].x - actual[i].x) < UNCERTAINTY);
            assertTrue("Point:" + i + " Dim:y Expected:" + expected[i].y + " Actual:" + actual[i].y,
                    Math.abs(expected[i].y - actual[i].y) < UNCERTAINTY);
        }
    }
}
