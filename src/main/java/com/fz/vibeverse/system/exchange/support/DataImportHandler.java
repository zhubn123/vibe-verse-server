package com.fz.vibeverse.system.exchange.support;

import java.util.List;

/**
 * 业务导入处理器，只承载业务字段解析、校验和落库。
 *
 * @param <T> 解析后的业务行类型
 */
public interface DataImportHandler<T> {

    String scene();

    String name();

    List<String> headers();

    List<List<String>> sampleRows();

    DataImportMode mode();

    T parse(DataImportRow row);

    String uniqueKey(T data);

    default String uniqueKeyName() {
        return "数据";
    }

    void save(T data, DataImportMode mode);

    default String bucket() {
        return "import";
    }

    default String sourceRemark() {
        return name() + "导入源文件";
    }

    default String errorRemark() {
        return name() + "导入错误明细";
    }

    default String errorFilename(Long taskId) {
        return scene() + "-errors-" + taskId + ".csv";
    }

    default String rowIdentity(DataImportRow row) {
        String firstValue = row.valueAt(0);
        return firstValue == null ? "" : firstValue;
    }
}
