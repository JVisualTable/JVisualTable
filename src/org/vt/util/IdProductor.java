package org.vt.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Id Productor, make sure id not repeat，and use this to alloc new primary keys;
 * 
 * @author Zerg Law
 * 
 */
public class IdProductor {

    private int lastIndex = -1;

    /**
     * Allocate ID
     * 
     * @param size
     * @param keys
     * @return
     */
    public List<Integer> getNewIds(int size, int[] keys) {
	List<Integer> ids = new ArrayList<Integer>();
	List<Integer[]> range = CacheUtil.getRangeArr(keys);
	for (int i = 0; i < range.size(); i++) {
	    Integer[] subRange = range.get(i);
	    if (i == 0) {
		if (subRange[0].intValue() > 0) {
		    for (int j = 0; j < subRange[0].intValue(); j++) {
			if (ids.size() < size) {
			    ids.add(j);
			} else {
			    lastIndex = ids.get(ids.size() - 1);
			    return ids;
			}
		    }
		}
	    } else {
		int lastIndex1 = range.get(i - 1)[1];
		int nowIndex1 = subRange[0].intValue();
		for (int j = lastIndex1 + 1; j < nowIndex1; j++) {
		    if (ids.size() < size) {
			ids.add(j);
		    } else {
			lastIndex = ids.get(ids.size() - 1);
			return ids;
		    }
		}
	    }
	}
	lastIndex = ids.get(ids.size() - 1);
	return ids;
    }

    public List<Integer> getIds(int size) {
	List<Integer> ids = new ArrayList<Integer>();
	if (lastIndex + 1 + size < 0) {
	    return ids;
	}
	for (int i = lastIndex + 1; i <= lastIndex + size; i++) {
	    ids.add(i);
	}
	lastIndex = ids.get(ids.size() - 1);
	return ids;
    }
}
