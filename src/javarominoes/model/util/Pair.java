/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.util;

/**
 * Simple pair data structure
 *
 * @author dylan
 * @param <F> Type of first element, mutable
 * @param <S> Type of second element, mutable
 */
public class Pair<F, S> {

  public F f;
  public S s;

  public Pair() {
    f = null;
    s = null;
  }

  public Pair(F f, S s) {
    withFirst(f);
    withSecond(s);
  }

  
  public Pair(Object[] objList, int fIdx, int sIdx) {
    this();

    if (objList.length < 2) return;
    if (0 > fIdx || objList.length <= fIdx) return; 
    if (0 > sIdx || objList.length <= sIdx) return;
    if (fIdx == sIdx) return;

    // otherwise, the list of objects and supplied indices are valid.
    withFirst((F) objList[fIdx]);
    withSecond((S) objList[sIdx]);
  }

  public final Pair withFirst(F f) {
    /*
    if (f instanceof Number) {
      System.out.println("Setting fst " + (fst == null ? "nil" : fst) + "->" + f);
    }*/

    this.f = f;
    return this;
  }

  public final Pair withSecond(S s) {
    /*
    if (s instanceof Number) {
      System.out.println("Setting snd " + (snd == null ? "nil" : snd) + "->" + s);
    }*/

    this.s = s;
    return this;
  }

  public boolean hasNullItems() {
    return f == null || s == null;
  }

  @Override
  public final String toString() {
    
    return f.toString() + "," + s.toString();
  }

}
