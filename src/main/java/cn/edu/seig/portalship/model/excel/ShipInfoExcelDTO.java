package cn.edu.seig.portalship.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 船舶数据 Excel 导入/导出 DTO
 * <p>
 * 字段顺序与 Excel 列顺序一致，index 从 0 开始。
 * 导出时自动写入表头，导入时按表头名或索引匹配。
 */
@Data
public class ShipInfoExcelDTO {

    @ExcelProperty(value = "船名", index = 0)
    @ColumnWidth(20)
    private String shipName;

    @ExcelProperty(value = "船籍", index = 1)
    @ColumnWidth(14)
    private String nationality;

    @ExcelProperty(value = "IMO编号", index = 2)
    @ColumnWidth(16)
    private String imoNo;

    @ExcelProperty(value = "MMSI编号", index = 3)
    @ColumnWidth(16)
    private String mmsiNo;

    @ExcelProperty(value = "船舶类型", index = 4)
    @ColumnWidth(16)
    private String shipType;

    @ExcelProperty(value = "船长(米)", index = 5)
    @ColumnWidth(12)
    private BigDecimal length;

    @ExcelProperty(value = "船宽(米)", index = 6)
    @ColumnWidth(12)
    private BigDecimal width;

    @ExcelProperty(value = "吃水(米)", index = 7)
    @ColumnWidth(12)
    private BigDecimal draft;

    @ExcelProperty(value = "载重吨", index = 8)
    @ColumnWidth(14)
    private BigDecimal deadweight;

    @ExcelProperty(value = "所属公司", index = 9)
    @ColumnWidth(22)
    private String company;

    @ExcelProperty(value = "航次号", index = 10)
    @ColumnWidth(14)
    private String voyageNo;

    @ExcelProperty(value = "货物类型", index = 11)
    @ColumnWidth(16)
    private String cargoType;

    @ExcelProperty(value = "货量(吨)", index = 12)
    @ColumnWidth(14)
    private BigDecimal cargoAmount;

    @ExcelProperty(value = "停靠泊位", index = 13)
    @ColumnWidth(14)
    private String berthNo;

    @ExcelProperty(value = "到港时间", index = 14)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private String arriveTime;

    @ExcelProperty(value = "离港时间", index = 15)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private String leaveTime;

    @ExcelProperty(value = "船舶状态", index = 16)
    @ColumnWidth(14)
    private String status;
}
