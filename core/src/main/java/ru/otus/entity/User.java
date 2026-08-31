package ru.otus.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import ru.otus.entity.enums.UserRole;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static ru.otus.entity.User.USER_ALL_GRAPH;
import static ru.otus.entity.User.USER_GRAPH;

@Entity
@Getter
@Setter
@EqualsAndHashCode(exclude = {"roles", "projectRoles", "currentLevel", "responsible"})
@ToString(exclude = {"roles", "projectRoles", "currentLevel", "responsible"})
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
    name = USER_ALL_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("roles"),
        @NamedAttributeNode("projectRoles"),
        @NamedAttributeNode("currentLevel"),
        @NamedAttributeNode("responsible")
    }
)
@NamedEntityGraph(
    name = USER_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("currentLevel"),
        @NamedAttributeNode("responsible")
    }
)
public class User {

    public static final String USER_GRAPH = "user-graph";

    public static final String USER_ALL_GRAPH = "user-all-graph";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @ElementCollection(fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<UserRole> roles;

    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    @JoinTable(
        name = "user_project_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "project_role_id")
    )
    private Set<ProjectRole> projectRoles;

    //Текущий КУ
    @ManyToOne
    @JoinColumn(name = "current_level_id")
    private CareerLevel currentLevel;

    //Должность по ТК
    @Column(name = "labor_code_position")
    private String laborCodePosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_id")
    private User responsible;

    public String getDisplayName() {
        var name = lastName + SPACE + firstName + SPACE + middleName;
        return name.isBlank() ? username : name;
    }
}
