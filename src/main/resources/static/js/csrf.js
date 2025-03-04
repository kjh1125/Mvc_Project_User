var csrfToken = getCsrfToken(); // ✅ 쿠키에서 CSRF 토큰 가져오기

$.ajaxSetup({
    beforeSend: function(xhr) {
        if (csrfToken) {
            xhr.setRequestHeader("X-XSRF-TOKEN", csrfToken);
        }
    }
});
function getCsrfToken() {
    var name = "XSRF-TOKEN=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var cookieArray = decodedCookie.split(';');
    for (var i = 0; i < cookieArray.length; i++) {
        var cookie = cookieArray[i].trim();
        if (cookie.indexOf(name) === 0) {
            return cookie.substring(name.length, cookie.length);
        }
    }
    return null;
}

// ✅ 기존 fetch()를 오버라이드하여 자동으로 CSRF 토큰 추가
const originalFetch = window.fetch;

window.fetch = async function(url, options = {}) {
    const csrfToken = getCsrfToken();  // CSRF 토큰 가져오기

    // ✅ 기존 요청 헤더 유지하면서 CSRF 토큰 추가
    const headers = {
        ...options.headers,
        "Content-Type": "application/json",
    };

    // ✅ CSRF 토큰이 있으면 추가
    if (csrfToken) {
        headers["X-XSRF-TOKEN"] = csrfToken;
    }

    // ✅ fetch 요청 실행
    return originalFetch(url, {
        ...options,
        headers: headers
    });
};
