--liquibase formatted sql

--changeset codex:20260502-2355-wms-picking-task labels:wms context:all
--comment: 新增拣货任务和拣货任务明细，用于出库分配后的拣货作业闭环
create table if not exists picking_task
(
    id                bigint primary key comment '主键ID',
    task_no           varchar(64)    not null comment '拣货任务号',
    outbound_order_id bigint         not null comment '出库单ID',
    outbound_order_no varchar(64)    not null default '' comment '出库单号',
    warehouse_id      bigint         not null comment '仓库ID',
    status            varchar(16)    not null default 'PENDING' comment '状态（PENDING/COMPLETED/CANCELLED）',
    total_qty         decimal(18, 2) not null default 0 comment '应拣数量',
    picked_qty        decimal(18, 2) not null default 0 comment '已拣数量',
    picking_time      datetime       null comment '拣货完成时间',
    remark            varchar(255)   not null default '' comment '备注',
    create_by         varchar(64)    not null default '' comment '创建人',
    create_time       datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by         varchar(64)    not null default '' comment '更新人',
    update_time       datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_picking_task_no (task_no),
    unique key uk_picking_task_outbound_order_id (outbound_order_id),
    key idx_picking_task_warehouse_status (warehouse_id, status),
    key idx_picking_task_status (status)
) engine = InnoDB comment '拣货任务'
  collate = utf8mb4_unicode_ci;

create table if not exists picking_task_item
(
    id                     bigint primary key comment '主键ID',
    task_id                bigint         not null comment '拣货任务ID',
    outbound_order_item_id bigint         not null comment '出库单明细ID',
    allocation_id          bigint         not null comment '出库库存分配ID',
    inventory_id           bigint         not null comment '库存ID',
    line_no                int            not null default 1 comment '行号',
    warehouse_id           bigint         not null comment '仓库ID',
    location_id            bigint         not null comment '库位ID',
    material_id            bigint         not null comment '物料ID',
    batch_no               varchar(128)   not null default '' comment '批次号',
    production_date        date           null comment '生产日期',
    expiry_date            date           null comment '到期日期',
    planned_qty            decimal(18, 2) not null default 0 comment '应拣数量',
    picked_qty             decimal(18, 2) not null default 0 comment '已拣数量',
    status                 varchar(16)    not null default 'PENDING' comment '状态（PENDING/COMPLETED/CANCELLED）',
    remark                 varchar(255)   not null default '' comment '备注',
    create_by              varchar(64)    not null default '' comment '创建人',
    create_time            datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by              varchar(64)    not null default '' comment '更新人',
    update_time            datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_picking_task_item_allocation_id (allocation_id),
    key idx_picking_task_item_task_id (task_id),
    key idx_picking_task_item_outbound_order_item_id (outbound_order_item_id),
    key idx_picking_task_item_inventory_id (inventory_id),
    key idx_picking_task_item_location_id (location_id),
    key idx_picking_task_item_material_id (material_id),
    key idx_picking_task_item_status (status)
) engine = InnoDB comment '拣货任务明细'
  collate = utf8mb4_unicode_ci;
