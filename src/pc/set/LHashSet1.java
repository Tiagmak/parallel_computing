package pc.set;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Non-concurrent hash set implementation with ReentrantLocks.
 */
public class LHashSet1<E> implements ISet<E> {

    private static final int NUMBER_OF_BUCKETS = 16; // should not be changed
    private final LinkedList<E>[] table;
    private final ReentrantLock rl = new ReentrantLock();
    private int size;

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    public LHashSet1() {
        table = (LinkedList<E>[]) new LinkedList[NUMBER_OF_BUCKETS];
        for (int i = 0; i < table.length; ++i) {
            table[i] = new LinkedList<>();
        }
        size = 0;
    }

    @Override
    public int size() {
        rl.lock();
        try {
            return size;
        } finally {
            rl.unlock();
        }
    }

    private LinkedList<E> getEntry(E elem) {
        return table[Math.abs(elem.hashCode() % table.length)];
    }

    @Override
    public boolean add(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        rl.lock();
        try {
            LinkedList<E> list = getEntry(elem);
            boolean r = !list.contains(elem);
            if (r) {
                list.addFirst(elem);
                ++size;
            }
            return r;
        } finally {
            rl.unlock();
        }
    }

    @Override
    public boolean remove(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        rl.lock();
        try {
            boolean r = getEntry(elem).remove(elem);
            if (r) {
                --size;
            }
            return r;
        } finally {
            rl.unlock();
        }
    }

    @Override
    public boolean contains(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        rl.lock();
        try {
            return getEntry(elem).contains(elem);
        } finally {
            rl.unlock();
        }
    }
}
