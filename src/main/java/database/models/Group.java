package database.models;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;


@SuppressWarnings("unused")
@Entity
@Table(name="Groups")
public class Group {
    @Id
    @GeneratedValue
    @Column(name="id", nullable=false, unique=true, length=11)
    private Long id;

    @Column(name="name", nullable=false)
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="owner_id", nullable=false)
    private User owner;

    @OneToMany(mappedBy="groupId", fetch=FetchType.LAZY)
    private List<Product> products;

    @Column(name="createdAt", nullable=false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Group(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    public Group() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public List<Product> getProducts() {
        return products;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", owner=" + owner +
                '}';
    }
}
