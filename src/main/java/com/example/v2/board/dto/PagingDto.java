package com.example.board.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;

/**
 * 페이징 정보 DTO.
 *
 * Spring Data 의 Page 객체에서 필요한 페이징 정보를 추출하여
 * Mustache 템플릿에서 쉽게 사용할 수 있는 형태로 변환한다.
 *
 * 기능:
 * - 처음/이전/다음/마지막 페이지 네비게이션
 * - 10개 단위의 페이지 번호 블록 생성
 * - 검색 조건(searchType, keyword)을 페이징 링크에 유지
 */
@Getter
public class PagingDto {

    /** 한 블록에 표시할 페이지 수 */
    private static final int PAGE_SIZE = 10;

    /** 현재 페이지가 첫 번째 페이지인지 여부 */
    private final boolean first;
    /** 현재 페이지가 마지막 페이지인지 여부 */
    private final boolean last;
    /** 이전 페이지 번호 (0-indexed). 첫 페이지면 0을 반환 */
    private final int prevPage;
    /** 다음 페이지 번호 (0-indexed). 마지막이면 현재 페이지를 반환 */
    private final int nextPage;
    /** 마지막 페이지 번호 (0-indexed) */
    private final int lastPage;
    /** 현재 페이지 번호 (0-indexed) */
    private final int currentPage;
    /** 전체 페이지 수 */
    private final int totalPages;
    /** 페이지 번호 목록 (현재 블록에 해당하는 페이지들) */
    private final List<PageNumberItem> pageNumbers;
    /** 검색 유형 (title / writer / titleContent) */
    private final String searchType;
    /** 검색 키워드 */
    private final String keyword;

    /** Page 객체와 검색 조건으로 PagingDto를 생성한다. */
    public PagingDto(Page<?> page, String searchType, String keyword) {
        this.first = page.isFirst();
        this.last = page.isLast();
        this.currentPage = page.getNumber();
        this.totalPages = Math.max(page.getTotalPages(), 1);
        this.searchType = searchType != null ? searchType : "";
        this.keyword = keyword != null ? keyword : "";

        // 이전/다음 페이지 번호 계산 (범위를 벗어나지 않도록 경계 처리)
        this.prevPage = Math.max(0, currentPage - 1);
        this.nextPage = Math.min(totalPages - 1, currentPage + 1);
        this.lastPage = totalPages > 0 ? totalPages - 1 : 0;

        // 현재 블록에 해당하는 페이지 번호 목록 생성
        this.pageNumbers = buildPageNumbers();
    }

    /** 현재 페이지 블록의 페이지 번호 목록을 생성한다. */
    private List<PageNumberItem> buildPageNumbers() {
        List<PageNumberItem> items = new ArrayList<>();
        // 현재 블록의 시작/끝 페이지 계산
        int startPage = (currentPage / PAGE_SIZE) * PAGE_SIZE;
        int endPage = Math.min(startPage + PAGE_SIZE, totalPages);

        for (int i = startPage; i < endPage; i++) {
            // pageIndex: 0-indexed (링크용), displayNumber: 1-indexed (화면 표시용)
            items.add(new PageNumberItem(i, i + 1, i == currentPage));
        }
        return items;
    }

    /**
     * 페이지 번호 아이템 레코드.
     *
     * @param pageIndex     0-indexed 페이지 번호 (URL 파라미터용)
     * @param displayNumber 1-indexed 페이지 번호 (화면 표시용)
     * @param active        현재 페이지 여부 (true 이면 강조 표시)
     */
    public record PageNumberItem(int pageIndex, int displayNumber, boolean active) {
    }
}
