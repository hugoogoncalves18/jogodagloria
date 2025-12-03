package com.jogogloria.utils;

/**
 * Array-backed implementation of an unordered list. Elements may be inserted at
 * the front, at the rear, or immediately after a specified target element.
 * <p>
 * Unlike an ordered list, this structure does not arrange elements according to
 * any natural or custom ordering. Insertions preserve only the relative
 * placement dictated by the operation used (front, rear, or after a target).
 * </p>
 *
 * <h2>Main characteristics:</h2>
 * <ul>
 *   <li>Uses a manually managed growable array (no Java Collections)</li>
 *   <li>Allows insertion at different positions with efficient shifting</li>
 *   <li>Accepts duplicate and {@code null} elements (unless explicitly restricted)</li>
 *   <li>Throws {@link NoElementFoundException} when a target for insertion is not found</li>
 * </ul>
 *
 * @param <T> the element type stored in the list; must implement {@link Comparable}
 *            for compatibility with other ADTs in the system
 */
public class ArrayUnorderedList<T extends Comparable<T>>
        extends ArrayList<T> implements UnorderedListADT<T> {

    /**
     * Inserts an element at the front (index 0) of the list. Existing elements
     * are shifted one position to the right to create available space.
     * <p>
     * If the internal array is full, its capacity is doubled before insertion.
     * </p>
     *
     * @param element the element to insert (may be {@code null})
     */
    @Override
    public void addToFront(T element) {
        if (rear == list.length)
            expandCapacity();

        // Shift elements to the right
        for (int i = rear; i > 0; i--) {
            list[i] = list[i - 1];
        }

        list[0] = element;
        rear++;
    }

    /**
     * Inserts an element at the rear of the list. No shifting is required, and
     * the element is simply placed at index {@code rear}.
     *
     * @param element the element to insert (may be {@code null})
     */
    @Override
    public void addToRear(T element) {
        if (rear == list.length)
            expandCapacity();

        list[rear++] = element;
    }

    /**
     * Inserts an element immediately after the first occurrence of the target
     * element. Elements after the insertion point are shifted one position to
     * the right.
     * <p>
     * If the target is not present in the list, a
     * {@link NoElementFoundException} is thrown.
     * </p>
     *
     * @param element the element to insert
     * @param target the existing element after which the new element is added
     *
     * @throws IllegalArgumentException if {@code target} is {@code null}
     * @throws NoElementFoundException if {@code target} is not found in the list
     */
    @Override
    public void addAfter(T element, T target) throws NoElementFoundException {
        if (rear == list.length)
            expandCapacity();

        if (target == null)
            throw new IllegalArgumentException("target cannot be null");

        // Search for target index
        int index = -1;
        for (int i = 0; i < rear; i++) {
            if (target.equals(list[i])) {
                index = i;
                break;
            }
        }

        if (index == -1)
            throw new NoElementFoundException("Target element not found");

        // Shift elements to the right
        for (int i = rear; i > index + 1; i--) {
            list[i] = list[i - 1];
        }

        list[index + 1] = element;
        rear++;
    }

    /**
     * Doubles the capacity of the underlying array by allocating a new array
     * and copying existing elements into it.
     */
    @SuppressWarnings("unchecked")
    private void expandCapacity() {
        T[] newList = (T[]) new Comparable[list.length * 2];
        for (int i = 0; i < rear; i++) {
            newList[i] = list[i];
        }
        list = newList;
    }

    /**
     * Retorna o elemento na posição especificada.
     * @param index A posição (0-based) do elemento a retornar.
     * @return O elemento na posição.
     * @throws IndexOutOfBoundsException Se o índice for inválido.
     */
    public T get(int index) {
        if (index < 0 || index >= this.rear) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        return this.list[index];
    }
}
