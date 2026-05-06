--liquibase formatted sql

--changeset codex:20260502-1815-system-wms-inventory-manage-permission labels:user-domain context:all
--comment: WMS 库存管理权限码与默认角色映射（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001024, 'wms:inventory:manage', '库存管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002024, 1930000000000000001, 1930000000000001024, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002115, 1930000000000000002, 1930000000000001024, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);
