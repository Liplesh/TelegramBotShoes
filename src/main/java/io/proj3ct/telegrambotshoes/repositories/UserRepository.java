package io.proj3ct.telegrambotshoes.repositories;

import io.proj3ct.telegrambotshoes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> { //Long - тип ID телеграмма
}
