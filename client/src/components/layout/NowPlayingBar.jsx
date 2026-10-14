import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Play, Pause, X } from 'lucide-react';

export default function NowPlayingBar() {
  const [nowPlaying, setNowPlaying] = useState(null);
  const location = useLocation();
  const navigate = useNavigate();

  const loadNowPlaying = () => {
    try {
      const data = localStorage.getItem('aniverse_now_playing');
      if (data) {
        setNowPlaying(JSON.parse(data));
      } else {
        setNowPlaying(null);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    loadNowPlaying();

    const handleUpdate = () => loadNowPlaying();
    window.addEventListener('aniverse_now_playing_update', handleUpdate);
    window.addEventListener('storage', handleUpdate);

    return () => {
      window.removeEventListener('aniverse_now_playing_update', handleUpdate);
      window.removeEventListener('storage', handleUpdate);
    };
  }, []);

  // Hide when on watch page or when no active playback
  if (!nowPlaying || location.pathname.startsWith('/watch') || location.pathname.startsWith('/watch-media')) {
    return null;
  }

  const formatTime = (secs) => {
    if (!secs || isNaN(secs)) return '00:00';
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const progress = nowPlaying.duration ? Math.min(100, Math.round(((nowPlaying.time || 0) / nowPlaying.duration) * 100)) : 0;

  const handleBarClick = () => {
    if (nowPlaying.mediaType || nowPlaying.mode === 'kdrama' || nowPlaying.mode === 'hollywood') {
      const type = nowPlaying.mediaType || 'tv';
      const id = nowPlaying.id || nowPlaying.animeId;
      const s = nowPlaying.seasonNumber || 1;
      const ep = nowPlaying.episodeNumber || 1;
      navigate(`/watch-media/${type}/${id}/${s}/${ep}?t=${nowPlaying.time || 0}`);
    } else {
      navigate(`/watch/${nowPlaying.animeId}/${nowPlaying.episodeId || ''}?t=${nowPlaying.time || 0}`);
    }
  };

  const handleDismiss = (e) => {
    e.stopPropagation();
    localStorage.removeItem('aniverse_now_playing');
    setNowPlaying(null);
    window.dispatchEvent(new Event('aniverse_now_playing_update'));
  };

  const handleTogglePlay = (e) => {
    e.stopPropagation();
    const updated = { ...nowPlaying, isPlaying: !nowPlaying.isPlaying };
    localStorage.setItem('aniverse_now_playing', JSON.stringify(updated));
    setNowPlaying(updated);
  };

  return (
    <div className="fixed bottom-4 left-4 right-4 md:left-8 md:right-8 lg:left-auto lg:right-8 lg:w-96 z-50">
      <div 
        onClick={handleBarClick}
        className="bg-[#13131a]/95 backdrop-blur-md border border-[#8B5CF6]/40 hover:border-[#8B5CF6] transition-all rounded-2xl shadow-2xl overflow-hidden cursor-pointer group"
      >
        {/* Top Accent Progress Bar */}
        <div className="w-full bg-[#1f1f2e] h-1">
          <div 
            className="bg-[#8B5CF6] h-full transition-all duration-300" 
            style={{ width: `${progress}%` }}
          />
        </div>

        <div className="p-3 flex items-center justify-between gap-3">
          {/* Thumbnail */}
          <div className="w-12 h-12 rounded-lg overflow-hidden flex-shrink-0 bg-[#1a1a2e] relative">
            <img 
              src={nowPlaying.coverImage} 
              alt={nowPlaying.title} 
              className="w-full h-full object-cover"
            />
            {nowPlaying.isPlaying && (
              <div className="absolute inset-0 bg-black/20 flex items-center justify-center">
                <span className="w-2 h-2 rounded-full bg-[#8B5CF6] animate-pulse"></span>
              </div>
            )}
          </div>

          {/* Details */}
          <div className="flex-grow min-w-0">
            <h4 className="text-white text-sm font-bold truncate group-hover:text-[#A78BFA] transition-colors">
              {nowPlaying.title}
            </h4>
            <div className="flex items-center gap-1.5 text-xs text-gray-400 mt-0.5">
              <span className="text-[#8B5CF6] font-semibold">EP {nowPlaying.episodeNumber || 1}</span>
              <span>•</span>
              <span className="text-gray-300 font-mono">
                {formatTime(nowPlaying.time)} / {formatTime(nowPlaying.duration || 1440)}
              </span>
            </div>
          </div>

          {/* Controls */}
          <div className="flex items-center gap-2 flex-shrink-0">
            <button
              onClick={handleTogglePlay}
              className="w-8 h-8 rounded-full bg-[#8B5CF6]/20 hover:bg-[#8B5CF6] text-[#8B5CF6] hover:text-white flex items-center justify-center transition-colors"
              title={nowPlaying.isPlaying ? "Pause" : "Play"}
            >
              {nowPlaying.isPlaying ? <Pause size={16} /> : <Play size={16} className="fill-current ml-0.5" />}
            </button>
            <button
              onClick={handleDismiss}
              className="w-7 h-7 rounded-full text-gray-400 hover:text-white hover:bg-white/10 flex items-center justify-center transition-colors"
              title="Close"
            >
              <X size={15} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
