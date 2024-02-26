package io.proj3ct.telegrambotshoes.repositories;

import io.proj3ct.telegrambotshoes.model.Shoes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoesRepository extends JpaRepository<Shoes, Long> {

    public List<Shoes> findAllBySizeAndQuantityAfter(double size, int quantity);

    public List<Shoes> findAllByPriceIsBeforeAndQuantityAfter(Long price, int quantity);

    public List<Shoes> findAllByPriceIsAndQuantityAfter(Long price, int quantity);

//    public List<Shoes> f

}
