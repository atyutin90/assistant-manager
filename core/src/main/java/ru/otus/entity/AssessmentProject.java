package ru.otus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static ru.otus.entity.AssessmentProject.ASSESSMENT_PROJECT_GRAPH;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"owner", "accesses"})
@ToString(exclude = {"owner", "accesses"})
@Entity
@Table(name = "assessment_project")
@NamedEntityGraph(
    name = ASSESSMENT_PROJECT_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("owner"),
        @NamedAttributeNode(value = "accesses", subgraph = "assessment-project-access-manager")
    },
    subgraphs = @NamedSubgraph(
        name = "assessment-project-access-manager",
        attributeNodes = @NamedAttributeNode("manager")
    )
)
public class AssessmentProject {

    public static final String ASSESSMENT_PROJECT_GRAPH = "assessment-project-graph";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @Builder.Default
    @Column(columnDefinition = "boolean default true", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private Set<AssessmentProjectAccess> accesses = new HashSet<>();
}
