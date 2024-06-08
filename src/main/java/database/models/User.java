package database.models;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;


@SuppressWarnings("unused")
@Entity
@Table(name="Users")
public class User {

    @Id
    @GeneratedValue
    @Column(name="id", nullable=false, unique=true, length=11)
    private Long id;

    @Column(name="username", nullable=false)
    private String username;

    @Column(name="tg_id", nullable=false, unique=true)
    private long tgId;

    @OneToMany(mappedBy="owner", fetch=FetchType.LAZY)
    private List<Group> groups;

    @OneToMany(mappedBy="owner", fetch=FetchType.LAZY)
    private List<Product> products;

    @Column(name="enable_notifications", nullable = false)
    private boolean enableNotifications;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public long getTgId() {
        return tgId;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    public List<Group> getAllGroups() {
        return groups;
    }

    public List<Group> getGroups() {
        Predicate<Group> predicate = group -> !group.isDeleted();
        return groups.stream().filter(predicate).toList();
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

    public void setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }

    public User(String username, long tgId) {
        this.username = username;
        this.tgId = tgId;
        this.enableNotifications = true;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + username + '\'' +
                ", tgId=" + tgId +
                '}';
    }

}