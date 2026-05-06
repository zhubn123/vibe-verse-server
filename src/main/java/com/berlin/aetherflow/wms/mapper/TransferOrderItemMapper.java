package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.TransferOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 移库单明细 Mapper。
 */
@Mapper
public interface TransferOrderItemMapper extends BaseMapper<TransferOrderItem> {
}
