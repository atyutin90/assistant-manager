package ru.otus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
import ru.otus.entity.enums.TechnologyLevel;

import static jakarta.persistence.GenerationType.IDENTITY;
import static ru.otus.entity.UserTechnology.USER_TECHNOLOGY_GRAPH;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "user_technology")
@NamedEntityGraph(
    name = USER_TECHNOLOGY_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("technology")
    }
)
public class UserTechnology {

    public static final String USER_TECHNOLOGY_GRAPH = "user-technology-graph";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technology_id")
    private Technology technology;

    private TechnologyLevel level;
}
