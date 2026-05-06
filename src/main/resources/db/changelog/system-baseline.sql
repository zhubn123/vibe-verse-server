--liquibase formatted sql

--changeset fz:system-schema labels:system context:all
--comment: 系统底座表结构
create table if not exists sys_user
(
    id               bigint primary key comment '主键ID',
    username         varchar(64)  not null comment '登录用户名',
    password_hash    varchar(255) not null comment '密码哈希',
    nickname         varchar(64)  not null default '' comment '用户昵称',
    email            varchar(128) null comment '邮箱',
    phone            varchar(32)  null comment '手机号',
    status           tinyint      not null default 0 comment '状态（0正常 1停用 2锁定）',
    login_fail_count int          not null default 0 comment '连续登录失败次数',
    lock_until       datetime     null comment '锁定截止时间',
    last_login_time  datetime     null comment '最后登录时间',
    create_by        varchar(64)  not null default '' comment '创建人',
    create_time      datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by        varchar(64)  not null default '' comment '更新人',
    update_time      datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_user_username (username),
    unique key uk_sys_user_email (email)
) engine = InnoDB comment '系统用户表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_role
(
    id          bigint primary key comment '主键ID',
    role_key    varchar(64)  not null comment '角色标识',
    role_name   varchar(64)  not null comment '角色名称',
    status      tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark      varchar(255) not null default '' comment '备注',
    create_by   varchar(64)  not null default '' comment '创建人',
    create_time datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)  not null default '' comment '更新人',
    update_time datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_role_key (role_key)
) engine = InnoDB comment '系统角色表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_user_role
(
    id          bigint primary key comment '主键ID',
    user_id     bigint      not null comment '用户ID',
    role_id     bigint      not null comment '角色ID',
    create_by   varchar(64) not null default '' comment '创建人',
    create_time datetime    not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64) not null default '' comment '更新人',
    update_time datetime    not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_user_role (user_id, role_id),
    key idx_sys_user_role_user_id (user_id),
    key idx_sys_user_role_role_id (role_id)
) engine = InnoDB comment '用户角色关联表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_permission
(
    id          bigint primary key comment '主键ID',
    perm_key    varchar(128) not null comment '权限标识（如 system:user:view）',
    perm_name   varchar(128) not null comment '权限名称',
    module      varchar(64)  not null comment '所属模块',
    action      varchar(64)  not null comment '动作标识（view/manage）',
    status      tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark      varchar(255) not null default '' comment '备注',
    create_by   varchar(64)  not null default '' comment '创建人',
    create_time datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)  not null default '' comment '更新人',
    update_time datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_permission_perm_key (perm_key),
    key idx_sys_permission_module (module)
) engine = InnoDB comment '系统权限表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_role_permission
(
    id            bigint primary key comment '主键ID',
    role_id       bigint      not null comment '角色ID',
    permission_id bigint      not null comment '权限ID',
    create_by     varchar(64) not null default '' comment '创建人',
    create_time   datetime    not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64) not null default '' comment '更新人',
    update_time   datetime    not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_role_permission (role_id, permission_id),
    key idx_sys_role_permission_role_id (role_id),
    key idx_sys_role_permission_permission_id (permission_id)
) engine = InnoDB comment '角色权限关联表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_audit_log
(
    id          bigint primary key comment '主键ID',
    user_id     bigint       null comment '用户ID（匿名事件可空）',
    username    varchar(64)  null comment '用户名（匿名事件可空）',
    event_type  varchar(32)  not null comment '事件类型',
    event_name  varchar(64)  not null comment '事件名称',
    request_uri varchar(255) null comment '请求路径',
    client_ip   varchar(64)  null comment '客户端IP',
    result      tinyint      not null default 0 comment '执行结果（1成功 0失败）',
    message     varchar(255) null comment '结果消息',
    occur_time  datetime     not null default CURRENT_TIMESTAMP comment '事件发生时间',
    create_by   varchar(64)  not null default '' comment '创建人',
    create_time datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)  not null default '' comment '更新人',
    update_time datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_sys_audit_log_user_id (user_id),
    key idx_sys_audit_log_event_type (event_type),
    key idx_sys_audit_log_occur_time (occur_time)
) engine = InnoDB comment '系统安全审计日志表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_dict_type
(
    id          int primary key auto_increment comment '主键ID',
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
    id          int primary key auto_increment comment '主键ID',
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

--changeset fz:system-seed labels:system context:all
--comment: 系统内置角色、管理员、权限与字典 seed
insert into sys_role (id, role_key, role_name, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000000001, 'admin', '系统管理员', 0, '系统内置角色', 'liquibase', now(), 'liquibase', now()),
       (1930000000000000002, 'operator', '操作员', 0, '系统内置角色', 'liquibase', now(), 'liquibase', now()),
       (1930000000000000003, 'viewer', '只读访客', 0, '系统内置角色', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    role_name = values(role_name),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_user (id, username, password_hash, nickname, email, phone, status, login_fail_count, lock_until, last_login_time,
                      create_by, create_time, update_by, update_time)
values (1930000000000000101, 'admin', '$2a$10$Ziw/AnOoKNlnpj3J0.N.SO07DQlU8KhlBx9gtNNgDbqPWHJ/kgErS',
        '管理员', 'admin@example.com', null, 0, 0, null, now(), 'liquibase', now(), 'liquibase', now())
on duplicate key update
    password_hash = values(password_hash),
    nickname = values(nickname),
    email = values(email),
    status = values(status),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_user_role (id, user_id, role_id, create_by, create_time, update_by, update_time)
values (1930000000000000201, 1930000000000000101, 1930000000000000001, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001001, 'system:user:view', '系统用户查看', 'system', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001002, 'system:user:manage', '系统用户管理', 'system', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001003, 'system:role:view', '系统角色查看', 'system', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001004, 'system:role:manage', '系统角色管理', 'system', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001005, 'system:permission:view', '系统权限目录查看', 'system', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001006, 'system:dict:view', '字典查看', 'system', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001007, 'system:dict:manage', '字典管理', 'system', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002001, 1930000000000000001, 1930000000000001001, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002002, 1930000000000000001, 1930000000000001002, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002003, 1930000000000000001, 1930000000000001003, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002004, 1930000000000000001, 1930000000000001004, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002005, 1930000000000000001, 1930000000000001005, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002006, 1930000000000000001, 1930000000000001006, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002007, 1930000000000000001, 1930000000000001007, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002101, 1930000000000000002, 1930000000000001006, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002201, 1930000000000000003, 1930000000000001006, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_type (dict_code, dict_name, module, status, remark, create_by, create_time, update_by, update_time)
values ('common_enable_status', '通用启停状态', 'system', 0, '系统通用启停状态', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    dict_name = values(dict_name),
    module = values(module),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_item (dict_code, item_value, item_label, sort_order, status, remark, create_by, create_time, update_by, update_time)
values ('common_enable_status', '0', '正常', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('common_enable_status', '1', '停用', 2, 0, '', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    item_label = values(item_label),
    sort_order = values(sort_order),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);
