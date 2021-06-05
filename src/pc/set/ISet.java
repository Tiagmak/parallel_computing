package pc.set;

/**
 * Interface for a set.
 *
 * @param <E> Type of elements in the set.
 */
public interface ISet<E> {

    /**
     * Get number of elements in the set.
     */
    int size();

    /**
     * Add an element to the set.
     *
     * @param elem The element to add.
     * @return <code>true</code> if the element was added to the set,
     * <code>false</code> if the set already contained the element.
     * @throws IllegalArgumentException if <code>elem == null</code>.
     */
    boolean add(E elem);

    /**
     * Remove an element from the set.
     *
     * @param elem The element to remove.
     * @return <code>true</code> if the element was removed from the set,
     * <code>false</code> if the set does not contain the element.
     * @throws IllegalArgumentException if <code>elem == null</code>.
     */
    boolean remove(E elem);

    /**
     * Check if an element belongs to the set.
     *
     * @param elem The element to search.
     * @return <code>true</code> if the element is in the set,
     * <code>false</code> otherwise.
     * @throws IllegalArgumentException if <code>elem == null</code>.
     */
    boolean contains(E elem);

}
