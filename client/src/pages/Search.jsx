import { useState, useEffect, useCallback } from 'react';
import { motion } from 'framer-motion';
import { Search as SearchIcon, Filter } from 'lucide-react';
import api from '../services/api';
import AnimeGrid from '../components/ui/AnimeGrid';
import GenreChip from '../components/ui/GenreChip';

const GENRES = ['Action', 'Adventure', 'Comedy', 'Drama', 'Fantasy', 'Horror', 'Mecha', 'Music', 'Mystery', 'Psychological', 'Romance', 'Sci-Fi', 'Slice of Life', 'Sports', 'Supernatural', 'Thriller'];

export default function Search() {
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
      const newResults = responseData.media || responseData.results || [];
      const pageInfo = responseData.pageInfo || {};

      setResults(prev => isNewSearch ? newResults : [...prev, ...newResults]);
      setHasMore(pageInfo.hasNextPage || false);
      if (isNewSearch) setPage(2);
      else setPage(p => p + 1);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [query, filters, page]);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchResults(true);
    }, 500);
    return () => clearTimeout(delayDebounceFn);
  }, [query, filters.genres, filters.format, filters.sort, filters.status]);

  const toggleGenre = (genre) => {
    setFilters(prev => {
      const genres = prev.genres.includes(genre)
        ? prev.genres.filter(g => g !== genre)
        : [...prev.genres, genre];
      return { ...prev, genres };
    });
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24"
    >
      <div className="flex flex-col md:flex-row gap-4 mb-8">
        <div className="relative flex-grow">
          <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <SearchIcon className="h-5 w-5 text-gray-400" />
          </div>
          <input
            type="text"
            className="block w-full pl-11 pr-4 py-4 bg-[#1a1a2e] border border-[#2D2D44] rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#8B5CF6] focus:border-transparent transition-all"
            placeholder="Search for anime..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
        <button
          onClick={() => setShowFilters(!showFilters)}
          className={`flex items-center justify-center px-6 py-4 rounded-xl border border-[#2D2D44] transition-all ${showFilters ? 'bg-[#8B5CF6] text-white' : 'bg-[#1a1a2e] text-gray-300 hover:bg-[#252540]'}`}
        >
          <Filter className="mr-2 h-5 w-5" />
          Filters
        </button>
      </div>

      {showFilters && (
        <motion.div 
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          className="mb-8 p-6 bg-[#1a1a2e] rounded-xl border border-[#2D2D44] space-y-6"
        >
          <div>
            <h3 className="text-white font-medium mb-3">Genres</h3>
            <div className="flex flex-wrap gap-2">
              {GENRES.map(genre => (
                <button
                  key={genre}
                  onClick={() => toggleGenre(genre)}
                  className={`px-3 py-1.5 text-sm rounded-full border transition-colors ${
                    filters.genres.includes(genre)
                      ? 'bg-[#8B5CF6] border-[#8B5CF6] text-white'
                      : 'border-[#2D2D44] text-gray-300 hover:border-[#8B5CF6]'
                  }`}
                >
                  {genre}
                </button>
              ))}
            </div>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-gray-400 text-sm mb-2">Format</label>
              <select 
                value={filters.format} 
                onChange={(e) => setFilters({...filters, format: e.target.value})}
                className="w-full bg-[#13131a] border border-[#2D2D44] text-white rounded-lg p-2.5 focus:ring-2 focus:ring-[#8B5CF6] outline-none"
              >
                <option value="">Any Format</option>
                <option value="TV">TV</option>
                <option value="MOVIE">Movie</option>
                <option value="OVA">OVA</option>
                <option value="ONA">ONA</option>
                <option value="SPECIAL">Special</option>
              </select>
            </div>
            <div>
              <label className="block text-gray-400 text-sm mb-2">Status</label>
              <select 
                value={filters.status} 
                onChange={(e) => setFilters({...filters, status: e.target.value})}
                className="w-full bg-[#13131a] border border-[#2D2D44] text-white rounded-lg p-2.5 focus:ring-2 focus:ring-[#8B5CF6] outline-none"
              >
                <option value="">Any Status</option>
                <option value="RELEASING">Airing</option>
                <option value="FINISHED">Finished</option>
                <option value="NOT_YET_RELEASED">Upcoming</option>
              </select>
            </div>
            <div>
              <label className="block text-gray-400 text-sm mb-2">Sort By</label>
              <select 
                value={filters.sort} 
                onChange={(e) => setFilters({...filters, sort: e.target.value})}
                className="w-full bg-[#13131a] border border-[#2D2D44] text-white rounded-lg p-2.5 focus:ring-2 focus:ring-[#8B5CF6] outline-none"
              >
                <option value="TRENDING_DESC">Trending</option>
                <option value="POPULARITY_DESC">Popularity</option>
                <option value="SCORE_DESC">Score</option>
                <option value="START_DATE_DESC">Newest</option>
              </select>
            </div>
          </div>
        </motion.div>
      )}

      {results.length > 0 ? (
        <>
          <AnimeGrid animes={results} loading={loading && page === 1} />
          {hasMore && (
            <div className="mt-8 flex justify-center">
              <button
                onClick={() => fetchResults()}
                disabled={loading}
                className="px-6 py-3 bg-[#252540] hover:bg-[#2D2D44] text-white rounded-full font-medium transition-colors disabled:opacity-50"
              >
                {loading ? 'Loading...' : 'Load More'}
              </button>
            </div>
          )}
        </>
      ) : !loading && (
        <div className="text-center py-20">
          <p className="text-gray-400 text-lg">No results found for your search criteria.</p>
        </div>
      )}
    </motion.div>
  );
}
