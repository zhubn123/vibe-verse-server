package com.berlin.aetherflow.wms.service;

import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrderItem;
import com.berlin.aetherflow.wms.domain.vo.OutboundAllocationPreviewVo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库库存分配 Service。
 */
public interface OutboundAllocationService {

    OutboundAllocationPreviewVo previewAllocation(Long orderId);

    Boolean allocate(Long orderId);

    Boolean releaseAllocation(Long orderId);

    boolean hasActiveAllocation(Long orderId);

    void ensureNoActiveAllocation(Long orderId);

    void consumeActiveAllocation(OutboundOrder order, List<OutboundOrderItem> orderItems, LocalDateTime operateTime);
}
