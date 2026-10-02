import axios from 'axios';
import crypto from 'crypto';
import { MAL_CLIENT_ID, MAL_CLIENT_SECRET, MAL_REDIRECT_URI } from '../config/env.js';

const MAL_AUTH_URL = 'https://myanimelist.net/v1/oauth2/authorize';
const MAL_TOKEN_URL = 'https://myanimelist.net/v1/oauth2/token';
const MAL_API_URL = 'https://api.myanimelist.net/v2';

/**
 * Generate a PKCE code verifier (43-128 chars)
 */
export function generateCodeVerifier() {
  return crypto.randomBytes(32).toString('base64url');
}

/**
 * For MAL, code_challenge = code_verifier (plain method)
 */
export function generateCodeChallenge(verifier) {
  return verifier;
}

/**
 * Build the MAL OAuth2 authorization URL
 */
export function getAuthorizationUrl(codeChallenge, state) {
  const params = new URLSearchParams({
    response_type: 'code',
    client_id: MAL_CLIENT_ID,
    redirect_uri: MAL_REDIRECT_URI,
    code_challenge: codeChallenge,
    code_challenge_method: 'plain',
    state: state,
  });
  return `${MAL_AUTH_URL}?${params.toString()}`;
}

/**
 * Exchange authorization code for access + refresh tokens
 */
export async function exchangeCodeForTokens(code, codeVerifier) {
  const params = new URLSearchParams({
    client_id: MAL_CLIENT_ID,
    grant_type: 'authorization_code',
    code,
    redirect_uri: MAL_REDIRECT_URI,
    code_verifier: codeVerifier,
  });

  if (MAL_CLIENT_SECRET) {
    params.append('client_secret', MAL_CLIENT_SECRET);
  }

  const response = await axios.post(MAL_TOKEN_URL, params.toString(), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  });

  return response.data;
}

/**
 * Refresh an expired access token
 */
export async function refreshAccessToken(refreshToken) {
  const params = new URLSearchParams({
    client_id: MAL_CLIENT_ID,
    grant_type: 'refresh_token',
    refresh_token: refreshToken,
  });

  if (MAL_CLIENT_SECRET) {
    params.append('client_secret', MAL_CLIENT_SECRET);
  }

  const response = await axios.post(MAL_TOKEN_URL, params.toString(), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  });

  return response.data;
}

/**
 * Get a valid access token, refreshing if expired
 */
export async function getValidToken(user) {
  if (!user.mal || !user.mal.connected) {
    throw new Error('MAL not connected');
  }

  const now = new Date();
  if (user.mal.tokenExpiresAt && now >= user.mal.tokenExpiresAt) {
    try {
      const tokens = await refreshAccessToken(user.mal.refreshToken);
      user.mal.accessToken = tokens.access_token;
      user.mal.refreshToken = tokens.refresh_token;
      user.mal.tokenExpiresAt = new Date(Date.now() + tokens.expires_in * 1000);
      await user.save();
    } catch (error) {
      user.mal.connected = false;
      await user.save();
      throw new Error('Failed to refresh MAL token. Please reconnect.');
    }
  }

  return user.mal.accessToken;
}

/**
 * Get MAL user profile
 */
export async function getMalUserProfile(accessToken) {
  const response = await axios.get(`${MAL_API_URL}/users/@me`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  return response.data;
}

/**
 * Get user's anime list from MAL
 */
export async function getMalAnimeList(accessToken, status = null, limit = 100, offset = 0) {
  const params = new URLSearchParams({
    fields: 'list_status,num_episodes,media_type,status',
    limit: limit.toString(),
    offset: offset.toString(),
    sort: 'list_updated_at',
  });
  if (status) params.append('status', status);

  const response = await axios.get(`${MAL_API_URL}/users/@me/animelist?${params}`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  return response.data;
}

/**
 * Update anime list status on MAL
 */
export async function updateMalAnimeStatus(accessToken, malId, data) {
  const params = new URLSearchParams();

  if (data.status) params.append('status', data.status);
  if (data.num_watched_episodes !== undefined) {
    params.append('num_watched_episodes', data.num_watched_episodes.toString());
  }
  if (data.score !== undefined) params.append('score', data.score.toString());

  const response = await axios.put(
    `${MAL_API_URL}/anime/${malId}/my_list_status`,
    params.toString(),
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  );
  return response.data;
}

/**
 * Delete anime from MAL list
 */
export async function deleteMalAnimeEntry(accessToken, malId) {
  await axios.delete(`${MAL_API_URL}/anime/${malId}/my_list_status`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
}

/**
 * Sync local watch progress to MAL
 */
export async function syncProgressToMal(user, animeData) {
  if (!user.mal?.connected || !user.preferences?.malAutoSync) return null;
  if (!animeData.malId) return null;

  try {
    const accessToken = await getValidToken(user);

    const updateData = {};

    if (animeData.episodeNumber !== undefined) {
      updateData.num_watched_episodes = animeData.episodeNumber;
    }

    if (animeData.status) {
      const statusMap = {
        watching: 'watching',
        completed: 'completed',
        plan_to_watch: 'plan_to_watch',
        on_hold: 'on_hold',
        dropped: 'dropped',
      };
      updateData.status = statusMap[animeData.status] || 'watching';
    }

    if (animeData.completed && animeData.totalEpisodes &&
        animeData.episodeNumber >= animeData.totalEpisodes) {
      updateData.status = 'completed';
    }

    if (animeData.score) {
      updateData.score = animeData.score;
    }

    const result = await updateMalAnimeStatus(accessToken, animeData.malId, updateData);
    return result;
  } catch (error) {
    console.error('MAL sync error:', error.message);
    return null;
  }
}
