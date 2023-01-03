package com.sourceallies.lucinemeds;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication()
public class LucineMedsApplication {

	@Bean
	public Directory dataDirectory() throws IOException {
		Path dataDirectory = Path.of("data");
		return FSDirectory.open(dataDirectory);
	}

	@Bean
	public Analyzer analyzer() {
		return new StandardAnalyzer();
	}

	@Bean
	@Profile("!index")
	public DirectoryReader directoryReader() throws IOException {
		return DirectoryReader.open(dataDirectory());
	}

	@Bean
	@Profile("!index")
	public IndexSearcher indexSearcher() throws IOException {
		return new IndexSearcher(directoryReader());
	}

	public static void main(String[] args) throws IOException {
		// BuildIndex.main(args);
		SpringApplication.run(LucineMedsApplication.class, args);
	}

}
