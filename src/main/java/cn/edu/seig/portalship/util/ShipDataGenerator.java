package cn.edu.seig.portalship.util;

import cn.edu.seig.portalship.model.excel.ShipPortData;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 船舶停靠出港数据生成器
 * <p>
 * 使用 EasyExcel 流式分批写入，生成 10 万条模拟数据，不会内存溢出。
 * <p>
 * 运行方式：直接执行 main 方法即可。
 */
public class ShipDataGenerator {

    private static final int TOTAL_ROWS = 100_000;
    private static final int BATCH_SIZE = 2_000;
    private static final String OUTPUT_DIR = "D:/test";
    private static final String OUTPUT_FILE = OUTPUT_DIR + "/ship_10w.xlsx";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 中文船名前缀
    private static final String[] SHIP_PREFIXES = {
            "远航", "长江", "东海", "南海", "渤海", "黄海", "明珠", "鲲鹏",
            "飞龙", "天鲲", "海龙", "星辰", "远洋", "中远", "长航", "振华",
            "安吉", "凤凰", "神舟", "和谐", "复兴", "泰山", "长城", "先锋"
    };

    // 船籍
    private static final String[] NATIONALITIES = {
            "中国", "新加坡", "巴拿马", "韩国", "日本"
    };

    // 港口
    private static final String[] PORTS = {
            "上海港", "宁波舟山港", "深圳港", "青岛港", "广州港"
    };

    // 状态
    private static final String[] STATUSES = {
            "已靠泊", "已离港", "在航", "待泊"
    };

    // 权重：偏向"已靠泊"和"已离港"
    private static final int[] STATUS_WEIGHTS = {40, 35, 15, 10};

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  船舶停靠出港数据生成器");
        System.out.println("  目标行数: " + TOTAL_ROWS);
        System.out.println("  输出文件: " + OUTPUT_FILE);
        System.out.println("========================================");

        long startTime = System.currentTimeMillis();

        // 确保输出目录存在
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("创建输出目录: " + OUTPUT_DIR);
        }

        // EasyExcel 流式分批写入
        try (ExcelWriter writer = EasyExcel.write(OUTPUT_FILE, ShipPortData.class).build()) {
            WriteSheet sheet = EasyExcel.writerSheet("船舶数据").build();

            int generated = 0;
            Random rng = ThreadLocalRandom.current();
            LocalDateTime now = LocalDateTime.now();

            while (generated < TOTAL_ROWS) {
                int batchSize = Math.min(BATCH_SIZE, TOTAL_ROWS - generated);
                List<ShipPortData> batch = new ArrayList<>(batchSize);

                for (int i = 0; i < batchSize; i++) {
                    batch.add(generateOne(generated + i + 1, now, rng));
                }

                writer.write(batch, sheet);
                generated += batchSize;

                // 每 10 批打印一次进度
                if (generated % (BATCH_SIZE * 10) == 0 || generated == TOTAL_ROWS) {
                    double pct = 100.0 * generated / TOTAL_ROWS;
                    double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
                    System.out.printf("  进度: %d / %d (%.1f%%)  耗时: %.1fs%n",
                            generated, TOTAL_ROWS, pct, elapsed);
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        long fileSizeMB = new File(OUTPUT_FILE).length() / (1024 * 1024);

        System.out.println("========================================");
        System.out.println("  生成完成!");
        System.out.println("  总行数: " + TOTAL_ROWS);
        System.out.println("  耗时: " + (elapsed / 1000.0) + " 秒");
        System.out.println("  文件大小: " + fileSizeMB + " MB");
        System.out.println("  路径: " + new File(OUTPUT_FILE).getAbsolutePath());
        System.out.println("========================================");
    }

    /**
     * 生成一条随机船舶数据
     */
    private static ShipPortData generateOne(int index, LocalDateTime base, Random rng) {
        // 船名：随机前缀 + 3位编号
        String shipName = pick(SHIP_PREFIXES, rng) + "号" + String.format("%03d", rng.nextInt(1000));

        // IMO 编号：9位随机数字
        String imoNo = String.valueOf(100_000_000L + rng.nextLong(900_000_000L));

        // 船籍
        String nationality = pick(NATIONALITIES, rng);

        // 停靠时间：近30天内随机时间点
        LocalDateTime berth = base.minusDays(rng.nextInt(30))
                .withHour(rng.nextInt(24))
                .withMinute(rng.nextInt(60))
                .withSecond(rng.nextInt(60));

        // 出港时间：停靠后 2~48 小时
        LocalDateTime depart = berth.plusHours(2 + rng.nextInt(47))
                .plusMinutes(rng.nextInt(60));

        // 港口
        String port = pick(PORTS, rng);

        // 状态（加权随机，更真实）
        String status = pickWeighted(STATUSES, STATUS_WEIGHTS, rng);

        return new ShipPortData(
                (long) index,
                shipName,
                imoNo,
                nationality,
                berth.format(DATE_FMT),
                depart.format(DATE_FMT),
                port,
                status
        );
    }

    private static <T> T pick(T[] arr, Random rng) {
        return arr[rng.nextInt(arr.length)];
    }

    /** 带权重的随机选择 */
    private static String pickWeighted(String[] items, int[] weights, Random rng) {
        int total = 0;
        for (int w : weights) total += w;
        int r = rng.nextInt(total);
        for (int i = 0; i < items.length; i++) {
            r -= weights[i];
            if (r < 0) return items[i];
        }
        return items[0];
    }
}
