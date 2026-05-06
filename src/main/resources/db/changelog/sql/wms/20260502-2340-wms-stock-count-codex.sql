--liquibase formatted sql

--changeset codex:20260502-2340-wms-stock-count labels:wms context:all
--comment: 新增库存盘点单和盘点明细，用于快照库存、录入实盘数量并按差异调账
create table if not exists stock_count_order
(
    id               bigint primary key comment '主键ID',
    count_no         varchar(64)    not null comment '盘点单号',
    warehouse_id     bigint         not null comment '仓库ID',
    status           varchar(16)    not null default 'PENDING' comment '状态（PENDING/ADJUSTED/CANCELLED）',
    total_items      int            not null default 0 comment '明细总数',
    difference_items int            not null default 0 comment '差异明细数',
    expected_qty     decimal(18, 2) not null default 0 comment '账面总数量',
    counted_qty      decimal(18, 2) not null default 0 comment '实盘总数量',
    difference_qty   decimal(18, 2) not null default 0 comment '差异总数量',
    count_time       datetime       null comment '盘点创建时间',
    adjust_time      datetime       null comment '调账时间',
    remark           varchar(255)   not null default '' comment '备注',
    create_by        varchar(64)    not null default '' comment '创建人',
    create_time      datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by        varchar(64)    not null default '' comment '更新人',
    update_time      datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_stock_count_order_no (count_no),
    key idx_stock_count_warehouse_status (warehouse_id, status),
    key idx_stock_count_status (status)
) engine = InnoDB comment '库存盘点单'
  collate = utf8mb4_unicode_ci;

create table if not exists stock_count_item
(
    id              bigint primary key comment '主键ID',
    count_id        bigint         not null comment '盘点单ID',
    inventory_id    bigint         not null comment '库存ID',
    line_no         int            not null default 1 comment '行号',
    warehouse_id    bigint         not null comment '仓库ID',
    area_id         bigint         null comment '区域ID',
    location_id     bigint         not null comment '库位ID',
    material_id     bigint         not null comment '物料ID',
    batch_no        varchar(128)   not null default '' comment '批次号',
    production_date date           null comment '生产日期',
    expiry_date     date           null comment '到期日期',
    expected_qty    decimal(18, 2) not null default 0 comment '账面数量',
    counted_qty     decimal(18, 2) null comment '实盘数量',
    difference_qty  decimal(18, 2) not null default 0 comment '差异数量',
    remark          varchar(255)   not null default '' comment '备注',
    create_by       varchar(64)    not null default '' comment '创建人',
    create_time     datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by       varchar(64)    not null default '' comment '更新人',
    update_time     datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_stock_count_item_line (count_id, line_no),
    key idx_stock_count_item_count_id (count_id),
    key idx_stock_count_item_inventory_id (inventory_id),
    key idx_stock_count_item_warehouse_location (warehouse_id, location_id),
    key idx_stock_count_item_material_batch (material_id, batch_no)
) engine = InnoDB comment '库存盘点明细'
  collate = utf8mb4_unicode_ci;
