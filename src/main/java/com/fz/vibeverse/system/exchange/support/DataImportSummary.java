package com.fz.vibeverse.system.exchange.support;

import java.util.List;

/**
 * 导入执行汇总。
 */
public record DataImportSummary(int totalCount, int successCount, int failCount, List<DataImportError> errors) {
}
