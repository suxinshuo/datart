-- ----------------------------
-- Table structure for sql_task
-- ----------------------------
DROP TABLE IF EXISTS `sql_task`;
CREATE TABLE `sql_task`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `source_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `script` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `script_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `priority` int(11) NOT NULL DEFAULT 5,
  `timeout` int(11) NOT NULL DEFAULT 3600,
  `max_size` int(11) NOT NULL DEFAULT 1000,
  `start_time` timestamp(0) NULL DEFAULT NULL,
  `end_time` timestamp(0) NULL DEFAULT NULL,
  `duration` bigint(20) NULL DEFAULT NULL,
  `error_message` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `fail_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `exec_instance_id` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `progress` int(11) NOT NULL DEFAULT 0,
  `org_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `execute_param` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `execute_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'AD_HOC',
  `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `permission` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_org_id`(`org_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sql_task_result
-- ----------------------------
DROP TABLE IF EXISTS `sql_task_result`;
CREATE TABLE `sql_task_result`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `task_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `data` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `row_count` int(11) NULL DEFAULT NULL,
  `column_count` int(11) NULL DEFAULT NULL,
  `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `permission` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sql_task_log
-- ----------------------------
DROP TABLE IF EXISTS `sql_task_log`;
CREATE TABLE `sql_task_log`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `task_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `log_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `log_level` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `log_content` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `permission` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE,
  INDEX `idx_log_time`(`log_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
