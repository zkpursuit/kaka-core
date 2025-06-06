package com.kaka.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A resizable, ordered or unordered int array. Avoids the boxing that occurs
 * with ArrayList Integer. If unordered, this class avoids a memory copy when
 * removing elements (the last element is moved to the removed element's
 * position).
 *
 * @author Nathan Sweet
 */
public class IntArray implements Serializable {

    int[] items;
    int size;
    private final boolean ordered;

    /**
     * Creates an ordered array with a capacity of 16.
     */
    public IntArray() {
        this(true, 16);
    }

    /**
     * Creates an ordered array with the specified capacity.
     *
     * @param capacity specified capacity.
     */
    public IntArray(int capacity) {
        this(true, capacity);
    }

    /**
     * @param ordered  If false, methods that remove elements may change the
     *                 order of other elements in the array, which avoids a memory copy.
     * @param capacity Any elements added beyond this will cause the backing
     *                 array to be grown.
     */
    public IntArray(boolean ordered, int capacity) {
        this.ordered = ordered;
        items = new int[capacity];
    }

    /**
     * Creates a new array containing the elements in the specific array. The
     * new array will be ordered if the specific array is ordered. The capacity
     * is set to the number of elements, so any subsequent elements added will
     * cause the backing array to be grown.
     *
     * @param array specific array.
     */
    public IntArray(IntArray array) {
        this.ordered = array.ordered;
        size = array.size;
        items = new int[size];
        System.arraycopy(array.items, 0, items, 0, size);
    }

    /**
     * Creates a new ordered array containing the elements in the specified
     * array. The capacity is set to the number of elements, so any subsequent
     * elements added will cause the backing array to be grown.
     *
     * @param array specific array.
     */
    public IntArray(int[] array) {
        this(true, array, 0, array.length);
    }

    /**
     * Creates a new array containing the elements in the specified array. The
     * capacity is set to the number of elements, so any subsequent elements
     * added will cause the backing array to be grown.
     *
     * @param ordered    If false, methods that remove elements may change the
     *                   order of other elements in the array, which avoids a memory copy.
     * @param array      specified array.
     * @param startIndex start index.
     * @param count      count.
     */
    public IntArray(boolean ordered, int[] array, int startIndex, int count) {
        this(ordered, count);
        size = count;
        System.arraycopy(array, startIndex, items, 0, count);
    }

    /**
     * add value.
     *
     * @param value value to be added.
     */
    public void add(int value) {
        int[] items = this.items;
        if (size == items.length) {
            items = resize(Math.max(8, (int) (size * 1.75f)));
        }
        items[size++] = value;
    }

    public void add(int value1, int value2) {
        int[] items = this.items;
        if (size + 1 >= items.length) {
            items = resize(Math.max(8, (int) (size * 1.75f)));
        }
        items[size] = value1;
        items[size + 1] = value2;
        size += 2;
    }

    public void add(int value1, int value2, int value3) {
        int[] items = this.items;
        if (size + 2 >= items.length) {
            items = resize(Math.max(8, (int) (size * 1.75f)));
        }
        items[size] = value1;
        items[size + 1] = value2;
        items[size + 2] = value3;
        size += 3;
    }

    public void add(int value1, int value2, int value3, int value4) {
        int[] items = this.items;
        if (size + 3 >= items.length) {
            items = resize(Math.max(8, (int) (size * 1.8f))); // 1.75 isn't enough when size=5.
        }
        items[size] = value1;
        items[size + 1] = value2;
        items[size + 2] = value3;
        items[size + 3] = value4;
        size += 4;
    }

    public void addAll(IntArray array) {
        addAll(array, 0, array.size);
    }

    public void addAll(IntArray array, int offset, int length) {
        if (offset + length > array.size) {
            throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
        }
        addAll(array.items, offset, length);
    }

    public void addAll(int... array) {
        addAll(array, 0, array.length);
    }

