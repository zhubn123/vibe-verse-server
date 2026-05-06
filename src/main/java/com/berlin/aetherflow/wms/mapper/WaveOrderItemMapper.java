package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.WaveOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 波次出库单明细 Mapper。
 */
@Mapper
public interface WaveOrderItemMapper extends BaseMapper<WaveOrderItem> {
}
