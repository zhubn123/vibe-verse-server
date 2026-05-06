package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.wms.domain.entity.TransferOrderItem;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 移库单明细参数。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = TransferOrderItem.class, reverseConvertGenerate = false)
public class TransferOrderItemBo extends BaseEntity {

    private Long id;

    private Long orderId;

    @Positive(message = "行号必须大于0", groups = {Default.class, CreateGroup.class})
    private Integer lineNo;

    @NotNull(message = "移库单明细物料不能为空", groups = {Default.class, CreateGroup.class})
    private Long materialId;

    @NotNull(message = "移库单明细源库位不能为空", groups = {Default.class, CreateGroup.class})
    private Long sourceLocationId;

    @NotNull(message = "移库单明细目标库位不能为空", groups = {Default.class, CreateGroup.class})
    private Long targetLocationId;

    @Size(max = 64, message = "批次号长度不能超过64个字符", groups = {Default.class, CreateGroup.class})
    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    @NotNull(message = "移库单明细数量不能为空", groups = {Default.class, CreateGroup.class})
    @DecimalMin(value = "0.01", message = "移库单明细数量必须大于0", groups = {Default.class, CreateGroup.class})
    private BigDecimal transferQty;

    @Size(max = 255, message = "移库单明细备注长度不能超过255个字符", groups = {Default.class, CreateGroup.class})
    private String remark;
}
