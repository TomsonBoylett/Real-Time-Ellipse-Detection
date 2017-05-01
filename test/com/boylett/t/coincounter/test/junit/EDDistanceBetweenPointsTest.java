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

/**
 *
 * @author tomson
 */
public class EDDistanceBetweenPointsTest {
    private static final double EPSILON = 0.0001;
    private EllipseDetection ed;
    private Method distance; 
    double[][][] points =
    {
        {
            {1,1},
            {1,1}
        },
        {
            {1,1},
            {1,2}
        },
        {
            {-5,-3},
            {-1,-2}
        },
        {
            {0,0},
            {-5,-3}
        },
        {
            {0,0},
            {3,5}
        },
        {
            {-6,-7},
            {3,5}
        },
    };
    
    private double distance(double[] p1, double[] p2) {
        double d = 0.0;
        try {
            d = (double) distance.invoke(ed, p1, p2);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return d;
    }
    
    @Before
    public void setUp() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ed = new EllipseDetection(new Mat());
        try {
            distance = ed.getClass().getDeclaredMethod("distance", double[].class, double[].class);
            distance.setAccessible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void oneOneOneOne() {
        assertEquals(0.0, distance(points[0][0], points[0][1]), EPSILON);
    }
    
    @Test
    public void oneOneOneTwo() {
        assertEquals(1.0, distance(points[1][0], points[1][1]), EPSILON);
    }
    
    @Test
    public void mFiveMThreeMOneMTwo() {
        assertEquals(4.123106, distance(points[2][0], points[2][1]), EPSILON);
    }
    
    @Test
    public void zeroZeroMFiveMThree() {
        assertEquals(5.830952, distance(points[3][0], points[3][1]), EPSILON);
    }
    
    @Test
    public void zeroZeroThreeFive() {
        assertEquals(5.830952, distance(points[4][0], points[4][1]), EPSILON);
    }
    
    @Test
    public void mSixmSevenThreeFive() {
        assertEquals(15.0, distance(points[5][0], points[5][1]), EPSILON);
    }
}
