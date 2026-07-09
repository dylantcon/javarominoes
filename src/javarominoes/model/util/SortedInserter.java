/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes.model.util;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author dylan
 */
public class SortedInserter {

  public static <T extends Comparable<? super T>> void insertInOrder(List<T> list, T item) {
    // find the index or the encoded insertion point
    int index = Collections.binarySearch(list, item);

    // if not found, convert negative return value to the positive index
    if (index < 0) {
      index = -(index + 1);
    }

    // insert the item at the correct sorted position
    list.add(index, item);
  }
}
