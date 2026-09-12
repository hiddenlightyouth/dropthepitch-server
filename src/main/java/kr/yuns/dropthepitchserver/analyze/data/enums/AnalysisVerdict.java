package kr.yuns.dropthepitchserver.analyze.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AnalysisVerdict {
    ANALYZABLE("분석 가능", null),
    NO_CONTENT("내용 없음", "평가할 내용이 없는 파일입니다."),
    NOT_TARGET("분석 대상 아님", "출시 전 자료가 아니어서 분석할 수 없습니다."),
    ILLEGAL("법상 불가", "대한민국 법상 허용되지 않는 내용이 포함되어 분석할 수 없습니다.");

    //label은 analysis-schema.json의 verdict 목록과 같아야 한다.
    private final String label;
    private final String message;

    /**
     * 모델이 돌려준 판정 문구를 enum으로 바꿉니다.
     *
     * @param label 판정 문구
     * @return 판정. 목록에 없으면 null
     */
    public static AnalysisVerdict from(String label) {
        return Arrays.stream(values())
                .filter(verdict -> verdict.label.equals(label))
                .findFirst()
                .orElse(null);
    }
}
