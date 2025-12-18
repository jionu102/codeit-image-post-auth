import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import PostList from './components/PostList';
import PostDetail from './components/PostDetail';
import PostForm from './components/PostForm';
import Login from './components/Login';
import { logoutUser } from './apiService';

function App() {

    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await logoutUser();
            setIsLoggedIn(false);
            alert('로그아웃 되었습니다.');
            navigate('/');
        } catch (error) {
            console.error('Logout failed', error);
        }
    };

    return (
        <div className="container">
            <nav>
                <Link to="/"><h1>Image-Post</h1></Link>
                <div style={{ display: 'flex', gap: '10px' }}>
                    {isLoggedIn ? (
                        <button onClick={handleLogout} className="new-post-btn" style={{ backgroundColor: '#dc3545' }}>
                            로그아웃
                        </button>
                    ) : (
                        <Link to="/login" className="new-post-btn" style={{ backgroundColor: '#6c757d' }}>
                            로그인
                        </Link>
                    )}
                    <Link to="/new" className="new-post-btn">새 글 작성</Link>
                </div>
            </nav>
            <main>
                <Routes>
                    <Route path="/" element={<PostList />} />
                    <Route path="/post/:id" element={<PostDetail />} />
                    <Route path="/new" element={<PostForm />} />
                    <Route path="/edit/:id" element={<PostForm />} />
                    <Route path="/login" element={<Login setIsLoggedIn={setIsLoggedIn} />} />
                </Routes>
            </main>
        </div>
    );
}

export default App;