package database.models;

import jakarta.persistence.*;

import java.time.Instant;


@SuppressWarnings("unused")
@Entity
@Table(name="Products")
public class Product {
    @Id
    @GeneratedValue
    @Column(name="id", nullable=false, unique=true, length=11)
    private Long id;

    @Column(name="name", nullable=false)
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="marketplace_id", nullable=false)
    private Marketplace marketplace;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="groupId_id", nullable=false)
    private Group groupId;

    @Column(name="createdAt", nullable=false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Product(String name, Marketplace marketplace, Group groupId) {
        this.name = name;
        this.marketplace = marketplace;
        this.groupId = groupId;
    }

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public Group getGroupId() {
        return groupId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", marketplace=" + marketplace +
                ", groupId=" + groupId +
                '}';
    }
}
