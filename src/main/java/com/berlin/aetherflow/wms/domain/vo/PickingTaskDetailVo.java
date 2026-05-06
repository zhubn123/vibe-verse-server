package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.wms.domain.entity.PickingTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 拣货任务详情 VO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PickingTask.class, convertGenerate = false)
public class PickingTaskDetailVo extends PickingTaskVo {

    private List<PickingTaskItemVo> items;
}
