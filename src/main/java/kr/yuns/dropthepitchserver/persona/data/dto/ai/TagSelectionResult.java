package kr.yuns.dropthepitchserver.persona.data.dto.ai;

import java.util.List;

//AI가 고른 태그
public record TagSelectionResult(
        List<String> tags
) { }
