package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.wms.domain.entity.StockCountOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 盘点单详情 VO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = StockCountOrder.class, convertGenerate = false)
public class StockCountDetailVo extends StockCountVo {

    private List<StockCountItemVo> items;
}
