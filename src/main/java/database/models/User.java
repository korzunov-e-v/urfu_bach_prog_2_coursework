package database.models;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;


@SuppressWarnings("unused")
@Entity
@Table(name="Users")
public class User {
    @Id
    @GeneratedValue
    @Column(name="id", nullable=false, unique=true, length=11)
    private Long id;

    @Column(name="name", nullable=false)
    private String name;

    @Column(name="tgId", nullable=false, unique=true)
    private long tgId;

    @OneToMany(mappedBy="owner", fetch=FetchType.LAZY)
    private List<Group> groups;

    @Column(name="createdAt", nullable=false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTgId() {
        return tgId;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User(String name, long tgId) {
        this.name = name;
        this.tgId = tgId;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tgId=" + tgId +
                '}';
    }

}