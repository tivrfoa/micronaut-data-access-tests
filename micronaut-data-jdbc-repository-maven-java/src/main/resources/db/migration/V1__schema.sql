DROP TABLE IF EXISTS genre;
drop table if exists Phone;
drop table if exists person_address;
drop table if exists address;
drop table if exists person;

CREATE TABLE genre (
    id   BIGINT NOT NULL AUTO_INCREMENT UNIQUE PRIMARY KEY,
   name  VARCHAR(255) NOT NULL UNIQUE
);

create table person (
    id int primary key,
    name varchar(30),
    born_timestamp timestamp,
    wakeup_time time
);


insert into person values
(1, 'Leandro', current_timestamp, current_time + 3),
(2, 'Guilherme', '1989-07-03 08:09:10', current_time + 1),
(3, 'Marcos', '1985-03-04 05:06:07', current_time + 2);

create table Phone (
    id int primary key,
    number int,
    person_id int,
    foreign key(person_id) references person(id)
);

insert into Phone values
(1, 1111, 1),
(2, 1112, 1),
(3, 2222, 2),
(4, 3333, 3);

create table address (
    id int primary key,
    street varchar(30)
);

insert into address values
(1, 'Halong Bay'),
(2, 'Xie Xie'),
(3, 'Arigato');

create table person_address (
    person_id int,
    address_id int,
    foreign key (person_id) references person(id),
    foreign key (address_id) references address(id)
);
