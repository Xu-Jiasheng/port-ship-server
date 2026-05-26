package cn.edu.seig.portalship.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步任务线程池配置
 * <p>
 * 导入线程池：核心线程数大、队列容量大，处理高并发批量写入
 * 导出线程池：核心线程数小、队列容量适中，处理分页查询任务
 * <p>
 * 两个线程池互不干扰，避免导入任务占满线程导致导出无法执行。
 */
@Configuration
public class AsyncThreadPoolConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncThreadPoolConfig.class);

    /**
     * Excel 导入专用线程池
     * 核心 4 线程 / 最大 8 线程 / 队列 20000，超出则由调用线程执行（背压）
     */
    @Bean("excelImportExecutor")
    public ThreadPoolExecutor excelImportExecutor() {
        AtomicInteger counter = new AtomicInteger(1);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                4, 8,
                120, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(20000),
                r -> {
                    Thread t = new Thread(r, "excel-import-" + counter.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.allowCoreThreadTimeOut(true);
        log.info("Excel import thread pool initialized: core=4, max=8, queue=20000");
        return executor;
    }

    /**
     * Excel 导出专用线程池
     * 核心 2 线程 / 最大 4 线程 / 队列 5000
     */
    @Bean("excelExportExecutor")
    public ThreadPoolExecutor excelExportExecutor() {
        AtomicInteger counter = new AtomicInteger(1);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2, 4,
                120, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                r -> {
                    Thread t = new Thread(r, "excel-export-" + counter.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.allowCoreThreadTimeOut(true);
        log.info("Excel export thread pool initialized: core=2, max=4, queue=5000");
        return executor;
    }
}
