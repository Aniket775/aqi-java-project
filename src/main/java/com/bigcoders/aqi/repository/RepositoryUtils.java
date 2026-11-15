package com.bigcoders.aqi.repository;

import org.springframework.jdbc.support.KeyHolder;
import java.util.Map;

public final class RepositoryUtils {

    private RepositoryUtils() {}

    public static Integer extractGeneratedKey(KeyHolder keyHolder, String idColumn) {
        if (keyHolder == null) {
            return null;
        }
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            for (Map.Entry<String, Object> entry : keyHolder.getKeys().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(idColumn) && entry.getValue() instanceof Number num) {
                    return num.intValue();
                }
            }
            for (Object val : keyHolder.getKeys().values()) {
                if (val instanceof Number num) {
                    return num.intValue();
                }
            }
        }
        try {
            Number num = keyHolder.getKey();
            return num != null ? num.intValue() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}

