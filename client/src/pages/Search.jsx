import { useState, useEffect, useCallback } from 'react';
import { motion } from 'framer-motion';
import { Search as SearchIcon, Filter, Film, Tv, Sparkles } from 'lucide-react';
import api from '../services/api';
import AnimeGrid from '../components/ui/AnimeGrid';
import GenreChip from '../components/ui/GenreChip';
import { useMode, MODES } from '../context/ModeContext';

const GENRES = ['Action', 'Adventure', 'Comedy', 'Drama', 'Fantasy', 'Horror', 'Mystery', 'Romance', 'Sci-Fi', 'Thriller', 'Crime', 'Family', 'Animation'];

export default function Search() {
  const { mode, config } = useMode();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(false);
  
  const [filters, setFilters] = useState({
    genres: [],
    format: '',
    sort: 'TRENDING_DESC',
    status: ''
  });

  const [showFilters, setShowFilters] = useState(false);

  const fetchResults = useCallback(async (isNewSearch = false) => {
    try {
      setLoading(true);
      const currentPage = isNewSearch ? 1 : page;
      
      let newResults = [];
      let canLoadMore = false;

      if (mode === MODES.ANIME) {
        const params = {
          page: currentPage,
          perPage: 20,
          sort: filters.sort,
        };
        if (query.trim()) params.search = query;
        if (filters.genres.length > 0) params.genres = filters.genres.join(',');
        if (filters.format) params.format = filters.format;
        if (filters.status) params.status = filters.status;

        const res = await api.Anime.searchAnime(params).catch(() => ({ data: { data: { media: [], pageInfo: { hasNextPage: false } } } }));
        const responseData = res.data?.data || res.data || {};
        newResults = responseData.media || responseData.results || [];
        canLoadMore = responseData.pageInfo?.hasNextPage || false;
      } else {
        // KDrama or Hollywood mode (via TMDB)
        const typeParam = filters.format === 'MOVIE' ? 'movie' : (filters.format === 'TV' ? 'tv' : 'all');
        const res = await api.Media.search({
          mode,
          query: query.trim(),
          type: typeParam,
          page: currentPage,
        }).catch(() => ({ data: { data: { results: [], totalPages: 1 } } }));

        const responseData = res.data?.data || res.data || {};
        newResults = responseData.results || [];
        canLoadMore = currentPage < (responseData.totalPages || 1);
      }

      setResults(prev => isNewSearch ? newResults : [...prev, ...newResults]);
      setHasMore(canLoadMore);
      if (isNewSearch) setPage(2);
      else setPage(p => p + 1);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [query, filters, page, mode]);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchResults(true);
    }, 450);
    return () => clearTimeout(delayDebounceFn);
  }, [query, filters.genres, filters.format, filters.sort, filters.status, mode]);

  const toggleGenre = (genre) => {
    setFilters(prev => {
      const genres = prev.genres.includes(genre)
        ? prev.genres.filter(g => g !== genre)
        : [...prev.genres, genre];
      return { ...prev, genres };
    });
  };

  const placeholderText = mode === MODES.ANIME
    ? 'Search anime titles, romanji, english...'
    : (mode === MODES.KDRAMA ? 'Search Korean dramas, K-movies, romance, thrillers...' : 'Search Hollywood blockbusters, series, movies...');

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 font-inter"
    >
      {/* Search Header with Mode Indicator */}
      <div className="flex flex-col md:flex-row gap-4 mb-8">
        <div className="relative flex-grow">
          <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <SearchIcon className="h-5 w-5 text-gray-400" />
          </div>
          <input
            type="text"
            className="block w-full pl-11 pr-4 py-4 bg-[#1a1a2e] border border-[#2D2D44] rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:border-transparent transition-all shadow-lg"
            style={{ '--tw-ring-color': config.accentColor }}
            placeholder={placeholderText}
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
        <button
          onClick={() => setShowFilters(!showFilters)}
          className={`flex items-center justify-center px-6 py-4 rounded-xl border border-[#2D2D44] transition-all font-semibold ${
            showFilters ? 'text-white' : 'bg-[#1a1a2e] text-gray-300 hover:bg-[#252540]'
          }`}
          style={showFilters ? { backgroundColor: config.accentColor } : {}}
        >
          <Filter className="mr-2 h-5 w-5" />
          Filters
        </button>
      </div>

      {/* Expandable Filters */}
      {showFilters && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          exit={{ opacity: 0, height: 0 }}
          className="bg-[#1a1a2e] border border-[#2D2D44] rounded-2xl p-6 mb-8 overflow-hidden shadow-xl"
        >
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div>
              <label className="block text-sm font-bold text-gray-300 mb-2">Format</label>
              <select
                value={filters.format}
                onChange={(e) => setFilters({ ...filters, format: e.target.value })}
                className="w-full bg-[#13131a] border border-[#2D2D44] text-white rounded-xl p-3 focus:outline-none"
              >
                <option value="">All Formats</option>
                <option value="TV">TV Series</option>
                <option value="MOVIE">Movie</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-300 mb-2">Sort By</label>
              <select
                value={filters.sort}
                onChange={(e) => setFilters({ ...filters, sort: e.target.value })}
                className="w-full bg-[#13131a] border border-[#2D2D44] text-white rounded-xl p-3 focus:outline-none"
              >
                <option value="TRENDING_DESC">Trending</option>
                <option value="POPULARITY_DESC">Most Popular</option>
                <option value="SCORE_DESC">Highest Rated</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-300 mb-2">Category Mode</label>
              <div className="p-3 bg-[#13131a] border border-[#2D2D44] rounded-xl text-xs font-bold text-gray-300 flex items-center justify-between">
                <span>{config.title}</span>
                <span className="px-2 py-0.5 rounded text-[10px] text-white" style={{ backgroundColor: config.accentColor }}>
                  {config.badgeText}
                </span>
              </div>
            </div>
          </div>

          <div>
            <label className="block text-sm font-bold text-gray-300 mb-3">Genres</label>
            <div className="flex flex-wrap gap-2">
              {GENRES.map((genre) => (
                <button
                  key={genre}
                  onClick={() => toggleGenre(genre)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    filters.genres.includes(genre)
                      ? 'text-white shadow-md'
                      : 'bg-[#13131a] text-gray-400 hover:text-white border border-[#26263A]'
                  }`}
                  style={filters.genres.includes(genre) ? { backgroundColor: config.accentColor } : {}}
                >
                  {genre}
                </button>
              ))}
            </div>
          </div>
        </motion.div>
      )}

      {/* Results Grid */}
      <AnimeGrid
        animeList={results}
        loading={loading && results.length === 0}
        hasMore={hasMore}
        onLoadMore={() => fetchResults(false)}
      />
    </motion.div>
  );
}
