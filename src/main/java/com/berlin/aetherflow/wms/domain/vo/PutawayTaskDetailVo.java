package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.wms.domain.entity.PutawayTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 上架任务详情 VO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PutawayTask.class, convertGenerate = false)
public class PutawayTaskDetailVo extends PutawayTaskVo {

    private List<PutawayTaskItemVo> items;
}
