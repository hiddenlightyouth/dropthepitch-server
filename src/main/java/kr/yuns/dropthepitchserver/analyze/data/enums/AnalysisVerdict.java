package kr.yuns.dropthepitchserver.analyze.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AnalysisVerdict {
    ANALYZABLE("분석 가능", null, false, 0),
    NO_CONTENT("내용 없음", "평가할 내용이 없는 파일입니다.", true, 0),
    NOT_TARGET("분석 대상 아님", "출시 전 자료가 아니어서 분석할 수 없습니다.", true, 1),
    ILLEGAL("법상 불가", "불법 도박·마약·성매매 등 불법 행위를 홍보하는 내용이 있어 분석할 수 없습니다.", true, 1),
    ADULT("성인·선정적", "성인 대상이거나 선정적인 콘텐츠는 분석하지 않습니다.", true, 1),
    MANIPULATION("분석 조작 시도", "분석 결과를 조작하려는 문장이 있어 분석할 수 없습니다. 해당 문장을 지우고 새 작업으로 올려주세요.", true, 1),
    PERSONAL_DATA("개인정보 포함", "주민등록번호·연락처 목록 같은 개인정보가 포함되어 분석할 수 없습니다.", true, 1),
    UNREADABLE("열 수 없는 파일", "파일을 열 수 없습니다. 암호를 풀거나 손상되지 않은 파일로 새 작업을 올려주세요.", true, 0),
    AI_BUSY("요청 한도 초과", "요청이 많아 분석하지 못했습니다. 잠시 후 새 작업으로 다시 시도해주세요.", false, 0),
    AI_ERROR("분석 오류", "분석 중 오류가 발생했습니다. 새 작업으로 다시 시도해주세요.", false, 0);

    private final String label;
    private final String message;
    private final boolean rejected;
    private final int keepCredit;

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
