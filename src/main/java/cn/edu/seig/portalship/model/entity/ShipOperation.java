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
@TableName("tb_ship_operation")
public class ShipOperation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("ship_id")
    private Long shipId;

    @TableField("quay_crane_no")
    private String quayCraneNo;

    @TableField("work_type")
    private String workType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("start_time")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("total_containers")
    private Integer totalContainers;

    @TableField("normal_boxes")
    private Integer normalBoxes;

    @TableField("reefer_boxes")
    private Integer reeferBoxes;

    @TableField("danger_boxes")
    private Integer dangerBoxes;

    @TableField("work_efficiency")
    private BigDecimal workEfficiency;

    @TableField("truck_count")
    private Integer truckCount;

    @TableField("create_by")
    private Long createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
