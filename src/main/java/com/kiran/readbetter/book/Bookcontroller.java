package com.kiran.readbetter.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class Bookcontroller {

    @Autowired
    BookRepository bookRepository;

    private final String prefixURL = "https://covers.openlibrary.org/b/id/";

    @GetMapping(value = "/books/{bookId}")
    public String getBook(@PathVariable String bookId, Model model){
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if(optionalBook.isPresent()){
            Book book = optionalBook.get();
            String coveImgUrl;
            if(book.getCoverIds()!=null && book.getCoverIds().size()>0){
                 coveImgUrl = prefixURL + book.getCoverIds().get(0) + "-L.jpg";
            }else{
                 coveImgUrl = "/images/no-image.png";
            }
            System.out.println(book.toString());

            model.addAttribute("coverImage", coveImgUrl);
            model.addAttribute("book", book);
            return "book";
        }

        return "book-not-found";
    }
}
