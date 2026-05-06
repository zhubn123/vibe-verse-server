package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.wms.domain.bo.TransferOrderItemBo;
import com.berlin.aetherflow.wms.domain.entity.TransferOrderItem;
import com.berlin.aetherflow.wms.mapper.TransferOrderItemMapper;
import com.berlin.aetherflow.wms.service.TransferOrderItemService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 移库单明细 Service 实现。
 */
@Service
public class TransferOrderItemServiceImpl extends ServiceImpl<TransferOrderItemMapper, TransferOrderItem>
        implements TransferOrderItemService {

    @Override
    public void saveTransferOrderItems(List<TransferOrderItemBo> itemsBo) {
        if (itemsBo == null || itemsBo.isEmpty()) {
            return;
        }
        List<TransferOrderItem> items = MapstructUtils.convert(itemsBo, TransferOrderItem.class);
        saveBatch(items);
    }

    @Override
    public void replaceTransferOrderItems(Long orderId, List<TransferOrderItemBo> itemsBo) {
        remove(Wrappers.<TransferOrderItem>lambdaQuery().eq(TransferOrderItem::getOrderId, orderId));
        saveTransferOrderItems(itemsBo == null ? Collections.emptyList() : itemsBo);
    }
}
