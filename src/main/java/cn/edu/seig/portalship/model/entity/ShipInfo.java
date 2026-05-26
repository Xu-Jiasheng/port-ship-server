package cn.edu.seig.portalship.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ship_info")
public class ShipInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("ship_name")
    private String shipName;

    @TableField("nationality")
    private String nationality;

    @TableField("imo_no")
    private String imoNo;

    @TableField("mmsi_no")
    private String mmsiNo;

    @TableField("ship_type")
    private String shipType;

    @TableField("length")
    private BigDecimal length;

    @TableField("width")
    private BigDecimal width;

    @TableField("draft")
    private BigDecimal draft;

    @TableField("deadweight")
    private BigDecimal deadweight;

    @TableField("company")
    private String company;

    @TableField("voyage_no")
    private String voyageNo;

    @TableField("cargo_type")
    private String cargoType;

    @TableField("cargo_amount")
    private BigDecimal cargoAmount;

    @TableField("berth_no")
    private String berthNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("arrive_time")
    private LocalDateTime arriveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("leave_time")
    private LocalDateTime leaveTime;

    @TableField("status")
    private String status;

    @TableField("create_by")
    private Long createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
