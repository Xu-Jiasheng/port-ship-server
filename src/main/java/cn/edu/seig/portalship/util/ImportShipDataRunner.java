package cn.edu.seig.portalship.util;

import cn.edu.seig.portalship.PortalShipServerApplication;
import cn.edu.seig.portalship.model.entity.ShipInfo;
import cn.edu.seig.portalship.model.excel.ShipPortData;
import cn.edu.seig.portalship.service.IShipInfoService;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 船舶数据导入工具（独立 main 方法运行）
 * <p>
 * 读取生成好的 Excel 文件（ShipPortData 格式），转换为 ShipInfo 实体，
 * 分批写入数据库 tb_ship_info 表。
 * <p>
 * 运行方式：直接执行 main 方法即可。
 * <p>
 * 字段映射：
 * <pre>
 *   shipNationality → nationality
 *   berthTime       → arriveTime
 *   departTime      → leaveTime
 *   portName        → berthNo
 *   已靠泊→在港  已离港→离港  在航→在航  待泊→待靠泊
 * </pre>
 */
public class ImportShipDataRunner {

    private static final String INPUT_FILE = "D:/test/ship_10w.xlsx";
    private static final int BATCH_SIZE = 5_000;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  船舶数据导入工具");
        System.out.println("  源文件: " + INPUT_FILE);
        System.out.println("========================================");

        File file = new File(INPUT_FILE);
        if (!file.exists()) {
            System.err.println("错误: 文件不存在 — " + file.getAbsolutePath());
            System.err.println("请先运行 ShipDataGenerator 生成测试数据");
            System.exit(1);
        }

        // 启动 Spring 容器（不启动 Web 服务器，仅加载 DB/MyBatis-Plus 配置）
        System.out.println("正在初始化 Spring 容器...");
        SpringApplication app = new SpringApplication(PortalShipServerApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);

        long startTime = System.currentTimeMillis();

        try (ConfigurableApplicationContext ctx = app.run(args)) {
            IShipInfoService shipInfoService = ctx.getBean(IShipInfoService.class);

            // 先统计当前表内数据
            long existingCount = shipInfoService.count();
            System.out.println("导入前船舶数量: " + existingCount);

            // EasyExcel 读取 + 分批写入
            ShipPortDataImportListener listener = new ShipPortDataImportListener(shipInfoService);

            System.out.println("开始读取并导入...");
            EasyExcel.read(INPUT_FILE, ShipPortData.class, listener).sheet().doRead();

            // 输出统计
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long newTotal = shipInfoService.count();
            System.out.println("========================================");
            System.out.println("  导入完成!");
            System.out.println("  总耗时: " + elapsed + " 秒");
            System.out.println("  读取行数: " + listener.totalRows.get());
            System.out.println("  成功插入: " + listener.successRows.get());
            System.out.println("  失败行数: " + listener.failedRows.get());
            System.out.println("  数据库总量: " + newTotal + " (新增 " + (newTotal - existingCount) + ")");
            System.out.println("========================================");
        }
    }

    /**
     * ShipPortData → ShipInfo 导入监听器（分批处理）
     */
    static class ShipPortDataImportListener implements ReadListener<ShipPortData> {

        private final IShipInfoService service;
        private final List<ShipInfo> buffer = new ArrayList<>(BATCH_SIZE);

        final AtomicInteger totalRows = new AtomicInteger(0);
        final AtomicInteger successRows = new AtomicInteger(0);
        final AtomicInteger failedRows = new AtomicInteger(0);

        ShipPortDataImportListener(IShipInfoService service) {
            this.service = service;
        }

        @Override
        public void invoke(ShipPortData dto, AnalysisContext context) {
            totalRows.incrementAndGet();

            ShipInfo entity = convert(dto);

            synchronized (buffer) {
                buffer.add(entity);
                if (buffer.size() >= BATCH_SIZE) {
                    flush();
                }
            }

            // 每 2 万行输出一次进度
            int n = totalRows.get();
            if (n % 20_000 == 0) {
                System.out.printf("  已读取: %d 行, 已写入: %d 行%n", n, successRows.get());
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            synchronized (buffer) {
                if (!buffer.isEmpty()) {
                    flush();
                }
            }
            System.out.printf("  读取完毕: 共 %d 行, 成功 %d, 失败 %d%n",
                    totalRows.get(), successRows.get(), failedRows.get());
        }

        @Override
        public void onException(Exception e, AnalysisContext context) {
            failedRows.incrementAndGet();
            if (failedRows.get() <= 10) {
                System.err.println("  行解析错误: " + e.getMessage());
            }
        }

        private void flush() {
            List<ShipInfo> batch = new ArrayList<>(buffer);
            buffer.clear();
            try {
                service.saveBatch(batch, 1000);
                successRows.addAndGet(batch.size());
            } catch (Exception e) {
                failedRows.addAndGet(batch.size());
                System.err.println("  批量插入失败 (" + batch.size() + " 条): " + e.getMessage());
            }
        }
    }

    /**
     * ShipPortData → ShipInfo 字段映射
     */
    static ShipInfo convert(ShipPortData dto) {
        ShipInfo entity = new ShipInfo();
        entity.setShipName(dto.getShipName());
        entity.setImoNo(dto.getImoNo());
        entity.setNationality(dto.getShipNationality());          // shipNationality → nationality
        entity.setArriveTime(parseDateTime(dto.getBerthTime()));  // berthTime → arriveTime
        entity.setLeaveTime(parseDateTime(dto.getDepartTime()));  // departTime → leaveTime
        entity.setBerthNo(dto.getPortName());                     // portName → berthNo
        entity.setStatus(normalizeStatus(dto.getStatus()));       // 状态归一化
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return entity;
    }

    /**
     * 状态归一化：Excel 中的中文状态 → 数据库存储值
     */
    static String normalizeStatus(String status) {
        if (status == null) return "待靠泊";
        return switch (status) {
            case "已靠泊" -> "在港";
            case "已离港" -> "离港";
            case "在航"   -> "在航";
            case "待泊"   -> "待靠泊";
            default      -> status;
        };
    }

    static LocalDateTime parseDateTime(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return LocalDateTime.parse(str, DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
