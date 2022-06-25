package com.example;

import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Status;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@ExecuteOn(TaskExecutors.IO)  
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
        return genreRepository
                .findById(id); 
    }

    @Put("/name") 
    public HttpResponse update(@Body @Valid GenreUpdateCommand command) { 
        genreRepository.update(command.getId(), command.getName());
        return HttpResponse
                .noContent()
                .header(HttpHeaders.LOCATION, location(command.getId()).getPath()); 
    }

    @Put 
    public HttpResponse<Genre> update(@Body @Valid Genre genre) { 
        Genre updatedGenre = genreRepository.update(genre);
        return HttpResponse
                .ok(updatedGenre)
                .header(HttpHeaders.LOCATION, location(genre.getId()).getPath()); 
    }

    @Get("/list") 
    public List<Genre> list(@Valid Pageable pageable) { 
        return genreRepository.findAll(pageable).getContent();
    }

    /**
     * Using MapResultSet
     * 
     * @return List<Genre>
     */
    @Get("/listGenres") 
    public List<Genre> list() { 
        return genreDao.listGenres();
    }

    @Post("/name")
    public HttpResponse<Genre> save(@Body("name") @NotBlank String name) {
        Genre genre = genreRepository.save(name, 0, "");

        return HttpResponse
                .created(genre)
                .headers(headers -> headers.location(location(genre.getId())));
    }

    @Post
    public HttpResponse<Genre> save(@Body Genre genre) {
        Genre newGenre = genreRepository.save(genre.getName(), genre.getValue(), genre.getCountry());

        return HttpResponse
                .created(newGenre)
                .headers(headers -> headers.location(location(genre.getId())));
    }

    @Post("/ex") 
    public HttpResponse<Genre> saveExceptions(@Body @NotBlank String name) {
        try {
            Genre genre = genreRepository.saveWithException(name, 0, "");
            return HttpResponse
                    .created(genre)
                    .headers(headers -> headers.location(location(genre.getId())));
        } catch(DataAccessException e) {
            return HttpResponse.noContent();
        }
    }

    @Delete("/{id}") 
    @Status(HttpStatus.NO_CONTENT)
    public void delete(Long id) {
        genreRepository.deleteById(id);
    }

    protected URI location(Long id) {
        return URI.create("/genres/" + id);
    }

    protected URI location(Genre genre) {
        return location(genre.getId());
    }
}