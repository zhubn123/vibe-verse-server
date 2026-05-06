package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.StockCountActionBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountCreateBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountItemsBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountReviewItemsBo;
import com.berlin.aetherflow.wms.domain.entity.StockCountOrder;
import com.berlin.aetherflow.wms.domain.query.StockCountQuery;
import com.berlin.aetherflow.wms.domain.vo.StockCountDetailVo;
import com.berlin.aetherflow.wms.domain.vo.StockCountVo;

/**
 * 盘点单 Service。
 */
public interface StockCountService extends IService<StockCountOrder> {

    PageResult<StockCountVo> queryList(StockCountQuery query);

    StockCountDetailVo getDetailById(Long id);

    Long createStockCount(StockCountCreateBo bo);

    Boolean saveCountItems(Long id, StockCountItemsBo bo);

    Boolean saveReviewItems(Long id, StockCountReviewItemsBo bo);

    Boolean applyAction(Long id, StockCountActionBo bo);
}
