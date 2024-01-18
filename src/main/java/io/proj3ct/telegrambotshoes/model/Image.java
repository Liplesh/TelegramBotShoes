package io.proj3ct.telegrambotshoes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "images")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column (name = "imageId")
    private Long imageId;

    @Column (name = "name")
    private String name;

    @Column (name = "originalFileName")
    private String originalFileName;

    @Column (name = "size")
    private Long size;

    @Column (name = "contentType")
    private String contentType;

    @Lob //в бд хранит в формате LongBlob
    private byte[] bytes;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Shoes shoes;
}
