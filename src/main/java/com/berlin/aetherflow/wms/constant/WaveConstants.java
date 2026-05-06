package com.berlin.aetherflow.wms.constant;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * 波次规划常量。
 */
public final class WaveConstants {

    private WaveConstants() {
    }

    public static final String CODE_PREFIX = "WV";

    public static final String STATUS_DRAFT = "DRAFT";

    public static final String STATUS_RELEASED = "RELEASED";

    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String GROUP_RULE_MANUAL = "MANUAL";

    public static final String GROUP_RULE_BY_ORDER = "BY_ORDER";

    public static final String GROUP_RULE_BY_SKU = "BY_SKU";

    public static final String GROUP_RULE_BY_AREA = "BY_AREA";

    public static final String ACTION_RELEASE = "RELEASE";

    public static final String ACTION_CANCEL = "CANCEL";

    public static final String ACTION_GENERATE_PICKING = "GENERATE_PICKING";

    public static String normalizeStatus(String status) {
        return StringUtils.trimToEmpty(status).toUpperCase(Locale.ROOT);
    }

    public static String normalizeGroupRule(String groupRule) {
        String normalized = StringUtils.trimToEmpty(groupRule).toUpperCase(Locale.ROOT);
        return StringUtils.isBlank(normalized) ? GROUP_RULE_MANUAL : normalized;
    }

    public static boolean isValidStatus(String status) {
        return STATUS_DRAFT.equals(status)
                || STATUS_RELEASED.equals(status)
                || STATUS_CANCELLED.equals(status);
    }

    public static boolean isValidGroupRule(String groupRule) {
        return GROUP_RULE_MANUAL.equals(groupRule)
                || GROUP_RULE_BY_ORDER.equals(groupRule)
                || GROUP_RULE_BY_SKU.equals(groupRule)
                || GROUP_RULE_BY_AREA.equals(groupRule);
    }
}
