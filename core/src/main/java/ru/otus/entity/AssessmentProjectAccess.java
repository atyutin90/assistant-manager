package ru.otus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"project", "manager"})
@Entity
@Table(name = "assessment_project_access")
public class AssessmentProjectAccess {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private AssessmentProjectAccessId id;

    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private AssessmentProject project;

    @MapsId("managerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @Builder.Default
    @Column(name = "read_access", nullable = false)
    private Boolean readAccess = false;

    @Builder.Default
    @Column(name = "edit_access", nullable = false)
    private Boolean editAccess = false;
}
