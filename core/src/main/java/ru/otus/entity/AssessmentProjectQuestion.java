package ru.otus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;
import static ru.otus.entity.AssessmentProjectQuestion.ASSESSMENT_PROJECT_QUESTION_GRAPH;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assessment_project_question")
@NamedEntityGraph(
    name = ASSESSMENT_PROJECT_QUESTION_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("project"),
        @NamedAttributeNode("question"),
        @NamedAttributeNode("careerLevel")
    }
)
public class AssessmentProjectQuestion {

    public static final String ASSESSMENT_PROJECT_QUESTION_GRAPH = "assessment-project-question-graph";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private AssessmentProject project;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "career_level_id", nullable = false)
    private CareerLevel careerLevel;
}
