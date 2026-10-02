import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Play, Clock } from 'lucide-react';
import api from '../services/api';
import LoadingSpinner from '../components/ui/LoadingSpinner';

export default function History() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        setLoading(true);
        const res = await api.User.getHistory().catch(() => ({ data: { data: [] } }));
        setHistory(res.data?.data || res.data || []);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchHistory();
  }, []);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-4xl mx-auto px-4 sm:px-6 py-24 min-h-screen"
    >
      <div className="flex items-center space-x-3 mb-8">
        <Clock className="w-8 h-8 text-[#8B5CF6]" />
        <h1 className="text-3xl font-bold text-white">Watch History</h1>
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : history.length > 0 ? (
        <div className="space-y-4">
          {history.map((item) => (
            <div key={item.id} className="flex flex-col sm:flex-row bg-[#1a1a2e] rounded-xl overflow-hidden border border-[#2D2D44] hover:border-[#8B5CF6]/50 transition-colors group">
              <div className="sm:w-48 h-32 flex-shrink-0 relative">
                <img 
                  src={item.anime.bannerImage || item.anime.coverImage?.large} 
                  alt={item.anime.title?.english}
                  className="w-full h-full object-cover"
                />
                <div className="absolute inset-0 bg-black/40 group-hover:bg-black/20 transition-colors"></div>
              </div>
              
              <div className="p-4 flex-grow flex flex-col justify-between">
                <div>
                  <h3 className="text-lg font-bold text-white line-clamp-1 hover:text-[#A78BFA] transition-colors">
                    <Link to={`/anime/${item.anime.id}`}>
                      {item.anime.title?.english || item.anime.title?.romaji}
                    </Link>
                  </h3>
                  <p className="text-[#A78BFA] text-sm font-medium mt-1">
                    Episode {item.episode.number}: {item.episode.title}
                  </p>
                  <p className="text-gray-500 text-xs mt-2">
                    Watched on {formatDate(item.updatedAt || item.createdAt)}
                  </p>
                </div>
                
                <div className="mt-4 sm:mt-0 self-start sm:self-end">
                  <Link
                    to={`/watch/${item.anime.id}/${item.episode.id}`}
                    className="flex items-center space-x-2 bg-[#252540] hover:bg-[#8B5CF6] text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
                  >
                    <Play size={16} className="fill-current" />
                    <span>Resume</span>
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-[#1a1a2e] rounded-xl border border-[#2D2D44]">
          <h3 className="text-xl text-white font-semibold mb-2">No history yet</h3>
          <p className="text-gray-400 mb-6">Start watching some anime to build up your history.</p>
          <Link to="/" className="inline-block px-6 py-3 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white rounded-lg font-medium transition-colors">
            Go to Home
          </Link>
        </div>
      )}
    </motion.div>
  );
}
