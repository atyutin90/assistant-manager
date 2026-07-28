package ru.otus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.otus.entity.enums.StaffEvaluationStatus;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static ru.otus.entity.enums.StaffEvaluationStatus.DRAFT;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"staffEvaluationUsers", "staffEvaluationQuestions"})
@ToString(exclude = {"staffEvaluationUsers", "staffEvaluationQuestions"})
@Entity
@Table(name = "staff_evaluation")
public class StaffEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate dateFrom;

    @Column(nullable = false)
    private LocalDate dateTo;

    @Builder.Default
    @Column(nullable = false)
    private StaffEvaluationStatus status = DRAFT;

    @Builder.Default
    @OneToMany(mappedBy = "staffEvaluation", cascade = ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<StaffEvaluationUser> staffEvaluationUsers = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "staffEvaluation", cascade = ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<StaffEvaluationQuestion> staffEvaluationQuestions = new HashSet<>();
}
