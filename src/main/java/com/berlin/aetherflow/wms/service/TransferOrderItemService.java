package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.wms.domain.bo.TransferOrderItemBo;
import com.berlin.aetherflow.wms.domain.entity.TransferOrderItem;

import java.util.List;

/**
 * 移库单明细 Service。
 */
public interface TransferOrderItemService extends IService<TransferOrderItem> {

    void saveTransferOrderItems(List<TransferOrderItemBo> itemsBo);

    void replaceTransferOrderItems(Long orderId, List<TransferOrderItemBo> itemsBo);
}
