package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.OutboundAllocation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库库存分配 Mapper。
 */
@Mapper
public interface OutboundAllocationMapper extends BaseMapper<OutboundAllocation> {
}
