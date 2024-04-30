package database.models;

import jakarta.persistence.*;


@SuppressWarnings("unused")
@Entity
@Table(name="Marketplaces")
public class Marketplace {
    @Id
    @GeneratedValue
    @Column(name="id", nullable=false, unique=true, length=11)
    private Long id;

    @Column(name="name", nullable=false)
    private String name;

    @Column(name="base_url", nullable=false)
    private String baseUrl;

    public Marketplace(String name, String baseUrl) {
        this.name = name;
        this.baseUrl = baseUrl;
    }

    public Marketplace() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String toString() {
        return "Marketplace{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
