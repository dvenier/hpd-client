/**
 * Copyright (c) 2006-2013 Mirth Corporation.
 * All rights reserved.
 *
 * NOTICE:  All information contained herein is, and remains, the
 * property of Mirth Corporation. The intellectual and technical
 * concepts contained herein are proprietary and confidential to
 * Mirth Corporation and may be covered by U.S. and Foreign
 * Patents, patents in process, and are protected by trade secret
 * and/or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from Mirth Corporation.
 */
package com.mirth.mail.hpd.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements an <code>Iterator</code> that wraps a <code>List</code> of iterators that will
 * obtain items via a round robin mechanism.
 * 
 * @param <E>
 * @see Iterator
 */
public class RoundRobinChain<E> implements Iterator<E> {

    private List<Iterator<? extends E>> iterators = new ArrayList<Iterator<? extends E>>();
    private int currentIteratorIndex = 0;
    private Iterator<? extends E> currentIterator;
    private boolean stale;

    private final ReentrantLock lock = new ReentrantLock();
    
    public RoundRobinChain() {
        super();
    }
    
    public RoundRobinChain(List<Iterator<? extends E>> iterators) {
        setIterators(iterators);
    }
    
    public List<E> toList() {
        List<E> list = new ArrayList<E>();
        while(hasNext()) {
            list.add(next());
        }
        return list;
    }
    
    public void addIterator(Iterator<? extends E> iterator) {
        lock.lock();
        try {
            getIterators().add(iterator);
        } finally {
            lock.unlock();
        }
    }
    
    protected List<Iterator<? extends E>> getIterators() {
        return iterators;
    }

    public void setIterators(List<Iterator<? extends E>> iterators) {
        this.iterators = iterators;
    }

    protected boolean doAnyHaveNext() {
        boolean anyFlag = false;
        
        List<Iterator<? extends E>> iterators = getIterators();
        if (iterators != null && (! iterators.isEmpty())) {
            for (Iterator<? extends E> iterator : iterators) {
                if (iterator != null && iterator.hasNext()) {
                    anyFlag = true;
                    break;
                }
            }
        }
        
        return anyFlag;
    }

    protected Iterator<? extends E> getNextIterator() {
        Iterator<? extends E> iterator = null;
        
        int currentIndex = getCurrentIteratorIndex();
        iterator = getNextIterator(currentIndex);
        
        setCurrentIterator(iterator);
        
        return iterator;
    }
    
    private Iterator<? extends E> getNextIterator(int index) {
        Iterator<? extends E> iterator = null;
        
        if (doAnyHaveNext()) {
            List<Iterator<? extends E>> iterators = getIterators();
            if (index > iterators.size()) {
                iterator = getNextIterator(0);
            } else {
                if (iterators.size() == index) {
                    index = 0;
                }
                Iterator<? extends E> itr = iterators.get(index);
                if (itr.hasNext()) {
                    iterator = itr;
                } else {
                    iterator = getNextIterator(index + 1);
                }
            }
        }
        
        if (iterator != null) {
            setCurrentIteratorIndex(index + 1);
        }
        
        return iterator;
    }
    
    protected Iterator<? extends E> getIterator() {
        Iterator<? extends E> iterator = null;
        
        if (isStale()) {
            iterator = getNextIterator();
            setStale(false);
        } else {
            iterator = getCurrentIterator();
        }
        
        return iterator;
    }    

    @Override
    public boolean hasNext() {
        boolean hasNext = false;
        
        lock.lock();
        try {
            Iterator<? extends E> iterator = getIterator();
            hasNext = (iterator == null) ? false : iterator.hasNext();
        } finally {
            lock.unlock();
        }
        
        return hasNext;
    }

    @Override
    public E next() {
        E next = null;
        
        lock.lock();
        try {
            Iterator<? extends E> iterator = getIterator();
            setStale(true);
            next = (iterator == null) ? null : iterator.next();
        } finally {
            lock.unlock();
        }

        return next;
    }


    @Override
    public void remove() {
        lock.lock();
        try {
            Iterator<? extends E> iterator = getIterator();
            if (iterator != null && iterator.hasNext()) {
                iterator.next();
                iterator.remove();
                setStale(true);
            }
        } finally {
            lock.unlock();
        }
    }


    protected int getCurrentIteratorIndex() {
        return currentIteratorIndex;
    }


    protected void setCurrentIteratorIndex(int currentIteratorIndex) {
        this.currentIteratorIndex = currentIteratorIndex;
    }


    protected Iterator<? extends E> getCurrentIterator() {
        if (this.currentIterator == null) {
            this.currentIterator = getNextIterator();
        }
        return currentIterator;
    }


    protected void setCurrentIterator(Iterator<? extends E> currentIterator) {
        this.currentIterator = currentIterator;
    }


    protected boolean isStale() {
        return stale;
    }


    protected void setStale(boolean stale) {
        this.stale = stale;
    }

}