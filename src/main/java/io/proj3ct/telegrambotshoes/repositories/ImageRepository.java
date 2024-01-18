package io.proj3ct.telegrambotshoes.repositories;

import io.proj3ct.telegrambotshoes.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
