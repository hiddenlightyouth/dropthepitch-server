package kr.yuns.dropthepitchserver.common.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

//@Async를 켠다. 실행 스레드는 spring.threads.virtual.enabled=true 설정에 따라 가상 스레드가 된다.
@Configuration
@EnableAsync
public class AsyncConfiguration { }
