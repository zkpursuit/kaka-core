package com.kaka.notice;

import java.util.*;

class CommandPoolSortedSet implements Set<CommandPool> {

    private final Set<CommandPool> set;
    private final static Comparator<CommandPool> comparator = (pool1, pool2) -> {
        if (pool1.equals(pool2)) return 0;
        if (pool2.priority <= pool1.priority) return 1;
        return -1;
    };

    CommandPoolSortedSet() {
        this.set = Collections.synchronizedSortedSet(new TreeSet<>(comparator));
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<CommandPool> iterator() {
        return set.iterator();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean add(CommandPool commandPool) {
        return set.add(commandPool);
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends CommandPool> c) {
        return set.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    @Override
    public void clear() {
        set.clear();
    }

}
