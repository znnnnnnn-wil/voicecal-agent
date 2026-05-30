/*
 Navicat Premium Data Transfer

 Source Server         : Localhost
 Source Server Type    : MySQL
 Source Server Version : 80042
 Source Host           : localhost:3306
 Source Schema         : voicecal

 Target Server Type    : MySQL
 Target Server Version : 80042
 File Encoding         : 65001

 Date: 30/05/2026 17:50:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for calendar_event
-- ----------------------------
DROP TABLE IF EXISTS `calendar_event`;
CREATE TABLE `calendar_event`
(
    `id`                 bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '日程ID',
    `title`              varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '日程标题',
    `description`        varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日程描述',
    `start_time`         datetime(6)                                                   NOT NULL COMMENT '开始时间',
    `end_time`           datetime(6)                                                   NOT NULL COMMENT '结束时间',
    `timezone`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT 'Asia/Shanghai' COMMENT '日程时区',
    `reminder_minutes`   int                                                           NULL DEFAULT NULL COMMENT '提前提醒分钟数',
    `reminder_triggered` tinyint(1)                                                    NOT NULL DEFAULT 0 COMMENT '提醒是否已触发',
    `reminded_at`        datetime(6)                                                   NULL DEFAULT NULL COMMENT '提醒触发时间',
    `location`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日程地点',
    `category`           varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT 'OTHER' COMMENT '日程分类：WORK/STUDY/LIFE/MEETING/INTERVIEW/OTHER',
    `status`             varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT 'ACTIVE' COMMENT '日程状态：ACTIVE/CANCELLED/DELETED',
    `created_at`         datetime(6)                                                   NOT NULL COMMENT '创建时间',
    `updated_at`         datetime(6)                                                   NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_calendar_event_start_time` (`start_time` ASC) USING BTREE,
    INDEX `idx_calendar_event_end_time` (`end_time` ASC) USING BTREE,
    INDEX `idx_calendar_event_status` (`status` ASC) USING BTREE,
    INDEX `idx_calendar_event_category` (`category` ASC) USING BTREE,
    INDEX `idx_calendar_event_created_at` (`created_at` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '日程事件表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for voice_command_log
-- ----------------------------
DROP TABLE IF EXISTS `voice_command_log`;
CREATE TABLE `voice_command_log`
(
    `id`               bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `conversation_id`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '对话ID',
    `raw_text`         varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户原始输入',
    `assistant_reply`  varchar(8000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI回复内容',
    `intent`           varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '识别意图',
    `tool_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '工具名称',
    `tool_args_json`   tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci      NULL COMMENT '工具参数JSON',
    `tool_result_json` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci      NULL COMMENT '工具结果JSON',
    `success`          tinyint(1)                                                     NOT NULL DEFAULT 0 COMMENT '是否执行成功',
    `created_at`       datetime(6)                                                    NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_voice_command_log_created_at` (`created_at` ASC) USING BTREE,
    INDEX `idx_voice_command_log_conversation_id` (`conversation_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '语音命令操作日志表'
  ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
