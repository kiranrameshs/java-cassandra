package com.kiran.readbetter;

import com.kiran.readbetter.author.AuthorRepository;
import com.kiran.readbetter.connection.DataStaxAstraProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class ReadbetterApplication {

	@Autowired
	AuthorRepository authorRepository;
	private ApplicationContext applicationContext;

	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}



	public static void main(String[] args) {
		SpringApplication.run(ReadbetterApplication.class, args);
	}

	/**
	 * This is necessary to have the Spring Boot app use the Astra secure bundle
	 * to connect to the database mostly move from local db to astra db service
	 */
	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void start(){
		System.out.println("App started");
//		Author author = new Author();
//		author.setId("id");
//		author.setName("name");
//		author.setPersonalName("personalName");
//		authorRepository.save(author);
//		System.out.println("Saved Author entity");


	}


}
