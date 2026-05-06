package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
import com.berlin.aetherflow.wms.domain.bo.StockFreezeBo;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import com.berlin.aetherflow.wms.domain.query.InventoryExpiryWarningQuery;
import com.berlin.aetherflow.wms.domain.query.InventoryQuery;
import com.berlin.aetherflow.wms.domain.vo.ExpiredInventoryFreezeResultVo;
import com.berlin.aetherflow.wms.domain.vo.InventoryExpiryWarningVo;
import com.berlin.aetherflow.wms.domain.vo.InventoryVo;

import java.util.List;

/**
* @author berlin
* @description 针对表【stock(库存表)】的数据库操作Service
* @createDate 2026-04-15 16:17:27
*/
public interface InventoryService extends IService<Inventory> {

    PageResult<InventoryVo> queryList(InventoryQuery query);

    PageResult<InventoryExpiryWarningVo> queryExpiryWarnings(InventoryExpiryWarningQuery query);

    void applyStockChanges(List<StockChangeBo> changes);

    void consumeLockedStockChanges(List<StockChangeBo> changes);

    Boolean freezeStock(Long id, StockFreezeBo bo);

    Boolean unfreezeStock(Long id, StockFreezeBo bo);

    ExpiredInventoryFreezeResultVo freezeExpiredStocks();
}
