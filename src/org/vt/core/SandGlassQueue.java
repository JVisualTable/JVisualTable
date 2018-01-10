package org.vt.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * batch blocked queue ,it can get few data.
 * 
 * @author Zerg Law
 * 
 * @param <E>
 */
public class SandGlassQueue<E> {

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition notFullCondition = lock.newCondition();

    private final Condition notEmptyCondition = lock.newCondition();

    private final Condition delayCondition = lock.newCondition();
    private int capacity = Integer.MAX_VALUE;
    private int maxNum;
    private long intervalTime;
    private long minInteralTime = 1000;
    private List<E> store = new LinkedList<E>();

    private boolean isFirstTake = true;
    private boolean destroyed = false;

    public SandGlassQueue(int maxNum, long intervalTime, long minInteralTime) {
	this.maxNum = maxNum;
	this.intervalTime = intervalTime;
	this.minInteralTime = minInteralTime;
    }

    public SandGlassQueue(int maxNum, long intervalTime) {
	this(maxNum, intervalTime, 1000);
    }

    public void put(List<E> list) throws InterruptedException {
	if (list == null || list.isEmpty()) {
	    return;
	}
	lock.lock();
	try {
	    while (!destroyed && (store.size() + list.size() > capacity)) {
		notFullCondition.await();
	    }
	    if (destroyed) {
		store.clear();
		System.err.println("SandGlassQueue is destroyed!");
		// throw new
		// InterruptedException("SandGlassQueue is destroyed!");
	    }
	    store.addAll(list);
	    notEmptyCondition.signal();
	} finally {
	    lock.unlock();
	}
    }

    private List<E> remove() {
	int fromIndex = 0;
	int toIndex = 0;
	if (store.isEmpty()) {
	    return null;
	}
	List<E> list = new ArrayList<E>();
	if (store.size() < getMaxNum()) {
	    fromIndex = 0;
	    toIndex = store.size() - 1;
	} else {
	    fromIndex = 0;
	    toIndex = getMaxNum() - 1;
	}
	for (int i = 0; i <= toIndex - fromIndex; i++) {
	    list.add(store.remove(0));
	}
	return list;
    }

    public List<E> take() throws InterruptedException {
	lock.lock();
	List<E> x;
	try {
	    while (!destroyed && store.isEmpty()) {
		notEmptyCondition.await();
	    }
	    if (destroyed) {
		store.clear();
		throw new InterruptedException("SandGlassQueue is destroyed!");
	    }
	    x = remove();
	    if (x != null) {
		if (!isFirstTake) {
		    if (store.size() - getMaxNum() > 0 && getIntervalTime() > 0) {
			delayCondition.await(getIntervalTime(),
				TimeUnit.MILLISECONDS);
		    } else {
			delayCondition.await(getMinInteralTime(),
				TimeUnit.MILLISECONDS);
		    }
		} else {
		    isFirstTake = false;
		}
	    }
	    notFullCondition.signal();
	    return x;
	} finally {
	    lock.unlock();
	}
    }

    public void setFirstTake(boolean isFirstTake1) {
	this.isFirstTake = isFirstTake1;
    }

    public long getMinInteralTime() {
	return minInteralTime;
    }

    public void setMinInteralTime(long minInteralTime) {
	this.minInteralTime = minInteralTime;
    }

    public int size() {
	lock.lock();
	try {
	    return store.size();
	} finally {
	    lock.unlock();
	}
    }

    public int getMaxNum() {
	return maxNum;
    }

    public long getIntervalTime() {
	return intervalTime;
    }

    public void destroy() {
	lock.lock();
	try {
	    synchronized (notFullCondition) {
		notFullCondition.signalAll();
	    }
	    synchronized (notEmptyCondition) {
		notEmptyCondition.signalAll();
	    }
	    synchronized (delayCondition) {
		delayCondition.signalAll();
	    }
	    destroyed = true;
	} finally {
	    lock.unlock();
	}
    }
}
