import axios from 'axios';
import NodeCache from 'node-cache';

const TMDB_API_KEY = process.env.TMDB_API_KEY || '4e44d9029b1270a757cddc766a1bcb63';
const BASE_URL = 'https://api.themoviedb.org/3';
const IMAGE_BASE_URL = 'https://image.tmdb.org/t/p/w500';
const BACKDROP_BASE_URL = 'https://image.tmdb.org/t/p/original';

const cache = new NodeCache({ stdTTL: 1800, checkperiod: 300 }); // 30 min cache

const client = axios.create({
  baseURL: BASE_URL,
  params: {
    api_key: TMDB_API_KEY,
  },
  timeout: 10000,
});

const formatMedia = (item, mediaType = null) => {
  const type = mediaType || item.media_type || (item.first_air_date ? 'tv' : 'movie');
  const title = item.title || item.name || item.original_title || item.original_name || 'Untitled';
  const originalTitle = item.original_title || item.original_name || title;
  const releaseDate = item.release_date || item.first_air_date || '';
  const year = releaseDate ? new Date(releaseDate).getFullYear() : null;

  return {
    id: item.id,
    tmdbId: item.id,
    type, // 'movie' or 'tv'
    title: {
      english: title,
      romaji: originalTitle,
      native: originalTitle,
    },
    name: title,
    overview: item.overview || '',
    coverImage: {
      extraLarge: item.poster_path ? `${IMAGE_BASE_URL}${item.poster_path}` : null,
      large: item.poster_path ? `${IMAGE_BASE_URL}${item.poster_path}` : null,
      medium: item.poster_path ? `${IMAGE_BASE_URL}${item.poster_path}` : null,
    },
    bannerImage: item.backdrop_path ? `${BACKDROP_BASE_URL}${item.backdrop_path}` : null,
    averageScore: item.vote_average ? Math.round(item.vote_average * 10) : null,
    voteAverage: item.vote_average || 0,
    voteCount: item.vote_count || 0,
    releaseDate,
    year,
    format: type === 'tv' ? 'TV' : 'MOVIE',
    status: item.status || 'RELEASED',
    genres: item.genre_ids || (item.genres ? item.genres.map(g => g.name) : []),
    originalLanguage: item.original_language || 'en',
    popularity: item.popularity || 0,
  };
};

const getTodayDate = () => new Date().toISOString().split('T')[0];

export const getKDramaHome = async () => {
  const cacheKey = 'home_kdrama_v2';
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  const today = getTodayDate();

  try {
    const [trendingRes, popularMoviesRes, topRatedRes, onTheAirRes] = await Promise.all([
      // 1. Trending Scripted Korean Dramas (exclude reality & talk shows)
      client.get('/discover/tv', {
        params: {
          with_original_language: 'ko',
          sort_by: 'popularity.desc',
          without_genres: '10764,10767,10763',
          with_genres: '18',
          'first_air_date.lte': today,
          'vote_count.gte': 15,
        },
      }),
      // 2. Popular Released Korean Movies
      client.get('/discover/movie', {
        params: {
          with_original_language: 'ko',
          sort_by: 'popularity.desc',
          'primary_release_date.lte': today,
          'vote_count.gte': 10,
        },
      }),
      // 3. Top Rated Korean Dramas of All Time
      client.get('/discover/tv', {
        params: {
          with_original_language: 'ko',
          sort_by: 'vote_average.desc',
          without_genres: '10764,10767,10763',
          with_genres: '18',
          'first_air_date.lte': today,
          'vote_count.gte': 80,
        },
      }),
      // 4. Currently Airing Korean TV Series
      client.get('/tv/on_the_air', {
        params: {
          with_original_language: 'ko',
        },
      }),
    ]);

    const result = {
      trending: (trendingRes.data.results || []).map(item => formatMedia(item, 'tv')),
      popularMovies: (popularMoviesRes.data.results || []).map(item => formatMedia(item, 'movie')),
      topRated: (topRatedRes.data.results || []).map(item => formatMedia(item, 'tv')),
      onTheAir: (onTheAirRes.data.results || [])
        .filter(i => i.original_language === 'ko' && (!i.genre_ids || (!i.genre_ids.includes(10764) && !i.genre_ids.includes(10767))))
        .map(item => formatMedia(item, 'tv')),
    };

    cache.set(cacheKey, result);
    return result;
  } catch (error) {
    console.error('TMDB KDrama Home Error:', error.message);
    throw error;
  }
};

export const getHollywoodHome = async () => {
  const cacheKey = 'home_hollywood_v2';
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  const today = getTodayDate();

  try {
    const [trendingRes, popularMoviesRes, popularTvRes, topRatedMoviesRes] = await Promise.all([
      // 1. Trending Movies & Shows of the week (only released, filter out upcoming)
      client.get('/trending/all/week'),
      // 2. Popular Released Movies
      client.get('/discover/movie', {
        params: {
          sort_by: 'popularity.desc',
          'primary_release_date.lte': today,
          'vote_count.gte': 40,
        },
      }),
      // 3. Popular Scripted TV Series (exclude reality/talk shows)
      client.get('/discover/tv', {
        params: {
          sort_by: 'popularity.desc',
          without_genres: '10764,10767,10763',
          'first_air_date.lte': today,
          'vote_count.gte': 50,
        },
      }),
      // 4. Top Rated Released Movies
      client.get('/movie/top_rated', {
        params: {
          'vote_count.gte': 200,
        },
      }),
    ]);

    // Filter trending to only items that are actually released with established viewership
    const validTrending = (trendingRes.data.results || []).filter(item => {
      const relDate = item.release_date || item.first_air_date;
      if (!relDate) return false;
      const isReleased = relDate <= today;
      const hasVotes = (item.vote_count || 0) >= 100;
      return isReleased && hasVotes;
    });

    const trendingItems = validTrending.length >= 6
      ? validTrending
      : [...(popularMoviesRes.data.results || []).slice(0, 5), ...(popularTvRes.data.results || []).slice(0, 5)];

    const result = {
      trending: trendingItems.map(item => formatMedia(item)),
      popularMovies: (popularMoviesRes.data.results || []).map(item => formatMedia(item, 'movie')),
      popularTv: (popularTvRes.data.results || []).map(item => formatMedia(item, 'tv')),
      topRated: (topRatedMoviesRes.data.results || []).map(item => formatMedia(item, 'movie')),
    };

    cache.set(cacheKey, result);
    return result;
  } catch (error) {
    console.error('TMDB Hollywood Home Error:', error.message);
    throw error;
  }
};

