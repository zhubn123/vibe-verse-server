package com.berlin.aetherflow.wms.constant;

/**
 * 库存流水业务类型常量。
 */
public interface StockBizTypeConst {

    /**
     * 入库单确认入账。
     */
    String INBOUND_ORDER = "INBOUND_ORDER";

    /**
     * 出库单确认出账。
     */
    String OUTBOUND_ORDER = "OUTBOUND_ORDER";

    /**
     * 库存调整单确认生效。
     */
    String INVENTORY_ADJUSTMENT = "INVENTORY_ADJUSTMENT";

    /**
     * 移库单确认生效。
     */
    String TRANSFER_ORDER = "TRANSFER_ORDER";

    /**
     * 库存冻结。
     */
    String STOCK_FREEZE = "STOCK_FREEZE";

    /**
     * 库存解冻。
     */
    String STOCK_UNFREEZE = "STOCK_UNFREEZE";

    /**
     * 盘点差异调账。
     */
    String STOCK_COUNT = "STOCK_COUNT";
}
