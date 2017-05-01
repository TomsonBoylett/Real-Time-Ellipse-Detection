/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter.test.junit;

import com.boylett.t.coincounter.MatchArcs;;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

/**
 *
 * @author tomson
 */
public class MatchArcsTest {
    static final double DELTA = 0.001;
    Point[][][] arcs = null;
    
    @Before
    public void setUp() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        arcs = new Point[][][]{
            // 1
            {
                {
                        new Point(253, 134),
                        new Point(132, 306),
                        new Point(336, 473)
                },
                {
                        new Point(1400, 795),
                        new Point(1109, 653),
                        new Point(652, 678)
                },
            },
            // 2
            {
                {
                        new Point(-845,386),
                        new Point(-426,205),
                        new Point(-044,385)
                },
                {
                        new Point(90,-21),
                        new Point(-228,-78),
                        new Point(-220,-412)
                }
            },
            // 3
            {
                {
                        new Point(1860,400),
                        new Point(4730,314),
                        new Point(3560,2314)
                },
                {
                        new Point(-770,944),
                        new Point(-3350,-130),
                        new Point(-1930,-820)
                }
            },
            // 4
            {
                {
                        new Point(393,100),
                        new Point(217,163),
                        new Point(65,352)
                },
                {
                        new Point(67,401),
                        new Point(104,452),
                        new Point(174,471)
                }
            },
            // 5
            {
                {
                        new Point(1444,2431),
                        new Point(479,2135),
                        new Point(75,1461)
                },
                {
                        new Point(75,1401),
                        new Point(503,744),
                        new Point(1428,464)
                }
            },
            // 6
            {
                {
                    new Point(827.0, 368.0),
                    new Point(903.0, 394.0),
                    new Point(940.0, 450.0)
                },
                {
                    
                    new Point(940.0, 480.0),
                    new Point(899.0, 532.0),
                    new Point(827.0, 555.0)
                }
            }
        };
    }

    @Test
    public void test1() {
        assertEquals(-0.411, MatchArcs.cnc(arcs[0][0], arcs[0][1]), DELTA);
    }
    
    @Test
    public void test2() {
        assertEquals(-0.007, MatchArcs.cnc(arcs[1][0], arcs[1][1]), DELTA);
    }
    
    @Test
    public void test3() {
        assertEquals(2.361, MatchArcs.cnc(arcs[2][0], arcs[2][1]), DELTA);
    }
    
    @Test
    public void test4() {
        assertEquals(0.863, MatchArcs.cnc(arcs[3][0], arcs[3][1]), DELTA);
    }
    
    @Test
    public void test5() {
        assertEquals(1.128, MatchArcs.cnc(arcs[4][0], arcs[4][1]), DELTA);
    }
    
    @Test
    public void test6() {
        assertEquals(1.128, MatchArcs.cnc(arcs[5][0], arcs[5][1]), DELTA);
    }
}
