package com.biorecorder.edflib.filters;

import java.util.LinkedList;
import java.util.List;

/**
 * A FIFO queue, written by Alvin Alexander (http://alvinalexander.com).
 *
 * As its name implies, this is a first-in, first-out queue.
 *
 * I developed this class for a football game I'm writing, where I want to remember
 * a limited number of plays that the "Computer Offensive Coordinator" has
 * previously called. The current need is that I don't want the computer calling
 * the same play three times in a row.
 *
 * I was going to add a `reverse` method here, but you can do that in Java with
 * `Collections.reverse(list)`, so I didn't think there was a need for it.
 *
 */
public class FifoQueue<E> {

    private List<E> list = new LinkedList<>();
    private int size = 3;

    public FifoQueue(int size) {
        this.size = size;
    }

    public void put(E e) {
        list.add(e);
        if (list.size() > size) list.remove(0);
    }

    /**
     * can return `null`
     */
    public E pop() {
        if (list.size() > 0) {
            E e = list.get(0);
            list.remove(0);
            return e;
        } else {
            return null; //but have a nice day
        }
    }

    /**
     * @throws IndexOutOfBoundsException
     */
    public E peek() {
        return list.get(0);
    }

    public boolean contains(E e) {
        return list.contains(e);
    }

    /**
     * @throws IndexOutOfBoundsException
     */
    public E get(int i) {
        return list.get(i);
    }

    public List<E> getBackingList() {
        // return a copy of the list
        return new LinkedList<>(list);
    }

    public void clear() {
        list.clear();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    // mostly needed for testing atm
    public int size() {
        return list.size();
    }

}