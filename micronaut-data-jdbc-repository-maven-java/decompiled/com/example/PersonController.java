package com.example;

import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.scheduling.annotation.ExecuteOn;
import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Consumer;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@ExecuteOn("io")
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
      List<Person> listPerson = this.personRepository.findAll(pageable).getContent();

      for(Person p : listPerson) {
         System.out.println(p.getAddresses());
         System.out.println(p.getPhones());
      }

      return listPerson;
   }

   @Get("/listAddressesAndPhones")
   public List<Person> listWithRelationships(@Valid Pageable pageable) {
      List<Person> listPerson = this.personRepository.list();

      for(Person p : listPerson) {
         System.out.println(p.getAddresses());
         System.out.println(p.getPhones());
      }

      return listPerson;
   }

   @Get("/listGenres")
   public List<Genre> list() {
      return this.genreDao.listGenres();
   }

   @Post
   public HttpResponse<Person> save(@Body("name") @NotBlank String name, Timestamp bornTimestamp) {
      Person person = this.personRepository.save(name, bornTimestamp);
      return HttpResponse.created(person).headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(this.location(person.getId()))));
   }

   @Delete("/{id}")
   @Status(HttpStatus.NO_CONTENT)
   public void delete(Long id) {
      this.personRepository.deleteById(id);
   }

   protected URI location(int id) {
      return URI.create("/genres/" + id);
   }

   protected URI location(Person person) {
      return this.location(person.getId());
   }
}
