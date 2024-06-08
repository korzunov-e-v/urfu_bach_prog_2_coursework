package database.models;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import org.hibernate.annotations.SQLDelete;


@SuppressWarnings("unused")
@Entity
@Table(name="Groups")
@SQLDelete(sql = "UPDATE Groups SET is_deleted = true, deleted_at = now() WHERE id = ?")
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

    @OneToMany(mappedBy="groupId", fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    private List<Product> products;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="is_deleted")
    private boolean isDeleted;

    @Column(name="deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Group(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.isDeleted = false;
        this.deletedAt= null;
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

    public List<Product> getAllProducts() {
        return products;
    }

    public List<Product> getProducts() {
        Predicate<Product> predicate = product -> !product.isDeleted();
        return products.stream().filter(predicate).toList();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
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
