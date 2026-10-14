import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

const Auth = {
  login: (username, password) => api.post('/auth/login', { username, password }),
  register: (username, password) => api.post('/auth/register', { username, password }),
  getMe: () => api.get('/auth/me'),
  updateProfile: (data) => api.put('/auth/profile', data),
};

const Anime = {
  getHomePage: () => api.get('/anime/home'),
  getTrending: () => api.get('/anime/trending'),
  getPopular: () => api.get('/anime/popular'),
  getRecentEpisodes: (params) => api.get('/anime/recent-episodes', { params }),
  searchAnime: (params) => api.get('/anime/search', { params }),
  getAnimeDetail: (id) => api.get(`/anime/${id}`),
  getEpisodes: (id) => api.get(`/anime/${id}/episodes`),
};

const Stream = {
  getStreamingSources: (dataIds) => api.get(`/stream/${dataIds}/sources`),
  getStreamingServers: (episodeId) => api.get(`/stream/${episodeId}/servers`),
  getEmbedUrl: (linkId) => api.get(`/stream/embed/${linkId}`),
};

const User = {
  getWatchlist: (status) => api.get('/user/watchlist', { params: { status } }),
  addToWatchlist: (data) => api.post('/user/watchlist', data),
  updateWatchlistEntry: (id, data) => api.put(`/user/watchlist/${id}`, data),
  removeFromWatchlist: (id) => api.delete(`/user/watchlist/${id}`),
  getHistory: () => api.get('/user/history'),
  saveProgress: (data) => api.post('/user/progress', data),
  getProgress: (animeId) => api.get(`/user/progress/${animeId}`),
};

const MAL = {
  getAuthUrl: () => api.get('/mal/auth'),
  getStatus: () => api.get('/mal/status'),
  disconnect: () => api.post('/mal/disconnect'),
  getList: (status) => api.get('/mal/list', { params: { status } }),
  syncAnime: (animeId, data) => api.post(`/mal/sync/${animeId}`, data),
  syncAll: () => api.post('/mal/sync-all'),
};

const Sync = {
  getGoogleAuthUrl: () => api.get('/sync/google/auth-url'),
  getGoogleStatus: () => api.get('/sync/google/status'),
  disconnectGoogle: () => api.post('/sync/google/disconnect'),
  downloadGoogleSync: () => api.get('/sync/google/download'),
  uploadGoogleSync: (syncData) => api.post('/sync/google/upload', { syncData }),
};

export default { Auth, Anime, Stream, User, MAL, Sync };
