package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.query.StockAgeQuery;
import com.berlin.aetherflow.wms.domain.vo.StockAgeSummaryVo;
import com.berlin.aetherflow.wms.domain.vo.StockAgeVo;
import com.berlin.aetherflow.wms.mapper.StockAgeMapper;
import com.berlin.aetherflow.wms.service.StockAgeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 库龄分析 Service 实现。
 */
@Service
@AllArgsConstructor
public class StockAgeServiceImpl implements StockAgeService {

    private final StockAgeMapper stockAgeMapper;

    @Override
    public PageResult<StockAgeVo> queryList(StockAgeQuery query) {
        query = normalizeQuery(query);
        LocalDate asOfDate = resolveAsOfDate(query);
        Page<StockAgeVo> page = new Page<>(query.getPageNo(), query.getPageSize());
        IPage<StockAgeVo> result = stockAgeMapper.selectStockAgePage(page, query, asOfDate);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), result.getRecords());
    }

    @Override
    public StockAgeSummaryVo querySummary(StockAgeQuery query) {
        query = normalizeQuery(query);
        LocalDate asOfDate = resolveAsOfDate(query);
        return stockAgeMapper.selectStockAgeSummary(query, asOfDate);
    }

    private StockAgeQuery normalizeQuery(StockAgeQuery query) {
        return query == null ? new StockAgeQuery() : query;
    }

    private LocalDate resolveAsOfDate(StockAgeQuery query) {
        if (query.getAsOfDate() == null) {
            return LocalDate.now();
        }
        return query.getAsOfDate();
    }
}
