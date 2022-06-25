package com.example;

import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Status;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@ExecuteOn("io")
@Controller("/genres")
public class GenreController {
   protected final GenreRepository genreRepository;

   public GenreController(GenreRepository genreRepository) {
      this.genreRepository = genreRepository;
   }

   @Get("/{id}")
   public Optional<Genre> show(Long id) {
      return this.genreRepository.findById(id);
   }

   @Put
   public HttpResponse update(@Body @Valid GenreUpdateCommand command) {
      this.genreRepository.update(command.getId(), command.getName());
      return HttpResponse.noContent().header("Location", this.location(command.getId()).getPath());
   }

   @Get("/list")
   public List<Genre> list(@Valid Pageable pageable) {
      return this.genreRepository.findAll(pageable).getContent();
   }

   @Post
   public HttpResponse<Genre> save(@Body("name") @NotBlank String name) {
      Genre genre = this.genreRepository.save(name);
      return HttpResponse.created(genre).headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(this.location(genre.getId()))));
   }

   @Post("/ex")
   public HttpResponse<Genre> saveExceptions(@Body @NotBlank String name) {
      try {
         Genre genre = this.genreRepository.saveWithException(name);
         return HttpResponse.created(genre).headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(this.location(genre.getId()))));
      } catch (DataAccessException var3) {
         return HttpResponse.noContent();
      }
   }

   @Delete("/{id}")
   @Status(HttpStatus.NO_CONTENT)
   public void delete(Long id) {
      this.genreRepository.deleteById(id);
   }

   protected URI location(Long id) {
      return URI.create("/genres/" + id);
   }

   protected URI location(Genre genre) {
      return this.location(genre.getId());
   }
}
