package com.fz.vibeverse.system.audit.model;

/**
 * 审计操作类型。
 */
public enum AuditType {
    OTHER("其它"),
    CREATE("新增"),
    UPDATE("修改"),
    DELETE("删除"),
    GRANT("授权"),
    IMPORT("导入"),
    EXPORT("导出"),
    LOGIN("登录"),
    LOGOUT("登出");

    private final String label;

    AuditType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
