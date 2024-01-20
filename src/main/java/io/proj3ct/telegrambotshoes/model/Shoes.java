package io.proj3ct.telegrambotshoes.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity(name = "shoes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Shoes {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "shoesId")
    private Long shoesId;

    @Column(name = "name")
    private String name;

    @Column(name = "size")
    private double size;

    @Column(name = "price")
    private Long price;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "reference")
    private String reference;

}
