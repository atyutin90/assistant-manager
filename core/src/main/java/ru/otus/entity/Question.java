package ru.otus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

import static ru.otus.entity.Question.QUESTION_GRAPH;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"skill", "projectRole"})
@ToString(exclude = {"skill", "projectRole"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question")
@NamedEntityGraph(
    name = QUESTION_GRAPH,
    attributeNodes = {@NamedAttributeNode("skill"), @NamedAttributeNode("projectRole")}
)
public class Question {

    public static final String QUESTION_GRAPH = "question-graph";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "boolean default true", nullable = false)
    private Boolean enabled;

    @Column(nullable = false, unique = true)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "project_role_id", nullable = false)
    private ProjectRole projectRole;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(nullable = false, length = 500)
    private String areaKnowledge;

    @Column(nullable = false, length = 500)
    private String section;

    @Column(nullable = false, length = 500)
    private String text;
}
