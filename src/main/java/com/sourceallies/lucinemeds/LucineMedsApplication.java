package com.sourceallies.lucinemeds;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;

import com.sourceallies.lucinemeds.loader.IndexBuilder;

@SpringBootApplication()
public class LucineMedsApplication {

	public static void main(String[] args) throws IOException {
		// BuildIndex.main(args);
		SpringApplication.run(LucineMedsApplication.class, args);
	}

}
