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
   protected final GenreDao genreDao;
   protected final GenreRepository genreRepository;

   public GenreController(GenreDao genreDao, GenreRepository genreRepository) {
      this.genreDao = genreDao;
      this.genreRepository = genreRepository;
   }

   @Get("/{id}")
   public Optional<Genre> show(Long id) {
      return this.genreRepository.findById(id);
   }

   @Put("/name")
   public HttpResponse update(@Body @Valid GenreUpdateCommand command) {
      this.genreRepository.update(command.getId(), command.getName());
      return HttpResponse.noContent().header("Location", this.location(command.getId()).getPath());
   }

   @Put
   public HttpResponse<Genre> update(@Body @Valid Genre genre) {
      Genre updatedGenre = this.genreRepository.update(genre);
      return HttpResponse.ok(updatedGenre).header("Location", this.location(genre.getId()).getPath());
   }

   @Get("/list")
   public List<Genre> list(@Valid Pageable pageable) {
      return this.genreRepository.findAll(pageable).getContent();
   }

   @Get("/listGenres")
   public List<Genre> list() {
      return this.genreDao.listGenres();
   }

   @Post("/name")
   public HttpResponse<Genre> save(@Body("name") @NotBlank String name) {
      Genre genre = this.genreRepository.save(name, 0.0, "");
      return HttpResponse.created(genre).headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(this.location(genre.getId()))));
   }

   @Post
   public HttpResponse<Genre> save(@Body Genre genre) {
      Genre newGenre = this.genreRepository.save(genre.getName(), genre.getValue(), genre.getCountry());
      return HttpResponse.created(newGenre).headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(this.location(genre.getId()))));
   }

   @Post("/ex")
   public HttpResponse<Genre> saveExceptions(@Body @NotBlank String name) {
      try {
         Genre genre = this.genreRepository.saveWithException(name, 0.0, "");
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
