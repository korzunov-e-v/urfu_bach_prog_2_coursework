package database.models;

import jakarta.persistence.*;


@Entity
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name="name", nullable=false, unique=false, length=255)
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="marketplace_id", nullable = false)
    private Marketplace marketplace;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="groupId_id", nullable = false)
    private Group groupId;

    // TODO: datetime

    // TODO: создать конструктор
}
