package ru.otus.entity;

import jakarta.persistence.Column;
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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.otus.entity.enums.AnswerResponse;

import static jakarta.persistence.GenerationType.IDENTITY;
import static ru.otus.entity.StaffEvaluationAnswer.STAFF_EVALUATION_ANSWER_GRAPH;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"question", "staffEvaluationUser"})
@Entity
@NamedEntityGraph(
    name = STAFF_EVALUATION_ANSWER_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("question"),
        @NamedAttributeNode("staffEvaluationUser")
    }
)
@Table(name = "staff_evaluation_answer")
public class StaffEvaluationAnswer {

    public static final String STAFF_EVALUATION_ANSWER_GRAPH = "staff-evaluation-answer-graph";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "staff_evaluation_user_id")
    private StaffEvaluationUser staffEvaluationUser;

    private AnswerResponse response;

    @Column(name = "verified_response")
    private AnswerResponse verifiedResponse;

    @Column(name = "verification_comment", length = 4000)
    private String verificationComment;
}
