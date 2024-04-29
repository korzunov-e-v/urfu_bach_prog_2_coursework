package database.models;

import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "Groups")
public class Group {
    @Id
    @GeneratedValue
    @Column(name="id", nullable=false, unique=true, length=11)
    private Long id;

    @Column(name="name", nullable=false, unique=false, length=255)
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "groupId", fetch = FetchType.LAZY)
    private List<Product> products;

    // TODO: создать конструктор
}
