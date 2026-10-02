import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import api from '../services/api';
import AnimeGrid from '../components/ui/AnimeGrid';

const TABS = ['All', 'Watching', 'Plan to Watch', 'Completed', 'On Hold', 'Dropped'];

export default function Watchlist() {
  const [watchlist, setWatchlist] = useState([]);
  const [activeTab, setActiveTab] = useState('All');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchWatchlist = async () => {
      try {
        setLoading(true);
        const res = await api.User.getWatchlist().catch(() => ({ data: { data: [] } })); // mock fallback
        setWatchlist(res.data?.data || res.data || []);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchWatchlist();
  }, []);

  const filteredList = activeTab === 'All' 
    ? watchlist 
    : watchlist.filter(item => item.status === activeTab.toUpperCase().replace(/ /g, '_'));

  // Adapt data structure to match AnimeCard expectations
  const displayList = filteredList.map(item => ({
    ...item.anime,
    episodesWatched: item.episodesWatched,
    totalEpisodes: item.anime.episodes,
  }));

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 min-h-screen"
    >
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <h1 className="text-3xl font-bold text-white">My Watchlist</h1>
        
        <div className="flex overflow-x-auto hide-scrollbar gap-2 pb-2 md:pb-0">
          {TABS.map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`whitespace-nowrap px-4 py-2 rounded-full text-sm font-medium transition-colors ${
                activeTab === tab 
                  ? 'bg-[#8B5CF6] text-white' 
                  : 'bg-[#1a1a2e] text-gray-400 hover:bg-[#252540] hover:text-white border border-[#2D2D44]'
              }`}
            >
              {tab}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <AnimeGrid loading={true} />
      ) : displayList.length > 0 ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 xl:grid-cols-6 gap-4 md:gap-6">
          {displayList.map(anime => (
            <div key={anime.id} className="relative group">
              <Link to={`/anime/${anime.id}`} className="block relative overflow-hidden rounded-lg aspect-[2/3] transition-all duration-300 group-hover:scale-105 group-hover:shadow-[0_0_20px_rgba(139,92,246,0.3)] bg-[#1a1a2e]">
                <img src={anime.coverImage?.large || anime.image} alt={anime.title?.english} className="w-full h-full object-cover" loading="lazy" />
                <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent"></div>
                <div className="absolute bottom-0 left-0 right-0 p-3">
                  <h3 className="text-white font-medium text-sm line-clamp-2 mb-2">{anime.title?.english || anime.title?.romaji}</h3>
                  <div className="w-full bg-gray-700 h-1.5 rounded-full overflow-hidden">
                    <div 
                      className="bg-[#8B5CF6] h-full rounded-full" 
                      style={{ width: `${anime.totalEpisodes ? (anime.episodesWatched / anime.totalEpisodes) * 100 : 0}%` }}
                    ></div>
                  </div>
                  <div className="text-xs text-gray-400 mt-1">
                    {anime.episodesWatched || 0} / {anime.totalEpisodes || '?'}
                  </div>
                </div>
              </Link>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-[#1a1a2e] rounded-xl border border-[#2D2D44]">
          <h3 className="text-xl text-white font-semibold mb-2">No anime found</h3>
          <p className="text-gray-400 mb-6">You don't have any anime in this list yet.</p>
          <Link to="/search" className="inline-block px-6 py-3 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white rounded-lg font-medium transition-colors">
            Browse Anime
          </Link>
        </div>
      )}
    </motion.div>
  );
}
