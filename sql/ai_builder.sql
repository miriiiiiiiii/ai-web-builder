-- 创建库
create database if not exists ai_builder;

-- 切换库
use ai_builder;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userEmail    varchar(50)                            not null comment '邮箱',
    nickName     varchar(256)                           not null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    gender       tinyint      default 0                 null comment '性别（男1 女0）',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_nickName (nickName)
    ) comment '用户' collate = utf8mb4_unicode_ci;

INSERT INTO `user` VALUES ('1', 'admin', '2022Eden', '1791863591@qq.com', 'meng', 'https://timibucket.oss-cn-guangzhou.aliyuncs.com/xingqi/96bc19e74a4342ae8d528fba4ef04aa7.jpg', '无', '0', 'admin', '2025-05-16 20:29:51', '2026-03-28 10:58:42', '2026-03-28 10:58:42', '0');
