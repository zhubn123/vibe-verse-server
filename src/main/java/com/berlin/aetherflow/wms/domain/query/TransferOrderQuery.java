package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.wms.domain.entity.TransferOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 移库单查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = TransferOrder.class, reverseConvertGenerate = false)
public class TransferOrderQuery extends PageQuery {

    private String orderNo;

    private Long warehouseId;

    private Integer status;

    private LocalDateTime transferStartTime;

    private LocalDateTime transferEndTime;

    private String transferReason;

    private String remark;
}
