package com.boylett.t.coincounter;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.MatOfPoint;

/**
 * Stores 4 sets of contours corresponding to the 4 quadrants of an ellipse.<br>
 * <br>
 * Related:<br>
 * http://i.imgur.com/RZJHVQ2.png
 *
 * @author tomson
 */
public class QuadrantSet {
    private List[] arcs;
    
    public QuadrantSet() {
        arcs = new List[4];
        for(int i = 0; i < 4; i++) {
            arcs[i] = new ArrayList<MatOfPoint>();
        }
    }

    public QuadrantSet(List<MatOfPoint> arcI, List<MatOfPoint> arcII,
            List<MatOfPoint> arcIII, List<MatOfPoint> arcIV) {
        arcs = new List[4];
        arcs[0] = arcI;
        arcs[1] = arcII;
        arcs[2] = arcIII;
        arcs[3] = arcIV;
    }
    
    public List<MatOfPoint> get(int i) {
        return arcs[i];
    }

    public List<MatOfPoint> getArcI() {
        return arcs[0];
    }

    public List<MatOfPoint> getArcII() {
        return arcs[1];
    }

    public List<MatOfPoint> getArcIII() {
        return arcs[2];
    }

    public List<MatOfPoint> getArcIV() {
        return arcs[3];
    }

    public void setArcI(List<MatOfPoint> arcI) {
        this.arcs[0] = arcI;
    }

    public void setArcII(List<MatOfPoint> arcII) {
        this.arcs[1] = arcII;
    }

    public void setArcIII(List<MatOfPoint> arcIII) {
        this.arcs[2] = arcIII;
    }

    public void setArcIV(List<MatOfPoint> arcIV) {
        this.arcs[3] = arcIV;
    }
}
