package com.kaka.notice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 事件名对应{@link Mediator}列表
 */
class MediatorListMap {

    private final Map<Object, List<Mediator>> map;

    MediatorListMap() {
        map = new HashMap<>();
    }

    private List<Mediator> _get(Object key) {
        return map.get(key);
    }

    synchronized public List<Mediator> get(Object key) {
        return _get(key);
    }

    synchronized public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    synchronized public void forEach(Object key, Consumer<? super Mediator> action) {
        List<Mediator> list = _get(key);
        if (list != null && !list.isEmpty()) {
            list.forEach(action);
        }
    }

    synchronized public void put(Object key, Mediator value) {
        List<Mediator> list = map.computeIfAbsent(key, k -> new ArrayList<>());
        list.add(value);
    }

    synchronized public boolean remove(Object key, Mediator value) {
        List<Mediator> list = _get(key);
        if (list != null) {
            return list.remove(value);
        }
        return false;
    }

    synchronized public void clear() {
        map.forEach((Object key, List<Mediator> list) -> {
            if (list != null && !list.isEmpty()) {
                list.clear();
            }
        });
        map.clear();
    }

}
