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
public class EDClosestPointArcTest {
    private EllipseDetection ed;
    private Method findClosestPointsBetweenArcs; 
    MatOfPoint[][] arcs = null;
    
    private int[] findClosestPointsBetweenArcs(MatOfPoint arc1, MatOfPoint arc2) {
        int[] a = null;
        try {
            a = (int[]) findClosestPointsBetweenArcs.invoke(ed, arc1, arc2);
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
            findClosestPointsBetweenArcs = ed.getClass().getDeclaredMethod("findClosestPointsBetweenArcs", MatOfPoint.class, MatOfPoint.class);
            findClosestPointsBetweenArcs.setAccessible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        arcs = new MatOfPoint[][]{
            {
                new MatOfPoint(
                        new Point(2.53, 1.34),
                        new Point(1.32, 3.06),
                        new Point(3.36, 4.73)
                ),
                new MatOfPoint(
                        new Point(14, 7.95),
                        new Point(11.09, 6.53),
                        new Point(6.52, 6.78)
                )
            },
            {
                new MatOfPoint(
                        new Point(-8.45,3.86),
                        new Point(-4.26,2.05),
                        new Point(-0.44,3.85)
                ),
                new MatOfPoint(
                        new Point(0.9,-0.21),
                        new Point(-2.28,-0.78),
                        new Point(-2.2,-4.12)
                )
            },
            {
                new MatOfPoint(
                        new Point(18.6,4),
                        new Point(47.3,3.14),
                        new Point(35.6,23.14)
                ),
                new MatOfPoint(
                        new Point(-7.7,9.44),
                        new Point(-33.5,-1.3),
                        new Point(-19.3,-8.2)
                )
            }
        };
    }
    
    @Test
    public void test1() {
        int[] actual = findClosestPointsBetweenArcs(arcs[0][0], arcs[0][1]);
        int[] expected = {2, 2};
        assertArrayEquals(expected, actual);
    }
    
    @Test
    public void test2() {
        int[] actual = findClosestPointsBetweenArcs(arcs[1][0], arcs[1][1]);
        int[] expected = {2, 0};
        assertArrayEquals(expected, actual);
    }
    
    @Test
    public void test3() {
        int[] actual = findClosestPointsBetweenArcs(arcs[2][0], arcs[2][1]);
        int[] expected = {0, 0};
        assertArrayEquals(expected, actual);
    }
}
