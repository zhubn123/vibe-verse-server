package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.wms.domain.entity.TransferOrder;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 移库单参数。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = TransferOrder.class, reverseConvertGenerate = false)
public class TransferOrderBo extends BaseEntity {

    @Null(message = "创建移库单时ID必须为空", groups = CreateGroup.class)
    private Long id;

    private String orderNo;

    @NotNull(message = "仓库不能为空", groups = {Default.class, CreateGroup.class})
    private Long warehouseId;

    private Integer status;

    private LocalDateTime transferTime;

    @NotBlank(message = "移库原因不能为空", groups = {Default.class, CreateGroup.class})
    @Size(max = 128, message = "移库原因长度不能超过128个字符", groups = {Default.class, CreateGroup.class})
    private String transferReason;

    @Size(max = 255, message = "备注长度不能超过255个字符", groups = {Default.class, CreateGroup.class})
    private String remark;

    @NotNull(message = "移库单明细不能为空", groups = CreateGroup.class)
    @Size(min = 1, message = "移库单明细不能为空", groups = {Default.class, CreateGroup.class})
    private List<@NotNull(message = "移库单明细项不能为空", groups = {Default.class, CreateGroup.class}) @Valid TransferOrderItemBo> orderItemsBo;
}
