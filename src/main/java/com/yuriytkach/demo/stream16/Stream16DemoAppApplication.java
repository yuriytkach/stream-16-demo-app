package com.yuriytkach.demo.stream16;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.yuriytkach.demo.stream16.email.MailProperties;
import com.yuriytkach.demo.stream16.sftp.SftpProperties;

@SpringBootApplication
@EnableConfigurationProperties({ AppProperties.class, SftpProperties.class, MailProperties.class })
public class Stream16DemoAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(Stream16DemoAppApplication.class, args);
	}

}
