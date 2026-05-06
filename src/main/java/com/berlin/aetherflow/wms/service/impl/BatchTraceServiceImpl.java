package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.query.BatchTraceDetailQuery;
import com.berlin.aetherflow.wms.domain.query.BatchTraceQuery;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceDetailVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceInventoryVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceOrderVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceTransactionVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceVo;
import com.berlin.aetherflow.wms.mapper.BatchTraceMapper;
import com.berlin.aetherflow.wms.service.BatchTraceService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批次追溯 Service 实现。
 */
@Service
@AllArgsConstructor
public class BatchTraceServiceImpl implements BatchTraceService {

    private final BatchTraceMapper batchTraceMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<BatchTraceVo> queryList(BatchTraceQuery query) {
        query = normalizeQuery(query);
        Page<BatchTraceVo> page = new Page<>(query.getPageNo(), query.getPageSize());
        IPage<BatchTraceVo> result = batchTraceMapper.selectBatchTracePage(page, query);
        List<BatchTraceVo> records = result.getRecords().stream()
                .peek(this::normalizeTraceVo)
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchTraceDetailVo queryDetail(BatchTraceDetailQuery query) {
        query = normalizeDetailQuery(query);
        BatchTraceDetailVo detail = batchTraceMapper.selectBatchTraceDetail(query);
        if (detail == null) {
            detail = new BatchTraceDetailVo();
            detail.setWarehouseId(query.getWarehouseId());
            detail.setMaterialId(query.getMaterialId());
            detail.setBatchNo(query.getBatchNo());
        }

        normalizeDetailVo(detail);
        List<BatchTraceInventoryVo> inventories = batchTraceMapper.selectBatchTraceInventories(query);
        inventories.forEach(this::normalizeInventoryVo);
        detail.setInventories(inventories);

        List<BatchTraceTransactionVo> transactions = batchTraceMapper.selectBatchTraceTransactions(query);
        transactions.forEach(this::normalizeTransactionVo);
        detail.setTransactions(transactions);

        List<BatchTraceOrderVo> orders = batchTraceMapper.selectBatchTraceOrders(query);
        orders.forEach(this::normalizeOrderVo);
        detail.setOrders(orders);
        return detail;
    }

    private BatchTraceQuery normalizeQuery(BatchTraceQuery query) {
        BatchTraceQuery normalized = query == null ? new BatchTraceQuery() : query;
        if (normalized.getBatchNo() != null) {
            normalized.setBatchNo(normalizeBatchNo(normalized.getBatchNo()));
        }
        return normalized;
    }

    private BatchTraceDetailQuery normalizeDetailQuery(BatchTraceDetailQuery query) {
        if (query == null) {
            throw new RuntimeException("批次追溯查询参数不能为空");
        }
        if (query.getMaterialId() == null) {
            throw new RuntimeException("物料不能为空");
        }
        if (query.getBatchNo() == null) {
            throw new RuntimeException("批次号参数不能为空，空批次请传空字符串");
        }
        query.setBatchNo(normalizeBatchNo(query.getBatchNo()));
        return query;
    }

    private String normalizeBatchNo(String batchNo) {
        return StringUtils.defaultString(StringUtils.trimToNull(batchNo));
    }

    private void normalizeTraceVo(BatchTraceVo vo) {
        if (vo == null) {
            return;
        }
        vo.setQuantity(zeroIfNull(vo.getQuantity()));
        vo.setLockedQuantity(zeroIfNull(vo.getLockedQuantity()));
        vo.setFrozenQuantity(zeroIfNull(vo.getFrozenQuantity()));
        vo.setAvailableQuantity(zeroIfNull(vo.getAvailableQuantity()));
        vo.setLocationCount(zeroIfNull(vo.getLocationCount()));
        vo.setTransactionCount(zeroIfNull(vo.getTransactionCount()));
        vo.setBatchNo(normalizeBatchNo(vo.getBatchNo()));
    }

    private void normalizeDetailVo(BatchTraceDetailVo vo) {
        vo.setQuantity(zeroIfNull(vo.getQuantity()));
        vo.setLockedQuantity(zeroIfNull(vo.getLockedQuantity()));
        vo.setFrozenQuantity(zeroIfNull(vo.getFrozenQuantity()));
        vo.setAvailableQuantity(zeroIfNull(vo.getAvailableQuantity()));
        vo.setLocationCount(zeroIfNull(vo.getLocationCount()));
        vo.setTransactionCount(zeroIfNull(vo.getTransactionCount()));
        vo.setBatchNo(normalizeBatchNo(vo.getBatchNo()));
    }

    private void normalizeInventoryVo(BatchTraceInventoryVo vo) {
        vo.setQuantity(zeroIfNull(vo.getQuantity()));
        vo.setLockedQuantity(zeroIfNull(vo.getLockedQuantity()));
        vo.setFrozenQuantity(zeroIfNull(vo.getFrozenQuantity()));
        vo.setAvailableQuantity(zeroIfNull(vo.getAvailableQuantity()));
        vo.setBatchNo(normalizeBatchNo(vo.getBatchNo()));
    }

    private void normalizeTransactionVo(BatchTraceTransactionVo vo) {
        vo.setChangeQty(zeroIfNull(vo.getChangeQty()));
        vo.setBeforeQty(zeroIfNull(vo.getBeforeQty()));
        vo.setAfterQty(zeroIfNull(vo.getAfterQty()));
        vo.setBatchNo(normalizeBatchNo(vo.getBatchNo()));
    }

    private void normalizeOrderVo(BatchTraceOrderVo vo) {
        vo.setPlannedQuantity(zeroIfNull(vo.getPlannedQuantity()));
        vo.setActualQuantity(zeroIfNull(vo.getActualQuantity()));
        vo.setDifferenceQuantity(zeroIfNull(vo.getDifferenceQuantity()));
        vo.setBatchNo(normalizeBatchNo(vo.getBatchNo()));
    }

    private BigDecimal zeroIfNull(BigDecimal quantity) {
        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    private Long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }
}
