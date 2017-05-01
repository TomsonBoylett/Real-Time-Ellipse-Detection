/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.opencv.core.Mat;

/**
 *
 * @author tomson
 */
public class MatIterator implements Iterator<double[]> {
    private Mat mat = null;
    private int row = 0;
    private int column = 0;

    public MatIterator(Mat mat) {
        this.mat = mat;
    }

    @Override
    public boolean hasNext() {
        return !(row == mat.rows() - 1 && column == mat.cols());
    }

    @Override
    public double[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        if (column == mat.cols()) {
            column = 0;
            row++;
        }
        
        double[] element = mat.get(row, column);
        
        
        column++;
        
        return element;
    }
    
    public void put(double[] element) {
        mat.put(row, column, element);
    }
    
}
