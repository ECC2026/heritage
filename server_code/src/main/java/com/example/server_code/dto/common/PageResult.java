package com.example.server_code.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> list = new ArrayList<>();
    private long total;
    private int page;
    private int size;
    private boolean hasNext;

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(new ArrayList<>(), 0, page, size, false);
    }
}
