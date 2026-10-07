package kr.yuns.dropthepitchserver.admin.data.repository.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public final class AdminRows {
    private AdminRows() {
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> list(EntityManager entityManager, String sql, AdminConditions conditions,
                                   Function<Tuple, T> mapper) {
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        conditions.bind(query);
        return ((List<Tuple>) query.getResultList()).stream().map(mapper).toList();
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> page(EntityManager entityManager, String sql, AdminConditions conditions,
                                   int page, int size, Function<Tuple, T> mapper) {
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        conditions.bind(query);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return ((List<Tuple>) query.getResultList()).stream().map(mapper).toList();
    }

    public static long number(EntityManager entityManager, String sql, AdminConditions conditions) {
        Query query = entityManager.createNativeQuery(sql);
        conditions.bind(query);
        Object value = query.getSingleResult();
        return value instanceof Number number ? number.longValue() : 0L;
    }

    public static Long asLong(Tuple row, String alias) {
        return row.get(alias) instanceof Number number ? number.longValue() : null;
    }

    public static long asLongOrZero(Tuple row, String alias) {
        return row.get(alias) instanceof Number number ? number.longValue() : 0L;
    }

    public static int asInt(Tuple row, String alias) {
        return row.get(alias) instanceof Number number ? number.intValue() : 0;
    }

    public static Double asDouble(Tuple row, String alias) {
        return row.get(alias) instanceof Number number ? number.doubleValue() : null;
    }

    public static String asString(Tuple row, String alias) {
        Object value = row.get(alias);
        return value == null ? null : value.toString();
    }

    public static LocalDateTime asDateTime(Tuple row, String alias) {
        Object value = row.get(alias);
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    public static LocalDate asDate(Tuple row, String alias) {
        Object value = row.get(alias);
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        return null;
    }

    public static <E extends Enum<E>> E asEnum(Tuple row, String alias, Class<E> type) {
        String value = asString(row, alias);
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Double asScore(Tuple row, String alias) {
        Double value = asDouble(row, alias);
        return value == null ? null : Math.round(value * 10) / 10.0;
    }
}
