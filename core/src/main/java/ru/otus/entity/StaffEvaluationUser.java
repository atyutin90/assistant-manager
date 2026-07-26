package ru.otus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.otus.entity.enums.StaffEvaluationUserStatus;

import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static ru.otus.entity.StaffEvaluationUser.STAFF_EVALUATION_USER_GRAPH;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.NEW;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"answers", "verifiedBy"})
@ToString(exclude = {"user", "staffEvaluation", "answers", "verifiedBy"})
@Entity
@Table(name = "staff_evaluation_user")
@NamedEntityGraph(
    name = STAFF_EVALUATION_USER_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("staffEvaluation")
    }
)
@NamedEntityGraph(
    name = StaffEvaluationUser.STAFF_EVALUATION_USER_ALL_GRAPH,
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("staffEvaluation"),
        @NamedAttributeNode("answers"),
        @NamedAttributeNode("verifiedBy")
    }
)
public class StaffEvaluationUser {

    public static final String STAFF_EVALUATION_USER_GRAPH = "staff-evaluation-user-graph";

    public static final String STAFF_EVALUATION_USER_ALL_GRAPH = "staff-evaluation-user-all-graph";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_evaluation_id")
    private StaffEvaluation staffEvaluation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "staffEvaluationUser", cascade = ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<StaffEvaluationAnswer> answers = new HashSet<>();

    @Column(length = 4000)
    private String feedbackMessage;

    @Builder.Default
    @Column(nullable = false, length = 32)
    private StaffEvaluationUserStatus status = NEW;

    @ManyToOne
    @JoinColumn(name = "verified_by")
    private User verifiedBy;
}
