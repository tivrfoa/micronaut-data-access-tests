package com.example;

import java.net.URI;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@ExecuteOn(TaskExecutors.IO)  
@Controller("/person")  
public class PersonController {

    protected final GenreDao genreDao;
    protected final PersonRepository personRepository;

    public PersonController(GenreDao genreDao, PersonRepository personRepository) {
        this.genreDao = genreDao;
        this.personRepository = personRepository;
    }

    @Get("/list") 
    public List<Person> list(@Valid Pageable pageable) { 
        return personRepository.findAll(pageable).getContent();
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

    @Post
    public HttpResponse<Person> save(@Body("name") @NotBlank String name, Timestamp bornTimestamp,
            Time wakeUpTime) {
        Person person = personRepository.save(name, bornTimestamp, wakeUpTime);

        return HttpResponse
                .created(person)
                .headers(headers -> headers.location(location(person.getId())));
    }

    @Delete("/{id}") 
    @Status(HttpStatus.NO_CONTENT)
    public void delete(Long id) {
        personRepository.deleteById(id);
    }

    protected URI location(int id) {
        return URI.create("/genres/" + id);
    }

    protected URI location(Person person) {
        return location(person.getId());
    }
}