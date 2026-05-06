package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.StockCountItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盘点单明细 Mapper。
 */
@Mapper
public interface StockCountItemMapper extends BaseMapper<StockCountItem> {
}
