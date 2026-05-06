--liquibase formatted sql

--changeset codex:20260502-2030-system-dictionary-schema labels:system context:all
--comment: 系统字典类型与字典项表
create table if not exists sys_dict_type
(
    id          bigint primary key comment '主键ID',
    dict_code   varchar(128) not null comment '字典编码',
    dict_name   varchar(128) not null comment '字典名称',
    module      varchar(64)  not null default 'system' comment '所属模块',
    status      tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark      varchar(255) not null default '' comment '备注',
    create_by   varchar(64)  not null default '' comment '创建人',
    create_time datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)  not null default '' comment '更新人',
    update_time datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_dict_type_code (dict_code),
    key idx_sys_dict_type_module (module)
) engine = InnoDB comment '系统字典类型表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_dict_item
(
    id          bigint primary key comment '主键ID',
    dict_code   varchar(128) not null comment '字典编码',
    item_value  varchar(128) not null comment '字典项值',
    item_label  varchar(128) not null comment '字典项标签',
    sort_order  int          not null default 0 comment '排序号',
    status      tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark      varchar(255) not null default '' comment '备注',
    create_by   varchar(64)  not null default '' comment '创建人',
    create_time datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)  not null default '' comment '更新人',
    update_time datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_dict_item_value (dict_code, item_value),
    key idx_sys_dict_item_code_status_sort (dict_code, status, sort_order)
) engine = InnoDB comment '系统字典项表'
  collate = utf8mb4_unicode_ci;

--changeset codex:20260502-2031-system-dictionary-seed labels:system context:all
--comment: 系统字典基线数据与读取权限（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001030, 'system:dict:view', '字典查看', 'system', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002030, 1930000000000000001, 1930000000000001030, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002130, 1930000000000000002, 1930000000000001030, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002230, 1930000000000000003, 1930000000000001030, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_type (id, dict_code, dict_name, module, status, remark, create_by, create_time, update_by, update_time)
values (2030000000000001001, 'common_enable_status', '通用启停状态', 'system', 0, '系统通用启停状态', 'liquibase', now(), 'liquibase', now()),
       (2030000000000001002, 'wms_order_status', 'WMS 单据状态', 'wms', 0, '入库、出库、库存调整单通用状态', 'liquibase', now(), 'liquibase', now()),
       (2030000000000001003, 'wms_inventory_adjust_type', 'WMS 库存调整方向', 'wms', 0, '库存调整单方向', 'liquibase', now(), 'liquibase', now()),
       (2030000000000001004, 'wms_stock_biz_type', 'WMS 库存流水业务类型', 'wms', 0, '库存流水来源类型', 'liquibase', now(), 'liquibase', now()),
       (2030000000000001005, 'wms_stock_freeze_type', 'WMS 库存冻结类型', 'wms', 0, '库存冻结/解冻类型', 'liquibase', now(), 'liquibase', now()),
       (2030000000000001006, 'wms_outbound_allocation_status', 'WMS 出库分配状态', 'wms', 0, '出库库存分配状态', 'liquibase', now(), 'liquibase', now()),
       (2030000000000001007, 'wms_area_type', 'WMS 区域类型', 'wms', 0, '仓库区域业务类型', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    dict_name = values(dict_name),
    module = values(module),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_item (id, dict_code, item_value, item_label, sort_order, status, remark, create_by, create_time, update_by, update_time)
values (2030000000000002001, 'common_enable_status', '0', '正常', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002002, 'common_enable_status', '1', '停用', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002011, 'wms_order_status', '0', '草稿', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002012, 'wms_order_status', '1', '已确认', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002021, 'wms_inventory_adjust_type', 'INCREASE', '增加库存', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002022, 'wms_inventory_adjust_type', 'DECREASE', '减少库存', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002031, 'wms_stock_biz_type', 'INBOUND_ORDER', '入库确认', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002032, 'wms_stock_biz_type', 'OUTBOUND_ORDER', '出库确认', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002033, 'wms_stock_biz_type', 'INVENTORY_ADJUSTMENT', '库存调整', 3, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002034, 'wms_stock_biz_type', 'STOCK_FREEZE', '库存冻结', 4, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002035, 'wms_stock_biz_type', 'STOCK_UNFREEZE', '库存解冻', 5, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002041, 'wms_stock_freeze_type', 'MANUAL', '人工冻结', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002042, 'wms_stock_freeze_type', 'QUALITY', '质量冻结', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002043, 'wms_stock_freeze_type', 'COUNT', '盘点冻结', 3, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002044, 'wms_stock_freeze_type', 'EXCEPTION', '异常冻结', 4, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002051, 'wms_outbound_allocation_status', 'UNALLOCATED', '未分配', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002052, 'wms_outbound_allocation_status', 'ACTIVE', '已分配', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002053, 'wms_outbound_allocation_status', 'RELEASED', '已释放', 3, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002054, 'wms_outbound_allocation_status', 'CONSUMED', '已出库', 4, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002061, 'wms_area_type', 'STORAGE', '存储区', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002062, 'wms_area_type', 'RECEIVING', '收货区', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002063, 'wms_area_type', 'PICKING', '拣货区', 3, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002064, 'wms_area_type', 'SHIPPING', '发货区', 4, 0, '', 'liquibase', now(), 'liquibase', now()),
       (2030000000000002065, 'wms_area_type', 'RETURN', '退货区', 5, 0, '', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    item_label = values(item_label),
    sort_order = values(sort_order),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);
