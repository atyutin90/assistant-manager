package ru.otus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static ru.otus.entity.StaffEvaluationQuestion.STAFF_EVALUATION_QUESTION_GRAPH;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"question", "staffEvaluation"})
@Entity
@NamedEntityGraph(
    name = STAFF_EVALUATION_QUESTION_GRAPH,
    attributeNodes = {@NamedAttributeNode("staffEvaluation"), @NamedAttributeNode("question")}
)
public class StaffEvaluationQuestion {

    public static final String STAFF_EVALUATION_QUESTION_GRAPH = "staff-evaluation-question-graph";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_evaluation_id")
    private StaffEvaluation staffEvaluation;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    private Integer position;
}
