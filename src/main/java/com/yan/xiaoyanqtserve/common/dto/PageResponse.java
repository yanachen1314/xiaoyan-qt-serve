package com.yan.xiaoyanqtserve.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PageResponse<T> {
    /**
     * 满足条件的总记录数
     */
    private long total;
    /**
     * 满足条件的总页数
     */
    private long pages;
    /**
     * 当前页码
     */
    private long currentPage;
    /**
     * 每页的记录数
     */
    private long pageSize;
    /**
     * 当前页的记录数据
     */
    private List<T> records;
}