package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.PickingTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 拣货任务 Mapper。
 */
@Mapper
public interface PickingTaskMapper extends BaseMapper<PickingTask> {
}
