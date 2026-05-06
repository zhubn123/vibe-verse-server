package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.wms.domain.entity.WaveOrder;
import io.github.linpeilie.annotations.AutoMapper;
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
 * 波次保存参数。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = WaveOrder.class, reverseConvertGenerate = false)
public class WaveBo extends BaseEntity {

    @Null(message = "创建波次时ID必须为空", groups = CreateGroup.class)
    private Long id;

    @Size(max = 64, message = "波次号长度不能超过64个字符", groups = {Default.class, CreateGroup.class})
    private String waveNo;

    @NotNull(message = "仓库不能为空", groups = CreateGroup.class)
    private Long warehouseId;

    @Size(max = 16, message = "波次状态长度不能超过16个字符", groups = {Default.class, CreateGroup.class})
    private String status;

    @Size(max = 16, message = "分组规则长度不能超过16个字符", groups = {Default.class, CreateGroup.class})
    private String groupRule;

    private LocalDateTime releaseTime;

    private LocalDateTime cancelTime;

    @Size(max = 255, message = "备注长度不能超过255个字符", groups = {Default.class, CreateGroup.class})
    private String remark;

    @NotNull(message = "波次出库单不能为空", groups = CreateGroup.class)
    @Size(min = 1, message = "波次出库单不能为空", groups = {Default.class, CreateGroup.class})
    private List<@NotNull(message = "出库单ID不能为空", groups = {Default.class, CreateGroup.class}) Long> outboundOrderIds;
}
