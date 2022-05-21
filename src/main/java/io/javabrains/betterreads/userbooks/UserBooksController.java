package io.javabrains.betterreads.userbooks;

import io.javabrains.betterreads.book.Book;
import io.javabrains.betterreads.book.BookRepository;
import io.javabrains.betterreads.user.BooksByUser;
import io.javabrains.betterreads.user.BooksByUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class UserBooksController {

    @Autowired
    UserBooksRepository userBooksRepository;

    @Autowired
    BooksByUserRepository booksByUserRepository;

    @Autowired
    BookRepository bookRepository;

    @PostMapping(value = "/addUserBook")
    public ModelAndView addBookForUser(
            @RequestBody MultiValueMap<String, String> formData,
            @AuthenticationPrincipal OAuth2User principal){

        if(principal == null || principal.getAttribute("login") == null){
            return null;
        }


        String bookId = formData.getFirst("bookId");
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (!optionalBook.isPresent()) {
            return new ModelAndView("redirect:/");
        }
        Book book = optionalBook.get();

        UserBooks userBooks  = new UserBooks();
        UserBooksPrimaryKey key = new UserBooksPrimaryKey();
        String userId = principal.getAttribute("login");
        key.setUserId(userId);
        key.setBookId(bookId);

        userBooks.setKey(key);

        if(StringUtils.hasText(formData.getFirst("startDate"))){
            userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startDate")));
        }
        if(StringUtils.hasText(formData.getFirst("completedDate"))){
            userBooks.setCompletedDate(LocalDate.parse(formData.getFirst("completedDate")));
        }
        userBooks.setReadingStatus(formData.getFirst("readingStatus"));

        int rating = 0;

        if(StringUtils.hasText(formData.getFirst("rating"))){
            rating = Integer.parseInt(formData.getFirst("rating"));
            userBooks.setRating(rating);
        }

        BooksByUser booksByUser = new BooksByUser();
        booksByUser.setId(userId);
        booksByUser.setBookId(bookId);
        booksByUser.setBookName(book.getName());
        booksByUser.setCoverIds(book.getCoverIds());
        booksByUser.setAuthorNames(book.getAuthorNames());
        booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
        booksByUser.setRating(rating);
        booksByUserRepository.save(booksByUser);

        userBooksRepository.save(userBooks);
        return new ModelAndView("redirect:/books/"+bookId);
    }

}
