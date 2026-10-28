package bootcamp.hibernate_practical.service;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.entity.Book;
import bootcamp.hibernate_practical.exception.BookNotFoundException;
import bootcamp.hibernate_practical.exception.BookStateException;
import bootcamp.hibernate_practical.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse createBook(CreateBookRequest request) {
        Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getGenre(),
                request.getPublicationYear(),
                true
        );
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public List<BookResponse> getBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title).stream().map(this::mapToResponse).toList();
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        return mapToResponse(book);
    }

    public BookResponse updateBook(Long id, UpdateBookRequest request) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        book.setAuthor(request.getAuthor());
        book.setAvailable(request.isAvailable());
        book.setGenre(request.getGenre());
        book.setPublicationYear(request.getPublicationYear());
        book.setTitle(request.getTitle());
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    public void borrowBook(Long id) throws RuntimeException {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        if (book.isAvailable() && !book.isBorrowedStatus()) {
            book.setBorrowedStatus(true);
            bookRepository.save(book);
        } else {
            throw new BookStateException("Selected book is not available to borrow");
        }
    }

    public void returnBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        if (!book.isBorrowedStatus()) {
            throw new BookStateException("Selected book is not borrowed");
        }
        book.setBorrowedStatus(false);
        bookRepository.save(book);
    }

    public List<BookResponse> findByAuthor(String author) {
        return bookRepository.findByAuthorIgnoreCase(author).stream().map(this::mapToResponse).toList();
    }

    public List<BookResponse> findAvailableBooks(){
        return bookRepository.findByAvailableTrue().stream().map(this::mapToResponse).toList();
    }

    private BookResponse mapToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getPublicationYear(),
                book.isAvailable(),
                book.isBorrowedStatus()
        );
    }
}
