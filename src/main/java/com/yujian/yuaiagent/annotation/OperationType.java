package com.yujian.yuaiagent.annotation;

/**
 * 操作类型枚举
 */
public enum OperationType {

    /** 新增 */
    CREATE("创建"),

    /** 修改 */
    UPDATE("更新"),

    /** 删除 */
    DELETE("删除"),

    /** 查询 */
    QUERY("查询"),

    /** 导入 */
    IMPORT("导入"),

    /** 导出 */
    EXPORT("导出"),

    /** 其他 */
    OTHER("其他");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
