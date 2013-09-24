package com.borqs.common.util;

public class AlphaPost implements Comparable {

    public String alpha;
    public int pos;
    public int newPos;

    public void setNewPosition(int pos) {
        newPos = pos;
    }

    public AlphaPost(String alpha, int pos) {
        this.alpha = alpha;
        this.pos = pos;
    }

    @Override
    public int compareTo(Object obj) {
        return alpha.compareTo((String) obj);
    }

    public String toString() {
        return " alpha=" + alpha;
    }
}
