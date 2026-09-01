package kr.yuns.dropthepitchserver.ai.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.ai.data.enums.AiPurpose;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_usage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class AiUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiPurpose purpose;

    @Column(nullable = false)
    private Integer inputTokens;

    @Column(nullable = false)
    private Integer outputTokens;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;
}
