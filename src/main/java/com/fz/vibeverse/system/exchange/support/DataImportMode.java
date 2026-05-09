package com.fz.vibeverse.system.exchange.support;

/**
 * 导入落库策略。
 */
public enum DataImportMode {

    /**
     * 只新增，数据库已存在时失败。
     */
    INSERT_ONLY,

    /**
     * 有则更新，无则新增。
     */
    UPSERT,

    /**
     * 只更新，数据库不存在时失败。
     */
    UPDATE_ONLY
}
