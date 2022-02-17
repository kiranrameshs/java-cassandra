package com.kiran.readbetter.dataloader;

import com.kiran.readbetter.ReadbetterApplication;
import com.kiran.readbetter.author.Author;
import com.kiran.readbetter.author.AuthorRepository;
import com.kiran.readbetter.book.Book;
import com.kiran.readbetter.book.BookRepository;
import com.kiran.readbetter.connection.DataStaxAstraProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class readBetterDataLoaderApplication {
    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    BookRepository bookRepository;

    @Value("${datadump.location.author}")
    private String authorDataDumpLocation;

    @Value("${datadump.location.work}")
    private String workDataDumpLocation;


    public static void main(String[] args) {
        SpringApplication.run(ReadbetterApplication.class, args);
    }

    public void initAuthor() {
        //get the path of the author data dump
        Path path = Path.of(authorDataDumpLocation);
        //parse each line of the data
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line ->
            {
                String jsonString = line.substring(line.indexOf("{"));
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    //create and initialize the author entity
                    Author author = new Author();
                    author.setName(jsonObject.optString("name"));
                    author.setPersonalName(jsonObject.optString("personal_name"));
                    author.setId(jsonObject.optString("key").replace("/authors/", ""));
                    //persist the data to db
                    authorRepository.save(author);
                    System.out.println("Saved author: " + author.getName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void initWorks() {
        //get the path of the works data dump
        Path path = Path.of(workDataDumpLocation);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        //parse each line of the data
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line ->
            {
                String jsonString = line.substring(line.indexOf("{"));
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    //create and initialize the book entity
                    Book book = new Book();

                    book.setId(jsonObject.optString("key").replaceAll("/works/", ""));

                    book.setName(jsonObject.optString("title"));

                    JSONObject descObj = jsonObject.optJSONObject("description");
                    if (descObj != null) {
                        book.setDescription(descObj.optString("value"));
                    } else {
                        book.setDescription("Sample description");
                    }

                    JSONObject publishDateObj = jsonObject.optJSONObject("created");
                    if (publishDateObj != null) {
                        book.setPublishDate(LocalDate.parse(publishDateObj.optString("value"), dateTimeFormatter));
                    }


                    JSONArray jsonArray = jsonObject.optJSONArray("covers");
                    if (jsonArray != null) {
                        List<String> coverIds = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            coverIds.add(jsonArray.optString(i));
                        }
                        book.setCoverIds(coverIds);
                    }


                    JSONArray authorArray = jsonObject.optJSONArray("authors");
                    if (authorArray != null) {
                        List<String> authorIds = new ArrayList<>();
                        for (int i = 0; i < authorArray.length(); i++) {
                            authorIds.add(authorArray.getJSONObject(i).optJSONObject("author").optString("key").replaceAll("/authors/", ""));
                        }
                        book.setAuthorIds(authorIds);

                        List<String> authorNames = new ArrayList<>();
                        for (String authorId : authorIds) {
                            Optional<Author> optionalAuthor = authorRepository.findById(authorId);
                            if (optionalAuthor.isPresent()) {
                                authorNames.add(optionalAuthor.get().getName());
                            } else {
                                authorNames.add("Unknown author");
                            }
                        }
                        book.setAuthorNames(authorNames);
                    }
                    //persist the data to db
                    bookRepository.save(book);
                    System.out.println("Saved book: " + book.getName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void start() {
        System.out.println("authorDataDumpLocation: " + authorDataDumpLocation);
//        initAuthor();
        System.out.println("Persist author data dump complete");
//        initWorks();
        System.out.println("Persist works data dump complete");
    }

}
