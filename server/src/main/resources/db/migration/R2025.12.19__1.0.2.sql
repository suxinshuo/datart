DROP TABLE IF EXISTS `sql_task`;

DROP TABLE IF EXISTS `sql_task_result`;

DROP TABLE IF EXISTS `sql_task_log`;

-- 修改字段类型
ALTER TABLE `view` MODIFY COLUMN `model` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL;
