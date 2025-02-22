window.onload = function () {
    const authButtons = document.querySelector("#auth-buttons");
    const beneficiaryActions = document.querySelector("#beneficiary-actions");
    const startProjectButton = document.querySelector("#start-project-btn");

    if (!authButtons) {
        console.error("auth-buttons 요소를 찾을 수 없습니다.");
        return;
    }

    const accessToken = localStorage.getItem("accessToken");
    let userRole = null;

    console.log("🔹 토큰 존재 여부:", accessToken ? "있음" : "없음");

    if (accessToken) {
        try {
            const tokenPayload = JSON.parse(atob(accessToken.split(".")[1]));
            const userName = tokenPayload.sub || "알 수 없음";
            userRole = tokenPayload.role || "역할 없음";

            console.log("로그인 된 사용자:", userName, "역할:", userRole);

            const roleTranslation = {
                "ROLE_BENEFICIARY": "수혜자",
                "ROLE_SPONSOR": "후원자",
                "ROLE_ADMIN": "관리자"
            };

            const translatedRole = roleTranslation[userRole] || "알 수 없는 역할";

            // 로그인 상태일 때 사용자 정보 표시
            authButtons.innerHTML = `
                <span class="user-name">${userName} (${translatedRole})님</span>
                <a href="/profile/${userName}" class="custom-btn">내 정보</a>
                ${userRole === "ROLE_ADMIN" ? `<a href="/admin" class="custom-btn primary">관리자</a>` : ""}
                <button id="logout-button" class="custom-btn">로그아웃</button>
            `;

            if (userRole === "ROLE_BENEFICIARY" && beneficiaryActions) {
                beneficiaryActions.innerHTML = `
                    <button class="custom-btn primary" onclick="location.href='/projects/new'">
                        생성하기
                    </button>
                `;
            }

            document.getElementById("logout-button").addEventListener("click", logout);

        } catch (error) {
            console.error("JWT 디코딩 오류:", error);
        }
    }

    if (startProjectButton) {
        startProjectButton.addEventListener("click", function (event) {
            if (userRole === "ROLE_BENEFICIARY") {
                window.location.href = "/projects/new";
            } else {
                event.preventDefault();
                alert("이 기능은 수혜자만 사용할 수 있습니다.");
            }
        });
    }
};

async function logout() {
    console.log("🔹 로그아웃 요청 중...");

    try {
        const response = await fetch("/api/users/logout", {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        });

        if (!response.ok) {
            throw new Error("서버 로그아웃 실패");
        }

        console.log("✅ 서버 로그아웃 완료");

        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        window.location.href = "/api/users/login";
    } catch (error) {
        console.error("❌ 로그아웃 오류:", error);
        alert("로그아웃 중 문제가 발생했습니다.");
    }
}