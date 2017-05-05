/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import org.opencv.core.Point;

/**
 *
 * @author tomson
 */
public class Line {
    private Point p1;
    private Point p2;

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }
    
    public Point get(int i) {
        if (i != 0 && i != 1) {
            throw new InvalidParameterException("i must equal 0 or 1");
        }
        if (i == 0) {
            return p1;
        }
        else {
            return p2;
        }
    }

    public Point getP1() {
        return p1;
    }

    public void setP1(Point p1) {
        this.p1 = p1;
    }

    public Point getP2() {
        return p2;
    }

    public void setP2(Point p2) {
        this.p2 = p2;
    }
    
    public double dX() {
        return p1.x - p2.x;
    }
    
    public double dY() {
        return p1.y - p2.y;
    }
    
    public double determinant() {
        return p1.x * p2.y - p1.y * p2.x;
    }
    
    /**
     * Computes the intersection between two lines.
     * (c) 2007 Alexander Hristov. Use Freely (LGPL
     * license). http://www.ahristov.com
     *
     * @param rhs The line to find the intersection with
     *
     * @return Point where the segments intersect, or null if they don't
     */
    public Point intersect(Line rhs) {
        double d = this.dX() * rhs.dY() - this.dY() * rhs.dX();
        
        if (d == 0) {
            return null;
        }

        double xi = (rhs.dX() * this.determinant() - this.dX() * rhs.determinant()) / d;
        double yi = (rhs.dY() * this.determinant() - this.dY() * rhs.determinant()) / d;

        return new Point(xi, yi);
    }
}
