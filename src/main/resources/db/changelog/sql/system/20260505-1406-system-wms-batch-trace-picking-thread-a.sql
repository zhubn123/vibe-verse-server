--liquibase formatted sql

--changeset thread-a:20260505-1406-system-wms-batch-trace-picking-thread-a labels:system,wms context:all
--comment: 新增批次追溯查看权限、默认角色映射，并扩展拣货状态与拣货异常类型字典（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001043, 'wms:batch-trace:view', '批次追溯查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002043, 1930000000000000001, 1930000000000001043, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002143, 1930000000000000002, 1930000000000001043, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002243, 1930000000000000003, 1930000000000001043, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_type (dict_code, dict_name, module, status, remark, create_by, create_time, update_by, update_time)
values ('wms_picking_task_status', 'WMS 拣货任务状态', 'wms', 0, '出库拣货任务状态', 'liquibase', now(), 'liquibase', now()),
       ('wms_picking_exception_type', 'WMS 拣货异常类型', 'wms', 0, '拣货异常与部分拣货处理类型', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    dict_name = values(dict_name),
    module = values(module),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_item (dict_code, item_value, item_label, sort_order, status, remark, create_by, create_time, update_by, update_time)
values ('wms_picking_task_status', 'PARTIAL', '部分拣货', 4, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_picking_task_status', 'EXCEPTION', '异常待处理', 5, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_picking_exception_type', 'SHORTAGE', '缺货', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_picking_exception_type', 'DAMAGED', '破损', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_picking_exception_type', 'MISMATCH', '扫码不匹配', 3, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_picking_exception_type', 'OTHER', '其他异常', 4, 0, '', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    item_label = values(item_label),
    sort_order = values(sort_order),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);
