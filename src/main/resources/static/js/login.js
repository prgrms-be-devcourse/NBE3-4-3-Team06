document.getElementById('loginForm').onsubmit = async (event) => {
    event.preventDefault(); // 기본 폼 제출 방지

    const name = document.getElementById('name').value;
    const password = document.getElementById('password').value;

    console.log("입력된 값:", { name, password }); // 디버깅용 로그 추가

    try {
        const response = await fetch('/api/users/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name, password }),  // JSON 형식으로 요청 전송
        });

        if (!response.ok) {
            const error = await response.json();
            console.error("로그인 실패:", error);
            alert(`로그인 실패: ${error.error}`);
            return;
        }

        const user = await response.json();
        console.log("로그인 성공!", user);

        alert(`로그인 성공! 환영합니다, ${user.userName}님.`);

        // JWT 저장
        localStorage.setItem("accessToken", user.accessToken);
        localStorage.setItem("refreshToken", user.refreshToken);

        window.location.href = '/';
    } catch (err) {
        console.error("네트워크 오류:", err);
        alert('네트워크 오류가 발생했습니다.');
    }
};