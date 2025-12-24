import axios from 'axios';

// 1. 토큰 변수 추가
let accessToken = null;

const API_URL = '/api/posts';

const apiClient = axios.create({
    baseURL: API_URL,
});

// 2. 요청 인터셉터: 토큰이 있으면 헤더에 추가
apiClient.interceptors.request.use(
    (config) => {
        if (accessToken) {
            config.headers['Authorization'] = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 3. 응답 인터셉터: 401 발생 시 처리
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            if (accessToken) {
                alert('세션이 만료되었습니다. 다시 로그인해주세요.');
                accessToken = null;
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

// [New] 외부에서 토큰 설정용 함수
export const setAccessToken = (token) => {
    accessToken = token;
};

// --- 기존 API 함수들 (변경 없음) ---

export const getAllPosts = () => apiClient.get('');
export const getPostById = (id) => apiClient.get(`/${id}`);

export const createPost = (postData) => {
    const formData = new FormData();
    const postRequestDto = {
        title: postData.title,
        content: postData.content,
        tags: postData.tags.split(',').map(tag => tag.trim()).filter(tag => tag),
    };
    formData.append('request', new Blob([JSON.stringify(postRequestDto)], { type: "application/json" }));

    if (postData.images) {
        for (let i = 0; i < postData.images.length; i++) {
            formData.append('images', postData.images[i]);
        }
    }
    return apiClient.post('', formData);
};

export const updatePost = (id, postData) => {
    const formData = new FormData();
    const postUpdateDto = {
        title: postData.title,
        content: postData.content,
        tags: postData.tags.split(',').map(tag => tag.trim()).filter(tag => tag),
    };
    formData.append('request', new Blob([JSON.stringify(postUpdateDto)], { type: "application/json" }));

    if (postData.images) {
        for (let i = 0; i < postData.images.length; i++) {
            formData.append('images', postData.images[i]);
        }
    }
    return apiClient.put(`/${id}`, formData);
};

export const deletePost = (id) => apiClient.delete(`/${id}`);

// --- 인증 관련 함수 수정 (JWT 처리) ---

export const loginUser = async (username, password) => {
    const params = new URLSearchParams();
    params.append('username', username);
    params.append('password', password);

    // [Mod] baseURL 오버라이드 및 토큰 저장 로직 추가
    const response = await apiClient.post('/login', params, {
        baseURL: '/api',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });

    if (response.data.accessToken) {
        setAccessToken(response.data.accessToken);
    }
    return response.data;
};

export const logoutUser = async () => {
    try {
        await apiClient.post('/logout', {}, { baseURL: '/api' });
    } finally {
        setAccessToken(null);
    }
};