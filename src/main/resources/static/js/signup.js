 document.addEventListener('DOMContentLoaded', () => {
        const nameInput = document.getElementById('name');
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirmPassword');
        const signupForm = document.getElementById('signupForm');

        nameInput.addEventListener('blur', async () => {
            const name = nameInput.value.trim();
            const nameStatus = document.getElementById('nameStatus');
            if (!name) {
                updateStatus(nameStatus, 'ID를 입력하세요.', 'text-danger');
                return;
            }

            try {
                const response = await fetch(`/api/users/check-name?name=${encodeURIComponent(name)}`);
                const isDuplicate = await response.json();

                if (isDuplicate) {
                    updateStatus(nameStatus, '이미 사용 중인 ID입니다.', 'text-danger');
                } else {
                    updateStatus(nameStatus, '사용 가능한 ID입니다.', 'text-success');
                }
            } catch (err) {
                updateStatus(nameStatus, 'ID 중복 확인 중 오류가 발생했습니다.', 'text-danger');
            }
        });

        emailInput.addEventListener('blur', async () => {
            const email = emailInput.value.trim();
            const emailStatus = document.getElementById('emailStatus');
            if (!email) {
                updateStatus(emailStatus, '이메일을 입력하세요.', 'text-danger');
                return;
            }

            try {
                const response = await fetch(`/api/users/check-email?email=${encodeURIComponent(email)}`);
                const isDuplicate = await response.json();

                if (isDuplicate) {
                    updateStatus(emailStatus, '이미 사용 중인 이메일입니다.', 'text-danger');
                } else {
                    updateStatus(emailStatus, '사용 가능한 이메일입니다.', 'text-success');
                }
            } catch (err) {
                updateStatus(emailStatus, '이메일 중복 확인 중 오류가 발생했습니다.', 'text-danger');
            }
        });

        confirmPasswordInput.addEventListener('input', () => {
            const passwordStatus = document.getElementById('passwordStatus');
            if (passwordInput.value.trim() !== confirmPasswordInput.value.trim()) {
                passwordStatus.textContent = '비밀번호가 일치하지 않습니다.';
            } else {
                passwordStatus.textContent = '';
            }
        });

        signupForm.addEventListener('submit', (event) => {
            const passwordStatus = document.getElementById('passwordStatus');
            if (passwordInput.value.trim() !== confirmPasswordInput.value.trim()) {
                event.preventDefault();
                passwordStatus.textContent = '비밀번호가 일치하지 않습니다.';
            } else {
                passwordStatus.textContent = '';
            }
        });

        function updateStatus(element, message, className) {
            element.textContent = message;
            element.className = `status ${className}`;
        }
    });