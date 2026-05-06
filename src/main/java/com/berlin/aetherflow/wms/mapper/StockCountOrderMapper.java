package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.StockCountOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盘点单 Mapper。
 */
@Mapper
public interface StockCountOrderMapper extends BaseMapper<StockCountOrder> {
}
