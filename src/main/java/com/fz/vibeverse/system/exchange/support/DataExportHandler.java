package com.fz.vibeverse.system.exchange.support;

import java.util.List;
import java.util.Map;

/**
 * 业务导出处理器，只承载数据查询和行构建。
 *
 * @param <T> 导出数据行业务类型
 */
public interface DataExportHandler<T> {

    String scene();

    String name();

    List<String> headers();

    List<?> buildRow(T data);

    List<T> queryData(Map<String, String> params);

    default String bucket() {
        return "export";
    }

    default String resultRemark() {
        return name() + "导出结果文件";
    }

    default String resultFilename() {
        return scene() + "-" + System.currentTimeMillis() + ".csv";
    }
}
