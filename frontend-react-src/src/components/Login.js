import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginUser } from '../apiService';

function Login({ setIsLoggedIn }) { // 기존 Props 유지
    const [credentials, setCredentials] = useState({ username: '', password: '' });
    const [rememberMe, setRememberMe] = useState(false);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setCredentials(prev => ({ ...prev, [name]: value }));
    };

    const handleCheckboxChange = (e) => {
        setRememberMe(e.target.checked);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null); // 에러 상태 초기화

        try {
            // [수정] apiService.loginUser는 이제 response.data를 직접 반환합니다.
            const data = await loginUser(
                credentials.username,
                credentials.password
                // rememberMe는 JWT 흐름에서 Refresh Token 쿠키로 대체되므로 무시되거나, 백엔드 로직에 따라 처리됨
            );

            // [수정] accessToken이 응답에 포함되어 있다면 로그인 성공으로 간주
            if (data.accessToken) {
                alert('로그인 성공!');

                // UI 상태 유지를 위한 localStorage 사용 (기존 로직 존중)
                // *주의: 실제 토큰은 apiService 변수에 저장되지만, 새로고침 시 UI 상태 복구를 위해 이는 유지합니다.
                localStorage.setItem('isLoggedIn', 'true');

                setIsLoggedIn(true);
                navigate('/');
            }
        } catch (err) {
            console.error(err);
            // 에러 메시지 처리
            if (err.response && err.response.status === 401) {
                setError('로그인 실패: 아이디 또는 비밀번호를 확인하세요.');
            } else {
                setError('로그인 중 오류가 발생했습니다.');
            }
        }
    };

    return (
        <div className="container" style={{ maxWidth: '400px', marginTop: '50px' }}>
            <h2>로그인</h2>
            <form onSubmit={handleSubmit} className="post-form">
                <input
                    type="text"
                    name="username"
                    placeholder="아이디"
                    value={credentials.username}
                    onChange={handleChange}
                    required
                />
                <input
                    type="password"
                    name="password"
                    placeholder="비밀번호"
                    value={credentials.password}
                    onChange={handleChange}
                    required
                />
                <div style={{ margin: '10px 0', textAlign: 'left' }}>
                    <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                        <input
                            type="checkbox"
                            checked={rememberMe}
                            onChange={handleCheckboxChange}
                            style={{ width: 'auto', marginRight: '8px' }}
                        />
                        로그인 상태 유지
                    </label>
                </div>

                {error && <div className="error-msg">{error}</div>}
                <button type="submit">로그인</button>
            </form>
        </div>
    );
}

export default Login;