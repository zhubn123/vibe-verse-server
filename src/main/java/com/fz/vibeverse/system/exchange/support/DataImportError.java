package com.fz.vibeverse.system.exchange.support;

/**
 * 导入行错误。
 */
public record DataImportError(int rowNo, String identity, String errorMessage) {
}
