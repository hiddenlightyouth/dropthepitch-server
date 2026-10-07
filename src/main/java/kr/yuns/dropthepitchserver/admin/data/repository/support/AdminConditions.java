package kr.yuns.dropthepitchserver.admin.data.repository.support;

import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminConditions {
    private final List<String> clauses = new ArrayList<>();
    private final Map<String, Object> params = new LinkedHashMap<>();

    public AdminConditions add(String clause) {
        clauses.add(clause);
        return this;
    }

    public AdminConditions add(Object value, String clause, String name) {
        if (value == null) {
            return this;
        }
        clauses.add(clause);
        params.put(name, value instanceof Enum<?> e ? e.name() : value);
        return this;
    }

    public AdminConditions param(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public AdminConditions in(Collection<?> values, String column, String name) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        clauses.add(column + " in (:" + name + ")");
        params.put(name, values.stream()
                .map(value -> value instanceof Enum<?> e ? e.name() : value)
                .toList());
        return this;
    }

    public AdminConditions keyword(String keyword, String... columns) {
        if (keyword == null || keyword.isBlank()) {
            return this;
        }
        List<String> likes = new ArrayList<>();
        for (String column : columns) {
            likes.add("lower(" + column + ") like :keyword");
        }
        clauses.add("(" + String.join(" or ", likes) + ")");
        params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        return this;
    }

    public AdminConditions period(LocalDate from, LocalDate to, String column) {
        if (from != null) {
            clauses.add(column + " >= :periodFrom");
            params.put("periodFrom", from.atStartOfDay());
        }
        if (to != null) {
            clauses.add(column + " < :periodTo");
            params.put("periodTo", to.plusDays(1).atStartOfDay());
        }
        return this;
    }

    public AdminConditions range(Number min, Number max, String expression, String name) {
        if (min != null) {
            clauses.add(expression + " >= :" + name + "Min");
            params.put(name + "Min", min);
        }
        if (max != null) {
            clauses.add(expression + " <= :" + name + "Max");
            params.put(name + "Max", max);
        }
        return this;
    }

    public String where() {
        return clauses.isEmpty() ? "" : " where " + String.join(" and ", clauses);
    }

    public void bind(Query query) {
        params.forEach(query::setParameter);
    }
}
