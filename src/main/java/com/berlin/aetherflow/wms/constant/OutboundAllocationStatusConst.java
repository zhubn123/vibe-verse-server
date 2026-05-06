package com.berlin.aetherflow.wms.constant;

/**
 * 出库库存分配状态常量。
 */
public interface OutboundAllocationStatusConst {

    /**
     * 未分配。
     */
    String UNALLOCATED = "UNALLOCATED";

    /**
     * 已锁定，等待出库确认或释放。
     */
    String ACTIVE = "ACTIVE";

    /**
     * 已释放。
     */
    String RELEASED = "RELEASED";

    /**
     * 已随出库确认消费。
     */
    String CONSUMED = "CONSUMED";

    static String labelOf(String status) {
        if (ACTIVE.equals(status)) {
            return "已分配";
        }
        if (RELEASED.equals(status)) {
            return "已释放";
        }
        if (CONSUMED.equals(status)) {
            return "已出库";
        }
        return "未分配";
    }
}
