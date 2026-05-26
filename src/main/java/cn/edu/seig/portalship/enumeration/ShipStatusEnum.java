package cn.edu.seig.portalship.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ShipStatusEnum {

    WAITING(0, "待靠泊"),
    IN_PORT(1, "在港"),
    WORKING(2, "作业中"),
    DEPARTED(3, "离港");

    @EnumValue
    private final Integer id;
    private final String status;

    ShipStatusEnum(Integer id, String status) {
        this.id = id;
        this.status = status;
    }
}
