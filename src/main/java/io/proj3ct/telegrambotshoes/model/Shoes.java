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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "shoes")
    private List<Image> images = new ArrayList<>();

    public void addImageToShoes(Image image){
        image.setShoes(this);
        images.add(image);
    }

}
