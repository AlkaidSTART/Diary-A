package com.diary.backend.controller;

import com.diary.backend.common.Result;
import com.diary.backend.entity.Book;
import com.diary.backend.entity.BookNote;
import com.diary.backend.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    @GetMapping
    public Result<List<Book>> getBooks(@RequestParam Long userId) {
        return Result.success(bookService.getBooks(userId));
    }

    @GetMapping("/{id}")
    public Result<Book> getBook(@PathVariable String id) {
        return Result.success(bookService.getBookMetadata(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadBook(@PathVariable String id) {
        log.debug("Downloading book with ID: {}", id);
        Book book = bookService.getBook(id);
        if (book == null) {
            log.debug("Book not found for ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        if (book.getFileData() == null) {
            log.debug("Book file data is null for ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        log.debug("Returning book file, size: {} bytes", book.getFileData().length);

        String rawTitle = book.getTitle();
        String safeTitle = (rawTitle == null || rawTitle.isBlank())
                ? "book"
                : rawTitle.replaceAll("[\\\\/\\r\\n]", "_").trim();
        String filename = safeTitle + ".epub";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/epub+zip"))
                .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename)
                .body(book.getFileData());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Void> uploadBook(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam(value = "coverImage", required = false) String coverImage,
            @RequestParam("file") MultipartFile file) {
        if (title == null || title.isBlank()) {
            return Result.error(400, "Book title is required");
        }
        if (file == null || file.isEmpty()) {
            return Result.error(400, "Book file is empty");
        }
        try {
            bookService.saveBook(userId, title.trim(), author != null ? author.trim() : "", file, coverImage);
            return Result.success();
        } catch (IOException e) {
            return Result.error(500, "Failed to upload file: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @PutMapping("/{id}/progress")
    public Result<Void> updateProgress(@PathVariable String id, @RequestParam String progress) {
        bookService.updateProgress(id, progress);
        return Result.success();
    }

    @GetMapping("/{id}/notes")
    public Result<List<BookNote>> getNotes(@PathVariable String id, @RequestParam Long userId) {
        return Result.success(bookService.getNotes(id, userId));
    }

    @PostMapping("/{id}/notes")
    public Result<Void> addNote(@PathVariable String id, @RequestBody BookNote note) {
        note.setBookId(id);
        if (note.getUserId() == null) {
            return Result.error(400, "User ID is required");
        }
        bookService.addNote(note.getUserId(), note);
        return Result.success();
    }

    @DeleteMapping("/notes/{noteId}")
    public Result<Void> deleteNote(@PathVariable String noteId) {
        bookService.deleteNote(noteId);
        return Result.success();
    }
}
