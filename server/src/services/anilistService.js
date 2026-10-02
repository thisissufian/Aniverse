import axios from 'axios';
import NodeCache from 'node-cache';
import { ANILIST_API_URL } from '../config/env.js';

const cache = new NodeCache();

const MEDIA_FRAGMENT = `
fragment MediaFields on Media {
  id
  idMal
  title {
    romaji
    english
    native
  }
  coverImage {
    extraLarge
    large
    color
  }
  bannerImage
  description(asHtml: false)
  format
  episodes
  duration
  status
  season
  seasonYear
  averageScore
  popularity
  genres
  studios(isMain: true) {
    nodes {
      name
    }
  }
  nextAiringEpisode {
    episode
    timeUntilAiring
    airingAt
  }
}
`;

const fetchAnilist = async (query, variables) => {
  const cacheKey = JSON.stringify({ query, variables });
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  try {
    const response = await axios.post(
      ANILIST_API_URL,
      { query, variables },
      { headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' } }
    );
    
    // Cache TTL logic is handled outside for fine-grained control, but default is 5 mins
    cache.set(cacheKey, response.data.data, 300);
    return response.data.data;
  } catch (error) {
    console.error('AniList API Error:', error.response?.data || error.message);
    throw new Error('Failed to fetch data from AniList');
  }
};

export const getTrending = async (page = 1, perPage = 20) => {
  const query = `
    ${MEDIA_FRAGMENT}
    query ($page: Int, $perPage: Int) {
      Page(page: $page, perPage: $perPage) {
        media(sort: TRENDING_DESC, type: ANIME, isAdult: false) {
          ...MediaFields
        }
      }
    }
  `;
  const data = await fetchAnilist(query, { page, perPage });
  // Overwrite cache TTL to 600s
  const cacheKey = JSON.stringify({ query, variables: { page, perPage } });
  cache.set(cacheKey, data, 600);
  return data.Page.media;
};

export const getPopular = async (page = 1, perPage = 20) => {
  const query = `
    ${MEDIA_FRAGMENT}
    query ($page: Int, $perPage: Int) {
      Page(page: $page, perPage: $perPage) {
        media(sort: POPULARITY_DESC, type: ANIME, isAdult: false) {
          ...MediaFields
        }
      }
    }
  `;
  const data = await fetchAnilist(query, { page, perPage });
  const cacheKey = JSON.stringify({ query, variables: { page, perPage } });
  cache.set(cacheKey, data, 600);
  return data.Page.media;
};

export const getTopAiring = async (page = 1, perPage = 20) => {
  const query = `
    ${MEDIA_FRAGMENT}
    query ($page: Int, $perPage: Int) {
      Page(page: $page, perPage: $perPage) {
        media(sort: SCORE_DESC, status: RELEASING, type: ANIME, isAdult: false) {
          ...MediaFields
        }
      }
    }
  `;
  const data = await fetchAnilist(query, { page, perPage });
  const cacheKey = JSON.stringify({ query, variables: { page, perPage } });
  cache.set(cacheKey, data, 600);
  return data.Page.media;
};

export const getRecentlyUpdated = async (page = 1, perPage = 20) => {
  const query = `
    ${MEDIA_FRAGMENT}
    query ($page: Int, $perPage: Int) {
      Page(page: $page, perPage: $perPage) {
        media(sort: UPDATED_AT_DESC, status: RELEASING, type: ANIME, isAdult: false) {
          ...MediaFields
        }
      }
    }
  `;
  const data = await fetchAnilist(query, { page, perPage });
  return data.Page.media;
};

export const searchAnime = async (searchQuery, filters = {}) => {
  const { genres, format, sort, status, season, year, page = 1, perPage = 20 } = filters;
  const query = `
    ${MEDIA_FRAGMENT}
    query ($search: String, $page: Int, $perPage: Int, $genre_in: [String], $format: MediaFormat, $sort: [MediaSort], $status: MediaStatus, $season: MediaSeason, $seasonYear: Int) {
      Page(page: $page, perPage: $perPage) {
        pageInfo {
          total
          currentPage
          lastPage
          hasNextPage
        }
        media(search: $search, genre_in: $genre_in, format: $format, sort: $sort, status: $status, season: $season, seasonYear: $seasonYear, type: ANIME, isAdult: false) {
          ...MediaFields
        }
      }
    }
  `;
  
  const variables = {
    search: searchQuery || undefined,
    page: parseInt(page),
    perPage: parseInt(perPage),
    genre_in: genres ? genres.split(',') : undefined,
    format: format || undefined,
    sort: sort ? [sort] : undefined,
    status: status || undefined,
    season: season || undefined,
    seasonYear: year ? parseInt(year) : undefined
  };

  const data = await fetchAnilist(query, variables);
  return data.Page;
};

export const getAnimeDetail = async (id) => {
  const query = `
    ${MEDIA_FRAGMENT}
    query ($id: Int) {
      Media(id: $id, type: ANIME) {
        ...MediaFields
        characters(sort: ROLE, perPage: 10) {
          edges {
            role
            node {
              id
              name {
                full
              }
              image {
                large
              }
            }
            voiceActors(language: JAPANESE) {
              id
              name {
                full
              }
              image {
                large
              }
            }
          }
        }
        recommendations(sort: RATING_DESC, perPage: 10) {
          nodes {
            mediaRecommendation {
              ...MediaFields
            }
          }
        }
        relations {
          edges {
            relationType
            node {
              ...MediaFields
            }
          }
        }
      }
    }
  `;
  const data = await fetchAnilist(query, { id: parseInt(id) });
  const cacheKey = JSON.stringify({ query, variables: { id: parseInt(id) } });
  cache.set(cacheKey, data, 3600); // 1 hour cache
  return data.Media;
};

export const getHomePageData = async () => {
  try {
    const [trending, popular, topAiring] = await Promise.all([
      getTrending(1, 20),
      getPopular(1, 20),
      getTopAiring(1, 20)
    ]);
    return { trending, popular, topAiring };
  } catch (error) {
    throw new Error('Failed to fetch home page data');
  }
};
