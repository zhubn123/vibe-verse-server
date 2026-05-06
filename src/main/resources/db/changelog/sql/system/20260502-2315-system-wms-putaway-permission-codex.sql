--liquibase formatted sql

--changeset codex:20260502-2315-system-wms-putaway-permission labels:system,wms context:all
--comment: WMS 上架任务权限码、默认角色映射和任务状态字典（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001032, 'wms:putaway-task:view', '上架任务查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001033, 'wms:putaway-task:manage', '上架任务管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002032, 1930000000000000001, 1930000000000001032, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002033, 1930000000000000001, 1930000000000001033, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002132, 1930000000000000002, 1930000000000001032, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002133, 1930000000000000002, 1930000000000001033, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002232, 1930000000000000003, 1930000000000001032, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_type (dict_code, dict_name, module, status, remark, create_by, create_time, update_by, update_time)
values ('wms_putaway_task_status', 'WMS 上架任务状态', 'wms', 0, '入库上架任务状态', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    dict_name = values(dict_name),
    module = values(module),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_item (dict_code, item_value, item_label, sort_order, status, remark, create_by, create_time, update_by, update_time)
values ('wms_putaway_task_status', 'PENDING', '待上架', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_putaway_task_status', 'COMPLETED', '已上架', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_putaway_task_status', 'CANCELLED', '已取消', 3, 0, '', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    item_label = values(item_label),
    sort_order = values(sort_order),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);
