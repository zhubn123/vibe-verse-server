--liquibase formatted sql

--changeset codex:20260502-2310-wms-putaway-task labels:wms context:all
--comment: 新增上架任务和上架任务明细，用于入库确认后的作业闭环
create table if not exists putaway_task
(
    id               bigint primary key comment '主键ID',
    task_no          varchar(64)    not null comment '上架任务号',
    inbound_order_id bigint         not null comment '入库单ID',
    inbound_order_no varchar(64)    not null default '' comment '入库单号',
    warehouse_id     bigint         not null comment '仓库ID',
    status           varchar(16)    not null default 'PENDING' comment '状态（PENDING/COMPLETED/CANCELLED）',
    total_qty        decimal(18, 2) not null default 0 comment '应上架数量',
    completed_qty    decimal(18, 2) not null default 0 comment '已上架数量',
    putaway_time     datetime       null comment '上架完成时间',
    remark           varchar(255)   not null default '' comment '备注',
    create_by        varchar(64)    not null default '' comment '创建人',
    create_time      datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by        varchar(64)    not null default '' comment '更新人',
    update_time      datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_putaway_task_no (task_no),
    unique key uk_putaway_task_inbound_order_id (inbound_order_id),
    key idx_putaway_task_warehouse_status (warehouse_id, status),
    key idx_putaway_task_status (status)
) engine = InnoDB comment '上架任务'
  collate = utf8mb4_unicode_ci;

create table if not exists putaway_task_item
(
    id                    bigint primary key comment '主键ID',
    task_id               bigint         not null comment '上架任务ID',
    inbound_order_item_id bigint         not null comment '入库单明细ID',
    line_no               int            not null default 1 comment '行号',
    material_id           bigint         not null comment '物料ID',
    location_id           bigint         not null comment '目标库位ID',
    batch_no              varchar(128)   not null default '' comment '批次号',
    production_date       date           null comment '生产日期',
    expiry_date           date           null comment '到期日期',
    planned_qty           decimal(18, 2) not null default 0 comment '应上架数量',
    completed_qty         decimal(18, 2) not null default 0 comment '已上架数量',
    status                varchar(16)    not null default 'PENDING' comment '状态（PENDING/COMPLETED/CANCELLED）',
    remark                varchar(255)   not null default '' comment '备注',
    create_by             varchar(64)    not null default '' comment '创建人',
    create_time           datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by             varchar(64)    not null default '' comment '更新人',
    update_time           datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_putaway_task_item_task_id (task_id),
    key idx_putaway_task_item_inbound_order_item_id (inbound_order_item_id),
    key idx_putaway_task_item_location_id (location_id),
    key idx_putaway_task_item_material_id (material_id),
    key idx_putaway_task_item_status (status)
) engine = InnoDB comment '上架任务明细'
  collate = utf8mb4_unicode_ci;
