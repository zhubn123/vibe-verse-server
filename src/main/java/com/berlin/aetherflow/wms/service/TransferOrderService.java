package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.TransferOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.TransferOrderBo;
import com.berlin.aetherflow.wms.domain.entity.TransferOrder;
import com.berlin.aetherflow.wms.domain.query.TransferOrderQuery;
import com.berlin.aetherflow.wms.domain.vo.TransferOrderDetailVo;
import com.berlin.aetherflow.wms.domain.vo.TransferOrderVo;

import java.util.List;

/**
 * 移库单 Service。
 */
public interface TransferOrderService extends IService<TransferOrder> {

    PageResult<TransferOrderVo> queryList(TransferOrderQuery query);

    TransferOrderDetailVo getDetailById(Long id);

    Long createTransferOrder(TransferOrderBo bo);

    Boolean updateTransferOrder(TransferOrderBo bo);

    Boolean applyAction(Long id, TransferOrderActionBo bo);

    Boolean removeTransferOrders(List<Long> ids);
}
