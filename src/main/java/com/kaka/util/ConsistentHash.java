package com.kaka.util;

import java.util.*;
import java.util.concurrent.locks.StampedLock;

/**
 * 一致性hash
 *
 * @author zkpursuit
 */
public class ConsistentHash<T> {
    private final int numberOfReplicas;
    private final SortedMap<Long, T> circle = new TreeMap<>();
    private final List<Long> circleKeys = new ArrayList<>();
    private final StampedLock lock = new StampedLock();

    /**
     * 构造方法
     *
     * @param numberOfReplicas 每个节点的虚拟节点数量
     * @param nodes            初始节点集合
     */
    public ConsistentHash(int numberOfReplicas, Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;
        nodes.forEach(this::_addNode);
    }

    /**
     * 构造方法
     *
     * @param numberOfReplicas 每个节点的虚拟节点数量
     */
    public ConsistentHash(int numberOfReplicas) {
        this(numberOfReplicas, Collections.emptyList());
    }

    /**
     * 计算hash值
     */
    private long calcHash(String node) {
        //return StringUtils.toNumber(node);
        return StringUtils.getCRC16Hash(node);
    }

    private void _addNode(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            String _node = node.toString() + "#" + i;
            Long hash = calcHash(_node);
            circle.put(hash, node);
            circleKeys.add(hash);
        }
    }

    /**
     * 添加节点
     *
     * @param node 数据服务节点
     */
    public void addNode(T node) {
        long stamp = lock.writeLock();
        try {
            _addNode(node);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 移除节点
     *
     * @param node 数据服务节点
     */
    public void removeNode(T node) {
        long stamp = lock.writeLock();
        try {
            for (int i = 0; i < numberOfReplicas; i++) {
                String _node = node.toString() + "#" + i;
                Long hash = calcHash(_node);
                circle.remove(hash);
                circleKeys.remove(hash);
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 计算key的hash获得key被分配的节点
     *
     * @param key 数据键
     * @return key被分配的节点
     */
    private T _getNode(String key) {
        if (circle.isEmpty()) {
            return null;
        }
        long hash = calcHash(key);
        if (!circle.containsKey(hash)) {
            SortedMap<Long, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }

    /**
     * 计算key的hash获得key被分配的节点
     *
     * @param key 数据键
     * @return key被分配的节点
     */
    public T getNode(String key) {
        long stamp = lock.tryOptimisticRead();
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                return _getNode(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return _getNode(key);
    }

    /**
     * 随机获取某个节点
     */
    public T getNode() {
        long stamp = lock.tryOptimisticRead();
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                return circle.isEmpty() ? null : circle.get(circleKeys.get(MathUtils.random(0, circleKeys.size() - 1)));
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return circle.isEmpty() ? null : circle.get(circleKeys.get(MathUtils.random(0, circleKeys.size() - 1)));
    }

    /**
     * 获取节点数量
     *
     * @return 节点数量
     */
    public long getSize() {
        long stamp = lock.tryOptimisticRead();
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                return circle.size();
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return circle.size();
    }

    /**
     * 清除所有节点
     */
    public void clear() {
        long stamp = lock.writeLock();
        try {
            this.circle.clear();
            this.circleKeys.clear();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

}
