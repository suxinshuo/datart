/*
 * Datart
 * <p>
 * Copyright 2021
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package datart.server.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import datart.server.common.ThreadPoolMdcExecutor;
import datart.server.config.interceptor.BasicValidRequestInterceptor;
import datart.server.config.interceptor.LoginInterceptor;
import datart.server.config.interceptor.ReqIDInjectHandlerInterceptor;
import datart.server.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${datart.server.path-prefix}")
    private String pathPrefix;

    @Resource
    private LoginInterceptor loginInterceptor;

    @Resource
    private ReqIDInjectHandlerInterceptor reqIDInjectHandlerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(reqIDInjectHandlerInterceptor).addPathPatterns(getPathPrefix() + "/**");
        registry.addInterceptor(loginInterceptor).addPathPatterns(getPathPrefix() + "/**");
        //i18n locale interceptor
        registry.addInterceptor(new BasicValidRequestInterceptor()).addPathPatterns("/**");
    }

    //Add request url prefix
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(getPathPrefix(), aClass -> aClass.getSuperclass().equals(BaseController.class));
    }

    public String getPathPrefix() {
        return StringUtils.removeEnd(pathPrefix, "/");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.QuoteFieldNames,
                SerializerFeature.WriteEnumUsingToString,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.DisableCircularReferenceDetect);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        converters.add(0, fastConverter);
    }

    @Bean("asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        log.info("start asyncTaskExecutor");

        // 需要注意一下请求 trace id 能不能传递到子线程中, 后续再看看要不要改造
        ThreadPoolTaskExecutor executor = new ThreadPoolMdcExecutor();
        // 配置核心线程数
        executor.setCorePoolSize(10);
        // 设置最大线程数
        executor.setMaxPoolSize(10);
        // 设置队列容量
        executor.setQueueCapacity(500);
        // 设置线程活跃时间(秒)
        executor.setKeepAliveSeconds(300);
        // 配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("Async-Executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 执行初始化
        executor.initialize();

        return executor;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 绑定自定义线程池(处理Callable返回值的核心配置)
        configurer.setTaskExecutor(asyncTaskExecutor());
        // 设置异步请求超时时间(单位：毫秒), 避免请求长时间阻塞
        configurer.setDefaultTimeout(10 * 60 * 1000);
    }

}