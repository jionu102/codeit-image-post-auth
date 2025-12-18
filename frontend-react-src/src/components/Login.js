import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginUser } from '../apiService';

function Login({ setIsLoggedIn }) { // Props로 상태 변경 함수 수신
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
        try {
            // [Step 3-5] API 호출 시 rememberMe 상태 전달
            const response = await loginUser(
                credentials.username,
                credentials.password,
                rememberMe
            );

            if (response.status === 200) {
                alert('로그인 성공!');
                setIsLoggedIn(true); // App.js 상태 업데이트
                navigate('/');
            }
        } catch (err) {
            console.error(err);
            setError('로그인 실패: 아이디 또는 비밀번호를 확인하세요.');
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

                {/* [Step 3-6] 로그인 유지 체크박스 UI 추가 */}
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