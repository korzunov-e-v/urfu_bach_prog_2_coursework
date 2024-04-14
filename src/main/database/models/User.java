import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue
    @Column(name="id")
    private Long id;

    @Column(name="name", nullable=false, unique=false, length=255)
    private String name;

    @Column(name="tgId", nullable=false, unique=true)
    private double tgId;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Group> groups;

    // TODO: создать конструктор
}