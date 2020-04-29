package org.apache.iotdb.db.index.distance;

import static org.apache.iotdb.db.index.common.IndexUtils.getDoubleFromAnyType;

import org.apache.iotdb.db.utils.datastructure.TVList;

/**
 * Euclidean distance, not including L∞-norm.
 */
public class LNormDouble implements Distance {

  public final double getP() {
    return p;
  }

  private int p;

  public LNormDouble(int p) {
    this.p = p;
  }

  @Override
  public double distWithoutSqrt(double a, double b) {
    return pow(Math.abs(a - b));
  }

  public double pow(double r) {
    double s = r;
    for (int i = 1; i < p; i++) {
      s *= r;
    }
    return s;
  }

  public static double pow(double r, int pp) {
    double s = r;
    for (int i = 1; i < pp; i++) {
      s *= r;
    }
    return s;
  }

  public double getThresholdNoRoot(double threshold) {
    return pow(threshold);
  }


  public double dist(double[] a, double[] b) {
    assert a.length == b.length;
    double dis = 0;
    for (int i = 0; i < a.length; i++) {
      dis += pow(Math.abs(a[i] - b[i]));
    }
    return Math.pow(dis, 1.0 / p);
  }


  public double dist(double[] a, int aOffset, TVList b, int bOffset, int length) {
    double dis = distPower(a, aOffset, b, bOffset, length);
    return dis == 0 ? 0 : Math.pow(dis, 1.0 / p);
  }

  public double distPower(double[] a, int aOffset, TVList b, int bOffset, int length) {
    assert a.length >= aOffset + length && a.length >= bOffset + length;
    double dis = 0;
    for (int i = 0; i < length; i++) {
      dis += pow(Math.abs(a[i + aOffset] - getDoubleFromAnyType(b, i + bOffset)));
    }
    return dis;
  }

  @Override
  public int distEarlyAbandon(double[] a, int aOffset, double[] b, int bOffset, int length,
      double thres) {
    assert a.length >= aOffset + length && b.length >= bOffset + length;
    double thresPow = pow(thres);
    double dis = 0;
    for (int i = 0; i < length; i++) {
      dis += pow(Math.abs(a[i + aOffset] - b[i + bOffset]));
      if (dis > thresPow) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public int distEarlyAbandonDetail(double[] a, int aOffset, double[] b, int bOffset, int length,
      double threshold) {
    assert a.length >= aOffset + length && b.length >= bOffset + length;
    double thresPow = pow(threshold);
    double dis = 0;
    for (int i = 0; i < length; i++) {
      dis += pow(Math.abs(a[i + aOffset] - b[i + bOffset]));
      if (dis > thresPow) {
        return (i + 1);
      }
    }
    return -length;
  }

  @Override
  public int distEarlyAbandonDetailNoRoot(double[] a, int aOffset, TVList b, int bOffset,
      int length, double
      thresPow) {
    if (a.length < aOffset + length || b.size() < bOffset + length) {
      throw new DistanceMetricException("The length is out of bound");
    }
    double dis = 0;
    for (int i = 0; i < length; i++) {
      dis += pow(Math.abs(a[i + aOffset] - getDoubleFromAnyType(b, i + bOffset)));
      if (dis > thresPow) {
        return (i + 1);
      }
    }
    return -length;
  }

  @Override
  public String toString() {
    return "LNorm_" + p;
  }
}
