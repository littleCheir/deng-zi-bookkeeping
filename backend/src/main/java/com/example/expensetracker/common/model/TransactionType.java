package com.example.expensetracker.common.model;

/**
 * 收支类型：数据库与 JSON 中都只允许 {@code INCOME} / {@code EXPENSE}。
 */
public enum TransactionType {

    INCOME,
    EXPENSE;

    /**
     * 按名称解析收支类型，无法识别时返回 {@code null}（由调用方决定如何报错）。
     */
    public static TransactionType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TransactionType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