    public void addAll(int[] array, int offset, int length) {
        int[] items = this.items;
        int sizeNeeded = size + length;
        if (sizeNeeded > items.length) {
            items = resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
        }
        System.arraycopy(array, offset, items, size, length);
        size += length;
    }

    public int get(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        }
        return items[index];
    }

    public void set(int index, int value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        }
        items[index] = value;
    }

    public void incr(int index, int value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        }
        items[index] += value;
    }

    public void mul(int index, int value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        }
        items[index] *= value;
    }

    public void insert(int index, int value) {
        if (index > size) {
            throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + size);
        }
        int[] items = this.items;
        if (size == items.length) {
            items = resize(Math.max(8, (int) (size * 1.75f)));
        }
        if (ordered) {
            System.arraycopy(items, index, items, index + 1, size - index);
        } else {
            items[size] = items[index];
        }
        size++;
        items[index] = value;
    }

    public void swap(int first, int second) {
        if (first >= size) {
            throw new IndexOutOfBoundsException("first can't be >= size: " + first + " >= " + size);
        }
        if (second >= size) {
            throw new IndexOutOfBoundsException("second can't be >= size: " + second + " >= " + size);
        }
        int[] items = this.items;
        int firstValue = items[first];
        items[first] = items[second];
        items[second] = firstValue;
    }

    public boolean contains(int value) {
        int i = size - 1;
        int[] items = this.items;
        while (i >= 0) {
            if (items[i--] == value) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(int value) {
        int[] items = this.items;
        for (int i = 0, n = size; i < n; i++) {
            if (items[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(int value) {
        int[] items = this.items;
        for (int i = size - 1; i >= 0; i--) {
            if (items[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * remove value.
     *
     * @param value value to be removed.
     * @return remove success.
     */
    public boolean removeValue(int value) {
        int[] items = this.items;
        for (int i = 0, n = size; i < n; i++) {
            if (items[i] == value) {
                removeIndex(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes and returns the item at the specified index.
     *
     * @param index specified index.
     */
    public int removeIndex(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        }
        int[] items = this.items;
        int value = items[index];
        size--;
        if (ordered) {
            System.arraycopy(items, index + 1, items, index, size - index);
        } else {
            items[index] = items[size];
        }
        return value;
    }

    /**
     * Removes the items between the specified indices, inclusive.
     *
     * @param start indices
     * @param end   inclusive
     */
    public void removeRange(int start, int end) {
        if (end >= size) {
            throw new IndexOutOfBoundsException("end can't be >= size: " + end + " >= " + size);
        }
        if (start > end) {
            throw new IndexOutOfBoundsException("start can't be > end: " + start + " > " + end);
        }
        int[] items = this.items;
        int count = end - start + 1;
        if (ordered) {
            System.arraycopy(items, start + count, items, start, size - (start + count));
        } else {
            int lastIndex = this.size - 1;
            for (int i = 0; i < count; i++) {
                items[start + i] = items[lastIndex - i];
            }
        }
        size -= count;
    }

    /**
     * Removes from this array all of elements contained in the specified array.
     *
     * @param array other IntArray
     * @return true if this array was modified.
     */
    public boolean removeAll(IntArray array) {
        int size = this.size;
        int startSize = size;
        int[] items = this.items;
        for (int i = 0, n = array.size; i < n; i++) {
            int item = array.get(i);
            for (int ii = 0; ii < size; ii++) {
                if (item == items[ii]) {
                    removeIndex(ii);
                    size--;
                    break;
                }
            }
        }
        return size != startSize;
    }

    /**
     * Removes and returns the last item.
     *
     * @return last item.
     */
    public int pop() {
        return items[--size];
    }

    /**
     * Returns the last item.
     *
     * @return last item.
     */
    public int peek() {
        return items[size - 1];
    }

    /**
     * Returns the first item.
     *
     * @return first item.
     */
    public int first() {
        if (size == 0) {
            throw new IllegalStateException("Array is empty.");
        }
        return items[0];
    }

    /**
     * clear.
     */
    public void clear() {
        size = 0;
    }

    /**
     * Reduces the size of the backing array to the size of the actual items.
     * This is useful to release memory when many items have been removed, or if
     * it is known that more items will not be added.
     *
     * @return {@link #items}
     */
    public int[] shrink() {
        if (items.length != size) {
            resize(size);
        }
        return items;
    }

    /**
     * Increases the size of the backing array to accommodate the specified
     * number of additional items. Useful before adding many items to avoid
     * multiple backing array resizes.
     *
     * @return {@link #items}
     */
    public int[] ensureCapacity(int additionalCapacity) {
        int sizeNeeded = size + additionalCapacity;
        if (sizeNeeded > items.length) {
            resize(Math.max(8, sizeNeeded));
        }
        return items;
    }

    /**
     * Sets the array size, leaving any values beyond the current size
     * undefined.
     *
     * @return {@link #items}
     */
    public int[] setSize(int newSize) {
        if (newSize > items.length) {
            resize(Math.max(8, newSize));
        }
        size = newSize;
        return items;
    }

    /**
     * resize
     *
     * @param newSize new size.
     * @return new array.
     */
    protected int[] resize(int newSize) {
        int[] newItems = new int[newSize];
        int[] items = this.items;
        System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
        this.items = newItems;
        return newItems;
    }

    /**
     * sort
     */
    public void sort() {
        Arrays.sort(items, 0, size);
    }

    /**
     * reverse
     */
    public void reverse() {
        int[] items = this.items;
        for (int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++) {
            int ii = lastIndex - i;
            int temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

    /**
     * shuffle
     */
    public void shuffle() {
        int[] items = this.items;
        for (int i = size - 1; i >= 0; i--) {
            int ii = MathUtils.random(i);
            int temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

    /**
     * Reduces the size of the array to the specified size. If the array is
     * already smaller than the specified size, no action is taken.
     *
     * @param newSize new size.
     */
    public void truncate(int newSize) {
        if (size > newSize) {
            size = newSize;
        }
    }

    /**
     * Returns a random item from the array, or zero if the array is empty.
     *
     * @return random item.
     */
    public int random() {
        if (size == 0) {
            return 0;
        }
        return items[MathUtils.random(0, size - 1)];
    }

    /**
     * get value array.
     *
     * @return value array.
     */
    public int[] toArray() {
        int[] array = new int[size];
        System.arraycopy(items, 0, array, 0, size);
        return array;
    }

    /**
     * get hashCode
     *
     * @return hashCode
     */
    public int hashCode() {
        if (!ordered) {
            return super.hashCode();
        }
        int[] items = this.items;
        int h = 1;
        for (int i = 0, n = size; i < n; i++) {
            h = h * 31 + items[i];
        }
        return h;
    }

    /**
     * compare equal.
     *
     * @param object other Object
     * @return true equal.
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!ordered) {
            return false;
        }
        if (!(object instanceof IntArray)) {
            return false;
        }
        IntArray array = (IntArray) object;
        if (!array.ordered) {
            return false;
        }
        int n = size;
        if (n != array.size) {
            return false;
        }
        int[] items1 = this.items;
        int[] items2 = array.items;
        for (int i = 0; i < n; i++) {
            if (items[i] != array.items[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * to string.
     *
     * @return string.
     */
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        int[] items = this.items;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('[');
        buffer.append(items[0]);
        for (int i = 1; i < size; i++) {
            buffer.append(", ");
            buffer.append(items[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * to string.
     *
     * @param separator separator
     * @return string.
     */
    public String toString(String separator) {
        if (size == 0) {
            return "";
        }
        int[] items = this.items;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append(items[0]);
        for (int i = 1; i < size; i++) {
            buffer.append(separator);
            buffer.append(items[i]);
        }
        return buffer.toString();
    }

    /**
     * @return new IntArray.
     * @see #IntArray(int[])
     */
    static public IntArray with(int... array) {
        return new IntArray(array);
    }

    /**
     * get size.
     *
     * @return size.
     */
    public int size() {
        return size;
    }
}
