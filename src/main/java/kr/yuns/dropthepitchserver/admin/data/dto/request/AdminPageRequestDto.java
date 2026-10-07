package kr.yuns.dropthepitchserver.admin.data.dto.request;

import lombok.Data;

@Data
public class AdminPageRequestDto {
    private static final int MAX_SIZE = 100;

    private int page = 0;
    private int size = 20;
    private String sort;
    private String direction;

    public int getPage() {
        return Math.max(page, 0);
    }

    public int getSize() {
        return Math.min(Math.max(size, 1), MAX_SIZE);
    }

    public boolean isAscending() {
        return "asc".equalsIgnoreCase(direction);
    }
}
