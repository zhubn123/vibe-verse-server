package com.fz.vibeverse.system.exchange.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fz.vibeverse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导入导出任务实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_data_exchange_task")
public class DataExchangeTask extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 方向：IMPORT / EXPORT。
     */
    private String direction;

    /**
     * 场景标识。
     */
    private String scene;

    /**
     * 状态：PENDING / RUNNING / SUCCESS / FAILED。
     */
    private String status;

    /**
     * 导入源文件对象 ID。
     */
    private Long sourceObjectId;

    /**
     * 导出结果文件对象 ID。
     */
    private Long resultObjectId;

    /**
     * 错误明细文件对象 ID。
     */
    private Long errorObjectId;

    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private String message;
    private String remark;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
}
