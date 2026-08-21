package com.zcomini.backend.shared.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

public final class SafeUtils {

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String str) {
            return str.trim().isEmpty();
        }
        if (obj instanceof MultipartFile file) {
            return file.isEmpty();
        }
        return ObjectUtils.isEmpty(obj);
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static <T, R> R getOrDefault(T obj, Function<T, R> getter, R defaultValue) {
        if (obj == null)
            return defaultValue;
        R result = getter.apply(obj);
        return result != null ? result : defaultValue;
    }

    public static <T> String getOrEmpty(T obj, Function<T, String> getter) {
        return getOrDefault(obj, getter, "");
    }

    public static <T, K, E, V> Map<K, V> extractAndMap(
            Collection<T> sources,
            Function<T, K> keyExtractor,
            Function<Set<K>, Iterable<E>> fetcher,
            Function<E, K> entityKeyExtractor,
            Function<E, V> valueMapper) {

        if (sources == null || sources.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<K> keys = sources.stream()
                .map(keyExtractor)
                .filter(k -> k != null && (!(k instanceof String) || !((String) k).trim().isEmpty()))
                .collect(Collectors.toSet());

        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<K, V> resultMap = new HashMap<>();
        fetcher.apply(keys)
                .forEach(entity -> resultMap.put(entityKeyExtractor.apply(entity), valueMapper.apply(entity)));

        return resultMap;
    }

    public static <E, K, V> Map<K, V> toMap(
            Iterable<E> elements,
            Function<E, K> keyExtractor,
            Function<E, V> valueMapper) {
        if (elements == null) {
            return Collections.emptyMap();
        }
        Map<K, V> resultMap = new HashMap<>();
        elements.forEach(entity -> {
            K key = keyExtractor.apply(entity);
            if (key != null) {
                resultMap.put(key, valueMapper.apply(entity));
            }
        });
        return resultMap;
    }
}
