
# Testing Different Ways of Accessing the Database with Micronaut

https://guides.micronaut.io/latest/micronaut-data-jdbc-repository-maven-java.html


# Docker and MySQL

```shell
newgrp docker

docker run -it --rm \
        -p 3306:3306 \
        -e MYSQL_DATABASE=db \
        -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
        mysql:8
```
