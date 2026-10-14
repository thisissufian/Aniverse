import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Play, Clock, Plus, Trash2, Cloud, RefreshCw, X } from 'lucide-react';
import toast from 'react-hot-toast';
import WatchlistModal from '../components/ui/WatchlistModal';
import syncManager from '../services/syncManager';
import api from '../services/api';
import { useMode, MODES } from '../context/ModeContext';

export default function History() {
  const { mode, config } = useMode();
  const [filterMode, setFilterMode] = useState(mode);
  const [history, setHistory] = useState([]);
  const [selectedAnimeForWatchlist, setSelectedAnimeForWatchlist] = useState(null);
  const [isSyncing, setIsSyncing] = useState(false);
  const [googleConnected, setGoogleConnected] = useState(false);
  const [googleUser, setGoogleUser] = useState(null);

  useEffect(() => {
    setFilterMode(mode);
  }, [mode]);

  const loadHistory = () => {
    try {
      const localHistory = JSON.parse(localStorage.getItem('aniverse_history') || '[]');
      setHistory(localHistory);
    } catch (e) {
      console.error('Failed to load local history', e);
    }
  };

  useEffect(() => {
    loadHistory();

    // Check Google Drive Status & auto-sync
    api.Sync.getGoogleStatus().then(res => {
      if (res.data?.connected) {
        setGoogleConnected(true);
        setGoogleUser(res.data.user);
        handleSync();
      }
    }).catch(() => {});
  }, []);

  const handleSync = async () => {
    setIsSyncing(true);
    try {
      const res = await syncManager.syncNow();
      if (res.success) {
        loadHistory();
        toast.success('History synced with Google Drive!');
      } else {
        toast.error(res.message || 'Sync failed');
      }
    } catch (err) {
      toast.error('Sync failed');
    } finally {
      setIsSyncing(false);
    }
  };

  const handleClearHistory = () => {
    if (window.confirm('Are you sure you want to clear your entire watch history?')) {
      localStorage.removeItem('aniverse_history');
      setHistory([]);
      toast.success('Watch history cleared');
    }
  };

  const handleDeleteItem = (e, animeId) => {
    e.preventDefault();
    e.stopPropagation();
    const updated = history.filter(item => item.animeId !== animeId);
    setHistory(updated);
    localStorage.setItem('aniverse_history', JSON.stringify(updated));
    toast.success('Removed from history');
  };

  const formatTime = (secs) => {
    if (!secs || isNaN(secs)) return '00:00';
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const formatDate = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-4xl mx-auto px-4 sm:px-6 py-24 min-h-screen font-inter"
    >
      {/* Title & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6 gap-4">
        <div className="flex items-center space-x-3">
          <Clock className="w-8 h-8 text-[#8B5CF6]" />
          <div>
            <h1 className="text-3xl font-bold text-white">Watch History</h1>
            <p className="text-gray-400 text-xs mt-0.5">Resume your anime at the exact second across phone & desktop</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          {googleConnected && (
            <button
              onClick={handleSync}
              disabled={isSyncing}
              className="flex items-center gap-2 bg-[#8B5CF6]/20 hover:bg-[#8B5CF6]/30 text-[#A78BFA] border border-[#8B5CF6]/40 px-3.5 py-2 rounded-xl text-xs font-semibold transition-colors"
              title="Sync with Google Drive"
            >
              <RefreshCw size={14} className={isSyncing ? "animate-spin" : ""} />
              <span>{isSyncing ? "Syncing..." : "Sync with Phone"}</span>
            </button>
          )}

          {history.length > 0 && (
            <button
              onClick={handleClearHistory}
              className="flex items-center gap-1.5 text-red-400 hover:text-red-300 hover:bg-red-500/10 px-3 py-2 rounded-xl text-xs font-semibold transition-colors border border-red-500/20"
            >
              <Trash2 size={14} />
              <span>Clear History</span>
            </button>
          )}
        </div>
      </div>

      {/* Cross-Platform Google Drive Cloud Sync Card */}
      <div className="bg-[#13131a] border border-[#2D2D44] rounded-2xl p-4 mb-8 flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#8B5CF6]/15 flex items-center justify-center text-[#8B5CF6] flex-shrink-0">
            <Cloud size={20} />
          </div>
          <div>
            <h3 className="text-white text-sm font-bold">
              {googleConnected ? `Google Drive Cloud Sync Connected` : `Cross-Platform Google Drive Sync`}
            </h3>
            <p className="text-gray-400 text-xs">
              {googleConnected 
                ? `Logged in as ${googleUser?.email || 'User'}. History & Favorites automatically synced with Android phone.`
                : `Connect Google Drive in Profile to sync exact second timestamps between Desktop and Mobile.`}
            </p>
          </div>
        </div>

        {!googleConnected && (
          <Link
            to="/profile"
            className="flex-shrink-0 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white px-4 py-2 rounded-xl text-xs font-bold transition-colors"
          >
            Connect Cloud
          </Link>
        )}
      </div>

      {/* Mode Filter Tabs */}
      <div className="flex items-center gap-2 mb-6 overflow-x-auto pb-1">
        <button
          onClick={() => setFilterMode('ALL')}
          className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
            filterMode === 'ALL'
              ? 'bg-white text-black shadow-md'
              : 'bg-[#13131a] text-gray-400 hover:text-white border border-[#26263A]'
          }`}
        >
          All History
        </button>
        <button
          onClick={() => setFilterMode(MODES.ANIME)}
          className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
            filterMode === MODES.ANIME
              ? 'bg-[#8B5CF6] text-white shadow-md'
              : 'bg-[#13131a] text-gray-400 hover:text-white border border-[#26263A]'
          }`}
        >
          Anime
        </button>
        <button
          onClick={() => setFilterMode(MODES.KDRAMA)}
          className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
            filterMode === MODES.KDRAMA
              ? 'bg-[#EC4899] text-white shadow-md'
              : 'bg-[#13131a] text-gray-400 hover:text-white border border-[#26263A]'
          }`}
        >
          K-Drama & Movies
        </button>
        <button
          onClick={() => setFilterMode(MODES.HOLLYWOOD)}
          className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
            filterMode === MODES.HOLLYWOOD
              ? 'bg-[#06B6D4] text-white shadow-md'
              : 'bg-[#13131a] text-gray-400 hover:text-white border border-[#26263A]'
          }`}
        >
          Hollywood & TV
        </button>
      </div>

      {/* YouTube Style History List */}
      {(() => {
        const filteredHistory = history.filter(item => {
          if (filterMode === 'ALL') return true;
          if (filterMode === MODES.ANIME) return item.mode === MODES.ANIME || !item.mode;
          return item.mode === filterMode;
        });

        if (filteredHistory.length === 0) {
          return (
            <div className="text-center py-20 bg-[#13131a] rounded-2xl border border-[#2D2D44]">
              <h3 className="text-xl text-white font-semibold mb-2">No history in this mode</h3>
              <p className="text-gray-400 mb-6 text-sm">Start watching to track your exact timestamps.</p>
              <Link to="/" className="inline-block px-6 py-2.5 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white rounded-xl text-sm font-bold transition-colors">
                Browse Titles
              </Link>
            </div>
          );
        }

        return (
          <div className="space-y-4">
            {filteredHistory.map((item) => {
              const timeSec = item.time || Math.round((item.positionMs || 0) / 1000);
              const durSec = item.duration || Math.round((item.durationMs || 1440000) / 1000);
              const progress = durSec > 0 ? Math.min(100, Math.round((timeSec / durSec) * 100)) : 0;
              const cover = item.coverUrl || item.image || item.coverImage;

              const isMedia = item.mediaType === 'movie' || item.mediaType === 'tv';
              const resumeLink = item.mediaType === 'movie'
                ? `/watch-media/movie/${item.id || item.animeId}?t=${timeSec}`
                : item.mediaType === 'tv'
                  ? `/watch-media/tv/${item.id || item.animeId}/${item.seasonNumber || 1}/${item.episodeNumber || 1}?t=${timeSec}`
                  : `/watch/${item.animeId}/${item.episodeId || ''}?t=${timeSec}`;

              const detailLink = isMedia
                ? `/media/${item.mediaType}/${item.id || item.animeId}`
                : `/anime/${item.animeId}`;

              return (
                <div 
                  key={item.animeId} 
                  className="flex flex-col sm:flex-row bg-[#13131a] rounded-2xl overflow-hidden border border-[#26263A] hover:border-[#8B5CF6]/60 transition-all group relative"
                >
                  {/* YouTube Style Thumbnail with Duration & Progress Bar */}
                  <div className="sm:w-56 h-36 flex-shrink-0 relative bg-black">
                    <img 
                      src={cover} 
                      alt={item.title}
                      className="w-full h-full object-cover"
                    />
                    <div className="absolute inset-0 bg-black/30 group-hover:bg-black/10 transition-colors"></div>
                    
                    {/* Play Overlay */}
                    <Link to={resumeLink} className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                      <div className="w-10 h-10 rounded-full bg-[#8B5CF6] text-white flex items-center justify-center shadow-lg">
                        <Play size={18} className="fill-current ml-0.5" />
                      </div>
                    </Link>

                    {/* Mode Tag */}
                    {item.mode && item.mode !== MODES.ANIME && (
                      <div className="absolute top-2 left-2 bg-black/80 text-[10px] font-bold px-1.5 py-0.5 rounded text-white border border-white/10 uppercase">
                        {item.mode}
                      </div>
                    )}

                    {/* Duration Tag */}
                    <div className="absolute bottom-2 right-2 bg-black/80 text-white text-[10px] font-mono font-bold px-1.5 py-0.5 rounded">
                      {formatTime(durSec)}
                    </div>

                    {/* YouTube Red/Violet Progress Bar at bottom of thumbnail */}
                    <div className="absolute bottom-0 left-0 right-0 h-1 bg-black/60">
                      <div 
                        className="h-full bg-[#8B5CF6]" 
                        style={{ width: `${progress}%` }}
                      />
                    </div>
                  </div>
                  
                  {/* Details */}
                  <div className="p-4 flex-grow flex flex-col justify-between">
                    <div>
                      <div className="flex items-start justify-between gap-2">
                        <h3 className="text-base font-bold text-white line-clamp-1 hover:text-[#A78BFA] transition-colors">
                          <Link to={detailLink}>
                            {item.title}
                          </Link>
                        </h3>

                        <button
                          onClick={(e) => handleDeleteItem(e, item.animeId)}
                          className="text-gray-500 hover:text-red-400 p-1 rounded-lg hover:bg-white/5 transition-colors"
                          title="Remove from history"
                        >
                          <X size={16} />
                        </button>
                      </div>

                      <p className="text-[#A78BFA] text-sm font-semibold mt-1">
                        {item.mediaType === 'movie' ? 'Full Movie' : `Episode ${item.episodeNumber} ${item.episodeTitle ? `- ${item.episodeTitle}` : ''}`}
                      </p>

                      {/* Exact Timestamp Badge (like YouTube) */}
                      <div className="flex items-center gap-2 mt-2">
                        <span className="text-xs font-mono font-bold text-amber-400 bg-amber-400/10 px-2 py-0.5 rounded-md border border-amber-400/20">
                          {formatTime(timeSec)} / {formatTime(durSec)}
                        </span>
                        <span className="text-xs text-gray-500">•</span>
                        <span className="text-xs text-gray-400 font-medium">{progress}% watched</span>
                        {item.updatedAt && (
                          <>
                            <span className="text-xs text-gray-500">•</span>
                            <span className="text-xs text-gray-500">{formatDate(item.updatedAt)}</span>
                          </>
                        )}
                      </div>
                    </div>
                    
                    {/* Actions: Resume at exact timestamp & Add to Watchlist */}
                    <div className="mt-4 flex items-center justify-end space-x-2">
                      <button
                        onClick={() => {
                          setSelectedAnimeForWatchlist({
                            id: item.id || item.animeId,
                            title: { english: item.title, romaji: item.title },
                            coverImage: { extraLarge: cover, large: cover },
                            format: item.mediaType === 'movie' ? 'MOVIE' : 'TV'
                          });
                        }}
                        className="flex items-center space-x-1.5 bg-[#1f1f2e] hover:bg-[#28283e] text-gray-300 hover:text-white px-3 py-1.5 rounded-lg text-xs font-medium transition-colors border border-[#2D2D44]"
                        title="Add to Watchlist"
                      >
                        <Plus size={14} />
                        <span>Watchlist</span>
                      </button>
                      <Link
                        to={resumeLink}
                        className="flex items-center space-x-2 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white px-4 py-1.5 rounded-lg text-xs font-bold transition-colors shadow-lg shadow-[#8B5CF6]/20"
                      >
                        <Play size={14} className="fill-current" />
                        <span>Resume at {formatTime(timeSec)}</span>
                      </Link>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        );
      })()}

      {selectedAnimeForWatchlist && (
        <WatchlistModal
          isOpen={!!selectedAnimeForWatchlist}
          onClose={() => setSelectedAnimeForWatchlist(null)}
          anime={selectedAnimeForWatchlist}
        />
      )}
    </motion.div>
  );
}
