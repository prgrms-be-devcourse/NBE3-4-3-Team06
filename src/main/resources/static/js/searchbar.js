document.addEventListener('DOMContentLoaded', function () {
    let searchForm = document.getElementById('globalSearchForm');
    let searchInput = document.getElementById('globalQuery');

    // ✅ 검색 폼이 제출될 때 검색 페이지로 이동
    searchForm.addEventListener('submit', function (event) {
        event.preventDefault(); // 기본 제출 이벤트 방지
        let query = searchInput.value.trim();

        if (query) {
            window.location.href = `/projects/search?query=${encodeURIComponent(query)}`;
        } else {
            window.location.href = `/projects/search`; // 검색어 없이 전체 목록 조회
        }
    });
});
