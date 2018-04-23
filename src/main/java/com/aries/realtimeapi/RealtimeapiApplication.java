package com.aries.realtimeapi;

import com.aries.extension.starter.PluginStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class RealtimeapiApplication extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO: jennifer.extension-1.0.1.jar 파일을 로드하면...
        // PluginStarter 클래스를 import 할 수 있으며, 스프링 인터셉터로 추가한다.
        registry.addInterceptor(new PluginStarter()).addPathPatterns("/plugin/**");
    }

    public static void main(String[] args) {
        SpringApplication.run(RealtimeapiApplication.class, args);
    }
}
