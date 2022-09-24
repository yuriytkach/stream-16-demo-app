package com.yuriytkach.demo.stream16;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestClientConfiguration {

  private final AppProperties appProperties;

  @Bean
  public RestTemplate recordSenderRestTemplate() {
    final var template = new RestTemplate(getRequestFactory());
    template.setUriTemplateHandler(new DefaultUriBuilderFactory(appProperties.getConsumerBasePath()));
    return template;
  }

  private SimpleClientHttpRequestFactory getRequestFactory() {
    final var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(appProperties.getConsumerConnectTimeout());
    factory.setReadTimeout(appProperties.getConsumerReadTimeout());
    return factory;
  }

}
