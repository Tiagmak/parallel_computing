package pc.set;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TArray;
import scala.concurrent.stm.japi.STM;

/**
 * Concurrent hash set implementation that uses STM.
 */
public class STMHashSet<E> implements ISet<E> {

    private static final int NUMBER_OF_BUCKETS = 16; // should not be changed 
    private final Ref.View<Integer> size;
    private final TArray.View<Node<E>> table;

    private static class Node<T> {
        T value;
        Ref.View<Node<T>> next = STM.newRef(null);
        Ref.View<Node<T>> prev = STM.newRef(null);
    }

    private int getIdx(E elem) {
        return Math.abs(elem.hashCode() % table.length());
    }

    /**
     * Constructor.
     */
    public STMHashSet() {
        size = STM.newRef(0);
        table = STM.newTArray(NUMBER_OF_BUCKETS);
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean add(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        int idx = getIdx(elem);

        return STM.atomic(() -> {
            if (!contains(idx, elem)) {
                Node<E> previousHead = table.apply(idx);
                Node<E> newNode = new Node<>();
                newNode.value = elem;
                newNode.prev.set(null);
                newNode.next.set(previousHead);
                if (previousHead != null) {
                    previousHead.prev.set(newNode);
                }
                table.update(idx, newNode);

                STM.increment(size, 1);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean remove(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        int idx = getIdx(elem);

        return STM.atomic(() -> {
            // Look for it in the list
            Node<E> ptr = table.apply(idx);
            while (ptr != null) {
                // Remove item
                if (ptr.value.equals(elem)) {
                    // If in the middle of the list
                    Node<E> prev = ptr.prev.get();
                    Node<E> after = ptr.next.get();
                    if (prev != null) {
                        //SoTA: prev    prev<-ptr->next    after
                        prev.next.set(after);
                    } else { // Head of the list
                        table.update(idx, ptr.next.get());
                    }
                    if (after != null)
                        after.prev.set(prev);

                    STM.increment(size, -1);
                    return true;
                }
                ptr = ptr.next.get();
            }
            return false;
        });
    }

    @Override
    public boolean contains(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        int idx = getIdx(elem);
        return STM.atomic(() -> contains(idx, elem));
    }

    private boolean contains(int idx, E elem) {
        return STM.atomic(() -> {
            Node<E> ptr = table.apply(idx);
            while (ptr != null) {
                if (ptr.value.equals(elem)) {
                    return true;
                }
                ptr = ptr.next.get();
            }
            return false;
        });
    }

    /*public static void main (String[] args) {
        STMHashSet<Integer> test = new STMHashSet<>();
        System.out.println("contains0:"+test.contains(42));
        System.out.println("add:"+test.add(42));
        System.out.println("contains1:"+test.contains(42));
        System.out.println("remove:"+test.remove(42));
        System.out.println("contains2:"+test.contains(42));
    }*/
}
