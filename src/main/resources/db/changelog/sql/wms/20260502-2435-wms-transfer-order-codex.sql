--liquibase formatted sql

--changeset codex:20260502-2435-wms-transfer-order labels:wms context:all
--comment: 新增移库单和移库单明细，支持库内移库草稿、确认和库存流水
create table if not exists transfer_order
(
    id              bigint primary key comment '主键ID',
    order_no        varchar(32)  not null comment '移库单号',
    warehouse_id    bigint       not null comment '仓库ID',
    status          tinyint      not null default 0 comment '状态（0草稿 1已确认）',
    transfer_time   datetime     null comment '实际移库时间',
    transfer_reason varchar(128) not null comment '移库原因',
    remark          varchar(255) not null default '' comment '备注',
    create_by       varchar(64)  not null default '' comment '创建人',
    create_time     datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by       varchar(64)  not null default '' comment '更新人',
    update_time     datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_transfer_order_no (order_no),
    key idx_transfer_order_warehouse_id (warehouse_id),
    key idx_transfer_order_status (status)
) engine = InnoDB comment '移库单'
  collate = utf8mb4_unicode_ci;

create table if not exists transfer_order_item
(
    id                 bigint primary key comment '主键ID',
    order_id           bigint         not null comment '移库单ID',
    line_no            int            not null default 1 comment '行号',
    material_id        bigint         not null comment '物料ID',
    source_location_id bigint         not null comment '源库位ID',
    target_location_id bigint         not null comment '目标库位ID',
    batch_no           varchar(64)    not null default '' comment '批次号',
    production_date    date           null comment '生产日期',
    expiry_date        date           null comment '到期日期',
    transfer_qty       decimal(18, 2) not null default 0 comment '移库数量',
    remark             varchar(255)   not null default '' comment '备注',
    create_by          varchar(64)    not null default '' comment '创建人',
    create_time        datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by          varchar(64)    not null default '' comment '更新人',
    update_time        datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_transfer_order_item_line (order_id, line_no),
    key idx_transfer_order_item_order_id (order_id),
    key idx_transfer_order_item_material_id (material_id),
    key idx_transfer_order_item_source_location_id (source_location_id),
    key idx_transfer_order_item_target_location_id (target_location_id)
) engine = InnoDB comment '移库单明细'
  collate = utf8mb4_unicode_ci;
