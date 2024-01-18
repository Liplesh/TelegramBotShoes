package io.proj3ct.telegrambotshoes.repositories;

import io.proj3ct.telegrambotshoes.model.Shoes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoesRepository extends JpaRepository<Shoes, Long> {
}
