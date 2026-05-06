package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.PickingTaskItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 拣货任务明细 Mapper。
 */
@Mapper
public interface PickingTaskItemMapper extends BaseMapper<PickingTaskItem> {
}
