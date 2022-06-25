
# Testing Different Ways of Accessing the Database with Micronaut

https://guides.micronaut.io/latest/micronaut-data-jdbc-repository-maven-java.html


## Docker and MySQL

```shell
newgrp docker

docker run -it --rm \
        -p 3306:3306 \
        -e MYSQL_DATABASE=db \
        -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
        mysql:8
```

## Creating `micronaut` database

```
$ sudo mysql

mysql> create database micronaut;
Query OK, 1 row affected (0,02 sec)

mysql> grant all on micronaut.* to 'lesco'@'localhost';
Query OK, 0 rows affected (0,02 sec)
```

