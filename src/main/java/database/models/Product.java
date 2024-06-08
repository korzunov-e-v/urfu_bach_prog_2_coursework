package database.models;

import jakarta.persistence.*;

import java.time.Instant;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;


@SuppressWarnings("unused")
@Entity
@Table(name="Products")
public class Product {
    @Id
    @GeneratedValue
    @Column(name="id", nullable=false, unique=true, length=11)
    private Long id;

    @Column(name="name", nullable=false, columnDefinition="TEXT")
    private String name;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "marketplace")
    private MarketplaceEnum marketplace;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="group_id")
    private Group groupId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="owner_id", nullable=false)
    private User owner;

    @Column(name="product_url", columnDefinition="TEXT")
    private String productUrl;

    @Column(name="created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Product(String name, MarketplaceEnum marketplace, Group groupId, User owner, String productUrl) {
        this.name = name;
        this.marketplace = marketplace;
        this.groupId = groupId;
        this.owner = owner;
        this.productUrl = productUrl;
    }

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MarketplaceEnum getMarketplace() {
        return marketplace;
    }

    public Group getGroupId() {
        return groupId;
    }

    public User getOwner() {
        return owner;
    }

    public String getProductUrl() {
        return productUrl;
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

    public enum MarketplaceEnum {
        OZON,
        WILDBERRIES,
        AVITO,
        YANDEXMARKET
    }
}
