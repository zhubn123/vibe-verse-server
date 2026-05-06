package com.fz.vibeverse.system.dict.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fz.vibeverse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统字典项实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 字典项值
     */
    private String itemValue;

    /**
     * 字典项标签
     */
    private String itemLabel;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
