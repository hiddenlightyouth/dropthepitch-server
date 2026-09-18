package kr.yuns.dropthepitchserver.opinion.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.opinion.data.enums.OpinionDetailType;
import lombok.*;

@Entity
@Table(name = "opinion_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class OpinionDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opinion_id", nullable = false)
    private Opinion opinion;

//    긍정, 부정, 개선 구분
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OpinionDetailType type;

//    상세 의견 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
