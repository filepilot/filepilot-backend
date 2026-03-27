package com.filepilot.vcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class VcsApplication {

	public static void main(String[] args) {
		SpringApplication.run(VcsApplication.class, args);
	}

}
