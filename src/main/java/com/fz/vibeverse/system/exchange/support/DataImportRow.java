package com.fz.vibeverse.system.exchange.support;

import java.util.List;

/**
 * CSV 导入行。
 */
public record DataImportRow(int rowNo, List<String> values) {

    public String valueAt(int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }
}
