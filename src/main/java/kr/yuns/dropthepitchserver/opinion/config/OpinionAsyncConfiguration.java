package kr.yuns.dropthepitchserver.opinion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class OpinionAsyncConfiguration {
    public static final String OPINION_COLLECTION_EXECUTOR = "opinionCollectionExecutor";

    private static final int OPINION_CONCURRENCY = 10;

    @Bean(OPINION_COLLECTION_EXECUTOR)
    public Executor opinionCollectionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(OPINION_CONCURRENCY);
        executor.setMaxPoolSize(OPINION_CONCURRENCY);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setThreadNamePrefix("opinion-ai-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }
}
