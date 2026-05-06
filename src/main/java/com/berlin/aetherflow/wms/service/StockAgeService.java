package com.berlin.aetherflow.wms.service;

import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.query.StockAgeQuery;
import com.berlin.aetherflow.wms.domain.vo.StockAgeSummaryVo;
import com.berlin.aetherflow.wms.domain.vo.StockAgeVo;

/**
 * 库龄分析 Service。
 */
public interface StockAgeService {

    PageResult<StockAgeVo> queryList(StockAgeQuery query);

    StockAgeSummaryVo querySummary(StockAgeQuery query);
}
