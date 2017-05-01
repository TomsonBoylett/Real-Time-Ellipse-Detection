/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter;

import math.geom2d.Point2D;
import math.geom2d.conic.Ellipse2D;
import math.geom2d.point.PointArray2D;
import math.geom2d.point.PointSet2D;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

/**
 *
 * @author tomson
 */
public class GeomCV {
    public static Ellipse2D convert(RotatedRect ellipse) {
        return new Ellipse2D(ellipse.center.x, ellipse.center.y, ellipse.size.width, ellipse.size.height, ellipse.angle);
    }
    
    public static Point2D convert(Point p) {
        return new Point2D(p.x, p.y);
    }
    
    public static PointSet2D convert(MatOfPoint mop) {
        Point[] cv = mop.toArray();
        PointArray2D geom = new PointArray2D(cv.length);
        for (Point p : cv) {
            geom.add(convert(p));
        }
        return geom; 
    }
}
