package cn.edu.seig.portalship.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 船舶停靠出港数据 Excel 模型（仅用于测试数据生成）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipPortData {

    @ExcelProperty(value = "ID", index = 0)
    @ColumnWidth(10)
    private Long id;

    @ExcelProperty(value = "船名", index = 1)
    @ColumnWidth(18)
    private String shipName;

    @ExcelProperty(value = "IMO编号", index = 2)
    @ColumnWidth(16)
    private String imoNo;

    @ExcelProperty(value = "船籍", index = 3)
    @ColumnWidth(12)
    private String shipNationality;

    @ExcelProperty(value = "停靠时间", index = 4)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(22)
    private String berthTime;

    @ExcelProperty(value = "出港时间", index = 5)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(22)
    private String departTime;

    @ExcelProperty(value = "港口", index = 6)
    @ColumnWidth(16)
    private String portName;

    @ExcelProperty(value = "状态", index = 7)
    @ColumnWidth(12)
    private String status;
}
