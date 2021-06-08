package pc.set;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Concurrent hash set implementation that locks the hash table entries.
 */
public class LHashSet3<E> implements ISet<E> {

    private static final int NUMBER_OF_BUCKETS = 16; // should not be changed
    private final LinkedList<E>[] table;
    private final ReentrantReadWriteLock[] rrwl;

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    public LHashSet3() {
        table = (LinkedList<E>[]) new LinkedList[NUMBER_OF_BUCKETS];
        for (int i = 0; i < table.length; ++i) {
            table[i] = new LinkedList<>();
        }
        rrwl = new ReentrantReadWriteLock[NUMBER_OF_BUCKETS];
        for (int i = 0; i < rrwl.length; ++i) {
            rrwl[i] = new ReentrantReadWriteLock();
        }
    }

    @Override
    public int size() {
        int size = 0;
        // Lock it all
        for (ReentrantReadWriteLock reentrantReadWriteLock : rrwl) {
            reentrantReadWriteLock.readLock().lock();
        }
        try {
            // Sum it all
            for (LinkedList<E> es : table) {
                size += es.size();
            }
            return size;
        } finally {
            // Free it all
            for (ReentrantReadWriteLock reentrantReadWriteLock : rrwl) {
                reentrantReadWriteLock.readLock().unlock();
            }
        }
    }

    private int getIdx(E elem) {
        return Math.abs(elem.hashCode() % table.length);
    }

    private ReentrantReadWriteLock getLock(E elem) {
        return rrwl[getIdx(elem)];
    }

    private ReentrantReadWriteLock getLock(int idx) {
        return rrwl[idx];
    }

    private LinkedList<E> getEntry(E elem) {
        return table[getIdx(elem)];
    }

    private LinkedList<E> getEntry(int idx) {
        return table[idx];
    }

    @Override
    public boolean add(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        int idx = getIdx(elem);
        getLock(idx).writeLock().lock();
        try {
            LinkedList<E> list = getEntry(idx);
            boolean r = !list.contains(elem);
            if (r) {
                list.addFirst(elem);
            }
            return r;
        } finally {
            getLock(idx).writeLock().unlock();
        }
    }

    @Override
    public boolean remove(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        int idx = getIdx(elem);
        getLock(idx).writeLock().lock();
        try {
            return getEntry(idx).remove(elem);
        } finally {
            getLock(idx).writeLock().unlock();
        }
    }

    @Override
    public boolean contains(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        int idx = getIdx(elem);
        getLock(idx).readLock().lock();
        try {
            return getEntry(idx).contains(elem);
        } finally {
            getLock(idx).readLock().unlock();
        }
    }
}
