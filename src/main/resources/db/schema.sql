-- =============================================
-- 智慧港口船舶信息管理系统 - 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS portalship_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE portalship_db;

-- 用户表
DROP TABLE IF EXISTS tb_user;
CREATE TABLE tb_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '加密密码',
    role VARCHAR(20) NOT NULL DEFAULT 'viewer' COMMENT '角色(admin/operator/viewer)',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status INT DEFAULT 0 COMMENT '状态(0-启用,1-禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员（密码123456的MD5）
INSERT INTO tb_user (username, password, role, email, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin', 'admin@portalship.com', 0);

-- 船舶信息表（新架构：完整字段）
DROP TABLE IF EXISTS tb_ship_info;
CREATE TABLE tb_ship_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    ship_name VARCHAR(100) NOT NULL COMMENT '船名',
    nationality VARCHAR(50) COMMENT '船籍',
    imo_no VARCHAR(20) COMMENT 'IMO编号',
    mmsi_no VARCHAR(20) COMMENT 'MMSI编号',
    ship_type VARCHAR(50) COMMENT '船舶类型(集装箱船/散货船/邮轮等)',
    length DECIMAL(8,2) COMMENT '船长(米)',
    width DECIMAL(8,2) COMMENT '船宽(米)',
    draft DECIMAL(8,2) COMMENT '吃水(米)',
    deadweight DECIMAL(10,2) COMMENT '载重吨',
    company VARCHAR(100) COMMENT '所属公司',
    voyage_no VARCHAR(50) COMMENT '航次号',
    cargo_type VARCHAR(100) COMMENT '货物类型',
    cargo_amount DECIMAL(10,2) COMMENT '货量(吨)',
    berth_no VARCHAR(20) COMMENT '停靠泊位',
    arrive_time DATETIME COMMENT '到港时间',
    leave_time DATETIME COMMENT '离港时间',
    status VARCHAR(20) DEFAULT '待靠泊' COMMENT '船舶状态(待靠泊/在港/作业中/离港)',
    create_by BIGINT COMMENT '录入人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ship_name (ship_name),
    INDEX idx_status (status),
    INDEX idx_arrive_time (arrive_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='船舶信息表';

-- 旧版船舶表（兼容保留，用于ShipServiceImpl回退）
DROP TABLE IF EXISTS ships;
CREATE TABLE ships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    ship_name VARCHAR(100) NOT NULL COMMENT '船名',
    cargo_type VARCHAR(100) COMMENT '货物类型',
    cargo_num INT DEFAULT 0 COMMENT '货物数量',
    destination VARCHAR(100) COMMENT '目的地',
    status VARCHAR(20) DEFAULT '待靠泊' COMMENT '状态',
    create_by BIGINT COMMENT '录入人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    INDEX idx_create_by (create_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='船舶表(旧版兼容)';

-- 船舶作业数据表
DROP TABLE IF EXISTS tb_ship_operation;
CREATE TABLE tb_ship_operation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    ship_id BIGINT NOT NULL COMMENT '关联船舶ID',
    quay_crane_no VARCHAR(20) COMMENT '岸桥编号',
    work_type VARCHAR(20) COMMENT '作业类型(装/卸/移箱)',
    start_time DATETIME COMMENT '作业开始时间',
    end_time DATETIME COMMENT '作业结束时间',
    total_containers INT DEFAULT 0 COMMENT '装卸总箱量',
    normal_boxes INT DEFAULT 0 COMMENT '普通箱数量',
    reefer_boxes INT DEFAULT 0 COMMENT '冷藏箱数量',
    danger_boxes INT DEFAULT 0 COMMENT '危险品箱数量',
    work_efficiency DECIMAL(5,2) COMMENT '作业效率(箱/小时)',
    truck_count INT DEFAULT 0 COMMENT '集卡车次',
    create_by BIGINT COMMENT '录入人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    INDEX idx_ship_id (ship_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='船舶作业数据表';

-- 文件信息表
DROP TABLE IF EXISTS tb_file_info;
CREATE TABLE tb_file_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    relate_id BIGINT NOT NULL COMMENT '关联ID(船舶/作业记录)',
    relate_type VARCHAR(20) NOT NULL COMMENT '关联类型(ship/operation)',
    file_name VARCHAR(100) NOT NULL COMMENT '文件名',
    file_path VARCHAR(255) NOT NULL COMMENT '文件存储路径',
    file_type VARCHAR(20) COMMENT '文件类型(image/pdf/excel)',
    file_size BIGINT COMMENT '文件大小(字节)',
    upload_by BIGINT COMMENT '上传人ID',
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_relate (relate_id, relate_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 系统日志表
DROP TABLE IF EXISTS tb_system_log;
CREATE TABLE tb_system_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(100) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 港口数据表
DROP TABLE IF EXISTS tb_port_data;
CREATE TABLE tb_port_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    port_name VARCHAR(100) COMMENT '港口名称',
    total_berths INT DEFAULT 0 COMMENT '总泊位数',
    available_berths INT DEFAULT 0 COMMENT '可用泊位数',
    today_throughput DECIMAL(10,2) COMMENT '今日吞吐量',
    monthly_throughput DECIMAL(12,2) COMMENT '本月吞吐量',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='港口数据表';

-- 天气数据表
DROP TABLE IF EXISTS tb_weather;
CREATE TABLE tb_weather (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    temperature DECIMAL(4,1) COMMENT '温度(℃)',
    wind_direction VARCHAR(20) COMMENT '风向',
    wind_speed DECIMAL(5,2) COMMENT '风速(m/s)',
    wave_height DECIMAL(4,2) COMMENT '浪高(m)',
    visibility DECIMAL(5,2) COMMENT '能见度(km)',
    weather_desc VARCHAR(50) COMMENT '天气描述',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='天气数据表';

-- 作业数据统计表
DROP TABLE IF EXISTS tb_work_data;
CREATE TABLE tb_work_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    work_date DATE COMMENT '作业日期',
    total_ships INT DEFAULT 0 COMMENT '总船舶数',
    completed_ships INT DEFAULT 0 COMMENT '已完成船舶数',
    total_containers INT DEFAULT 0 COMMENT '总箱量',
    efficiency_rate DECIMAL(5,2) COMMENT '作业效率(箱/小时)',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业数据统计表';
