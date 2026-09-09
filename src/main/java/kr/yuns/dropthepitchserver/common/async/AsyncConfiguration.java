package kr.yuns.dropthepitchserver.common.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//@Async를 켠다.
//실행기를 이름으로 지정하는 이유:
//스프링 부트는 Executor 빈이 하나라도 있으면 @ConditionalOnMissingBean(Executor.class)에 걸려
//기본 실행기를 아예 만들지 않는다. 의견 수집용 전용 풀이 생긴 뒤로 이름을 안 붙인 @Async가
//그 풀로 흘러들어가, 분석이 의견 수집과 고정 10개 스레드를 나눠 쓰고 있었다.
//분석·썸네일은 대부분 네트워크를 기다리는 일이라 가상 스레드가 맞다.
@Configuration
@EnableAsync
public class AsyncConfiguration {

    public static final String ANALYSIS_EXECUTOR = "analysisExecutor";

    @Bean(ANALYSIS_EXECUTOR)
    public Executor analysisExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
