--liquibase formatted sql

--changeset fz:reminder-schema labels:system context:all
--comment: 站内提醒表
create table if not exists sys_reminder
(
    id           bigint primary key comment '主键ID',
    user_id      bigint        not null comment '提醒所属用户ID',
    title        varchar(128)  not null comment '提醒标题',
    content      varchar(1000) not null default '' comment '提醒内容',
    remind_time  datetime      not null comment '提醒时间',
    status       varchar(16)   not null comment '状态（PENDING/DONE/CANCELLED）',
    done_time    datetime      null comment '完成时间',
    cancel_time  datetime      null comment '取消时间',
    source_type  varchar(64)   not null default '' comment '来源类型',
    source_id    bigint        null comment '来源ID',
    remark       varchar(255)  not null default '' comment '备注',
    create_by    varchar(64)   not null default '' comment '创建人',
    create_time  datetime      not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by    varchar(64)   not null default '' comment '更新人',
    update_time  datetime      not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_reminder_user_status_time (user_id, status, remind_time),
    key idx_reminder_user_create_time (user_id, create_time)
) engine = InnoDB comment '站内提醒表'
  collate = utf8mb4_unicode_ci;

--changeset fz:reminder-menu-seed labels:system context:all
--comment: 站内提醒菜单 seed
insert into sys_menu (id, parent_id, menu_key, title, path, icon, permission_key, sort_order, visible, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000004016, 0, 'reminders', '提醒中心', '/reminders', 'Bell', null, 80, 1, 1, '系统默认菜单', 'liquibase', now(), 'liquibase', now())
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