export const searchMedia = async ({ query, mode = 'hollywood', type = 'all', genre = null, year = null, page = 1 }) => {
  const cacheKey = `search_${mode}_${type}_${query}_${genre}_${year}_${page}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  try {
    let endpoint = '/search/multi';
    const params = { page, include_adult: false };

    if (query) {
      params.query = query;
      if (type === 'movie') endpoint = '/search/movie';
      else if (type === 'tv') endpoint = '/search/tv';
    } else {
      // Discover mode when no search query
      endpoint = type === 'movie' ? '/discover/movie' : '/discover/tv';
      params.sort_by = 'popularity.desc';
      if (type !== 'movie') {
        params.without_genres = '10764,10767,10763';
      }
      if (genre) params.with_genres = genre;
      if (year) {
        if (type === 'movie') params.primary_release_year = year;
        else params.first_air_date_year = year;
      }
    }

    if (mode === 'kdrama') {
      params.with_original_language = 'ko';
    }

    const res = await client.get(endpoint, { params });
    let results = (res.data.results || []).filter(item => {
      // Filter out people search results
      if (item.media_type === 'person') return false;
      if (mode === 'kdrama' && query) {
        // When searching in KDrama mode, favor Korean original language or origin country
        return item.original_language === 'ko' || (item.origin_country && item.origin_country.includes('KR'));
      }
      return true;
    });

    const formatted = {
      page: res.data.page || 1,
      totalPages: res.data.total_pages || 1,
      totalResults: res.data.total_results || 0,
      results: results.map(item => formatMedia(item, type !== 'all' ? type : null)),
    };

    cache.set(cacheKey, formatted, 600); // 10 min cache
    return formatted;
  } catch (error) {
    console.error('TMDB Search Media Error:', error.message);
    throw error;
  }
};

export const getMediaDetail = async (type, id) => {
  const cacheKey = `detail_${type}_${id}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  try {
    const res = await client.get(`/${type}/${id}`, {
      params: {
        append_to_response: 'credits,recommendations,videos',
      },
    });

    const data = res.data;
    const formatted = formatMedia(data, type);

    // Format Seasons and Episodes metadata for TV
    if (type === 'tv') {
      formatted.numberOfSeasons = data.number_of_seasons || 1;
      formatted.numberOfEpisodes = data.number_of_episodes || 1;
      formatted.seasons = (data.seasons || []).filter(s => s.season_number > 0).map(s => ({
        id: s.id,
        seasonNumber: s.season_number,
        name: s.name,
        episodeCount: s.episode_count,
        posterPath: s.poster_path ? `${IMAGE_BASE_URL}${s.poster_path}` : null,
        airDate: s.air_date,
      }));
    } else {
      formatted.runtime = data.runtime || 0;
    }

    // Cast members
    formatted.cast = (data.credits?.cast || []).slice(0, 10).map(c => ({
      id: c.id,
      name: c.name,
      character: c.character,
      profilePath: c.profile_path ? `${IMAGE_BASE_URL}${c.profile_path}` : null,
    }));

    // Recommendations
    formatted.recommendations = (data.recommendations?.results || []).slice(0, 12).map(r => formatMedia(r, type));

    cache.set(cacheKey, formatted, 1800);
    return formatted;
  } catch (error) {
    console.error(`TMDB Detail Error (${type}/${id}):`, error.message);
    throw error;
  }
};

export const getTvSeasonEpisodes = async (tvId, seasonNumber = 1) => {
  const cacheKey = `season_${tvId}_${seasonNumber}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  try {
    const res = await client.get(`/tv/${tvId}/season/${seasonNumber}`);
    const data = res.data;

    const episodes = (data.episodes || []).map(ep => ({
      id: ep.id,
      episodeNumber: ep.episode_number,
      seasonNumber: ep.season_number,
      title: ep.name || `Episode ${ep.episode_number}`,
      overview: ep.overview || '',
      stillPath: ep.still_path ? `${IMAGE_BASE_URL}${ep.still_path}` : null,
      airDate: ep.air_date,
      runtime: ep.runtime || 0,
      voteAverage: ep.vote_average || 0,
    }));

    cache.set(cacheKey, episodes, 3600);
    return episodes;
  } catch (error) {
    console.error(`TMDB Season Error (${tvId}/S${seasonNumber}):`, error.message);
    throw error;
  }
};

export default {
  getKDramaHome,
  getHollywoodHome,
  searchMedia,
  getMediaDetail,
  getTvSeasonEpisodes,
};
