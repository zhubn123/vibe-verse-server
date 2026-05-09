--liquibase formatted sql

--changeset fz:data-exchange-task-schema labels:system context:all
--comment: 导入导出任务表
create table if not exists sys_data_exchange_task
(
    id               bigint primary key comment '主键ID',
    direction        varchar(16)  not null comment '方向（IMPORT/EXPORT）',
    scene            varchar(64)  not null comment '场景标识',
    status           varchar(16)  not null comment '状态（PENDING/RUNNING/SUCCESS/FAILED）',
    source_object_id bigint       null comment '导入源文件对象ID',
    result_object_id bigint       null comment '导出结果文件对象ID',
    error_object_id  bigint       null comment '错误明细文件对象ID',
    total_count      int          not null default 0 comment '总记录数',
    success_count    int          not null default 0 comment '成功记录数',
    fail_count       int          not null default 0 comment '失败记录数',
    message          varchar(512) not null default '' comment '任务消息',
    remark           varchar(255) not null default '' comment '备注',
    start_time       datetime     null comment '开始时间',
    finish_time      datetime     null comment '完成时间',
    create_by        varchar(64)  not null default '' comment '创建人',
    create_time      datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by        varchar(64)  not null default '' comment '更新人',
    update_time      datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_data_exchange_direction_status (direction, status),
    key idx_data_exchange_scene (scene),
    key idx_data_exchange_create_time (create_time),
    key idx_data_exchange_source_object_id (source_object_id),
    key idx_data_exchange_result_object_id (result_object_id),
    key idx_data_exchange_error_object_id (error_object_id)
) engine = InnoDB comment '导入导出任务表'
  collate = utf8mb4_unicode_ci;

--changeset fz:data-exchange-task-feature-seed labels:system context:all
--comment: 导入导出权限与菜单 seed
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001015, 'system:exchange:view', '导入导出查看', 'system', 'view', 1, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001016, 'system:exchange:manage', '导入导出管理', 'system', 'manage', 1, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002015, 1930000000000000001, 1930000000000001015, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002016, 1930000000000000001, 1930000000000001016, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_menu (id, parent_id, menu_key, title, path, icon, permission_key, sort_order, visible, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000004015, 1930000000000004014, 'system-exchange-tasks', '导入导出', '/system/exchange-tasks', 'ArrowDownUp', 'system:exchange:view', 30, 1, 1, '系统默认菜单', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    parent_id = values(parent_id),
    title = values(title),
    path = values(path),
    icon = values(icon),
    permission_key = values(permission_key),
    sort_order = values(sort_order),
    visible = values(visible),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

--changeset fz:data-exchange-record-menu-rename labels:system context:all
--comment: 将通用导入导出入口调整为记录查看入口
update sys_permission
set perm_name = case perm_key
                    when 'system:exchange:view' then '导入导出记录查看'
                    when 'system:exchange:manage' then '导入导出记录管理'
                    else perm_name
                end,
    update_by = 'liquibase',
    update_time = now()
where perm_key in ('system:exchange:view', 'system:exchange:manage');

update sys_menu
set title = '导入导出记录',
    update_by = 'liquibase',
    update_time = now()
where menu_key = 'system-exchange-tasks';
