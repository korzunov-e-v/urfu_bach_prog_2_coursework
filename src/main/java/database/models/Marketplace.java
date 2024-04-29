package database.models;

import jakarta.persistence.*;


@Entity
@Table(name = "Marketplaces")
public class Marketplace {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name="name", nullable=false, unique=false, length=255)
    private String name;

    @Column(name="baseUrl", nullable=false, unique=false, length=255)
    private String baseUrl;

    // TODO: создать конструктор
}
