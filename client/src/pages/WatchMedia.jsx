import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Play, SkipForward, ArrowLeft, Star, Tv, Film, Server, Plus, Clock, LayoutGrid, List } from 'lucide-react';
import api from '../services/api';
import VideoPlayer from '../components/player/VideoPlayer';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import WatchlistModal from '../components/ui/WatchlistModal';
import { useMode } from '../context/ModeContext';

export default function WatchMedia() {
  const { type, id, season = '1', episode = '1' } = useParams();
  const [searchParams] = useSearchParams();
  const resumeTimeParam = searchParams.get('t');
  const navigate = useNavigate();
  const { mode } = useMode();

  const [media, setMedia] = useState(null);
  const [episodes, setEpisodes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingStream, setLoadingStream] = useState(false);
  const [viewMode, setViewMode] = useState('grid'); // 'grid' or 'list'
  const [embedUrl, setEmbedUrl] = useState(null);
  const [servers, setServers] = useState([]);
  const [activeServer, setActiveServer] = useState(null);
  const [isWatchlistModalOpen, setIsWatchlistModalOpen] = useState(false);

  const isTv = type === 'tv';
  const currentSeason = parseInt(season, 10) || 1;
  const currentEpisode = parseInt(episode, 10) || 1;

  useEffect(() => {
    let isCancelled = false;

    const loadMediaAndStream = async () => {
      try {
        if (!media || media.id !== parseInt(id, 10)) {
          setLoading(true);
        } else {
          setLoadingStream(true);
        }

        const promises = [
          api.Media.getStream(type, id, currentSeason, currentEpisode),
        ];

        const needDetail = !media || media.id !== parseInt(id, 10);
        if (needDetail) {
          promises.push(api.Media.getDetail(type, id));
          if (isTv) {
            promises.push(api.Media.getEpisodes(id, currentSeason));
          }
        }

        const results = await Promise.all(promises);
        if (isCancelled) return;

        const streamRes = results[0];
        const streamData = streamRes.data?.data || streamRes.data;

        let mediaData = media;
        if (needDetail && results.length > 1) {
          const detailRes = results[1];
          mediaData = detailRes.data?.data || detailRes.data;
          setMedia(mediaData);

          if (results.length > 2) {
            const epRes = results[2];
            setEpisodes(epRes.data?.data || []);
          }
        }

        if (streamData?.servers && streamData.servers.length > 0) {
          setServers(streamData.servers);
          // Preserve selected server name if exists in new list
          const chosen = streamData.servers.find(s => s.server === activeServer?.server) || streamData.servers[0];
          setActiveServer(chosen);
          setEmbedUrl(chosen.url);
        }

        // Save to Local History with Mode tag
        try {
          const history = JSON.parse(localStorage.getItem('aniverse_history') || '[]');
          const itemKey = `${type}_${id}`;
          const titleText = mediaData.title?.english || mediaData.name || 'Untitled';
          const coverUrl = mediaData.coverImage?.large || mediaData.bannerImage || '';

          const existingIndex = history.findIndex(h => h.animeId === itemKey || (h.id === id && h.mediaType === type));
          const historyItem = {
            animeId: itemKey,
            id,
            mediaType: type,
            mode: mode, // 'kdrama' or 'hollywood'
            title: titleText,
            coverImage: coverUrl,
            coverUrl: coverUrl,
            episodeNumber: isTv ? currentEpisode : 1,
            seasonNumber: isTv ? currentSeason : 1,
            episodeTitle: isTv ? `S${currentSeason} E${currentEpisode}` : 'Full Movie',
            time: resumeTimeParam ? parseInt(resumeTimeParam, 10) : 0,
            duration: (mediaData.runtime || 120) * 60,
            updatedAt: new Date().toISOString(),
          };

          if (existingIndex > -1) {
            history[existingIndex] = { ...history[existingIndex], ...historyItem };
          } else {
            history.unshift(historyItem);
          }

          localStorage.setItem('aniverse_history', JSON.stringify(history.slice(0, 100)));

          // Set Now Playing for Spotify-like mini player bar
          localStorage.setItem('aniverse_now_playing', JSON.stringify({
            animeId: itemKey,
            id,
            mediaType: type,
            mode: mode,
            episodeNumber: isTv ? currentEpisode : 1,
            seasonNumber: isTv ? currentSeason : 1,
            title: titleText,
            coverImage: coverUrl,
            time: resumeTimeParam ? parseInt(resumeTimeParam, 10) : 0,
            duration: (mediaData.runtime || 120) * 60,
            isPlaying: true,
          }));
          window.dispatchEvent(new Event('aniverse_now_playing_update'));
        } catch (e) {
          console.error('Failed to save media history:', e);
        }
      } catch (err) {
        console.error('Failed to load media or stream:', err);
      } finally {
        if (!isCancelled) {
          setLoading(false);
          setLoadingStream(false);
        }
      }
    };

    loadMediaAndStream();
    return () => {
      isCancelled = true;
    };
  }, [type, id, currentSeason, currentEpisode, mode]);

  const handleServerChange = (server) => {
    setActiveServer(server);
    setEmbedUrl(server.url);
  };

  const hasNextEpisode = isTv && currentEpisode < episodes.length;
  const nextEpisodeNum = currentEpisode + 1;

  if (loading) return <LoadingSpinner />;
  if (!media) return <div className="text-center text-white py-24">Stream not found</div>;

  const title = media.title?.english || media.name;

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-[1600px] mx-auto px-4 md:px-8 py-20 font-inter"
    >
      <div className="flex items-center justify-between mb-4">
        <Link
          to={`/media/${type}/${id}`}
          className="inline-flex items-center gap-2 text-gray-400 hover:text-white transition-colors text-sm font-semibold"
        >
          <ArrowLeft size={16} /> Back to Details
        </Link>

        {isTv && hasNextEpisode && (
          <Link
            to={`/watch-media/tv/${id}/${currentSeason}/${nextEpisodeNum}`}
            className="inline-flex items-center gap-2 bg-[#8B5CF6]/20 hover:bg-[#8B5CF6]/30 text-[#A78BFA] border border-[#8B5CF6]/40 px-3.5 py-1.5 rounded-xl text-xs font-bold transition-colors"
          >
            <span>Next Ep {nextEpisodeNum}</span>
            <SkipForward size={14} />
          </Link>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Main Video Player Area */}
        <div className="lg:col-span-3 flex flex-col gap-4">
          <div className="aspect-video w-full rounded-2xl overflow-hidden bg-black shadow-2xl border border-white/10 relative">
            {embedUrl ? (
              <VideoPlayer
                key={`${activeServer?.server}-${currentSeason}-${currentEpisode}`}
                url={embedUrl}
                isEmbed={true}
                startTime={resumeTimeParam ? parseInt(resumeTimeParam, 10) : 0}
                onEnded={() => {
                  if (hasNextEpisode) {
                    navigate(`/watch-media/tv/${id}/${currentSeason}/${nextEpisodeNum}`);
                  }
                }}
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-gray-400">
                Loading stream...
              </div>
            )}

            {loadingStream && (
              <div className="absolute inset-0 bg-black/75 backdrop-blur-sm flex flex-col items-center justify-center z-30 transition-all">
                <div className="w-8 h-8 border-3 border-[#8B5CF6] border-t-transparent rounded-full animate-spin mb-2"></div>
                <p className="text-white text-xs font-semibold">Loading episode stream...</p>
              </div>
            )}
          </div>

          {/* Servers Bar */}
          <div className="bg-[#13131a] border border-[#26263A] rounded-2xl p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-center gap-2 text-sm text-gray-400 font-semibold">
              <Server size={18} className="text-[#8B5CF6]" />
              <span>Streaming Servers:</span>
            </div>

            <div className="flex flex-wrap gap-2">
              {servers.map((srv, idx) => (
                <button
                  key={srv.server || idx}
                  onClick={() => handleServerChange(srv)}
                  className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all ${
                    activeServer?.server === srv.server
                      ? 'bg-[#8B5CF6] text-white shadow-md shadow-[#8B5CF6]/30'
                      : 'bg-[#1a1a2e] text-gray-300 hover:text-white border border-[#2D2D44]'
                  }`}
                >
                  {srv.name}
                </button>
              ))}
            </div>
          </div>

          {/* Media Info Under Player */}
          <div className="bg-[#13131a] border border-[#26263A] rounded-2xl p-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-xs font-bold text-[#A78BFA] bg-[#8B5CF6]/20 px-2.5 py-0.5 rounded-md border border-[#8B5CF6]/30">
                    {isTv ? `SEASON ${currentSeason} • EPISODE ${currentEpisode}` : 'MOVIE'}
                  </span>
                  {media.year && (
                    <span className="text-xs text-gray-400 font-semibold">{media.year}</span>
                  )}
                </div>
                <h1 className="text-2xl font-bold text-white">{title}</h1>
              </div>

              <div className="flex items-center gap-3">
                <button
                  onClick={() => setIsWatchlistModalOpen(true)}
                  className="flex items-center gap-1.5 bg-[#1a1a2e] hover:bg-[#252540] text-gray-300 hover:text-white px-4 py-2 rounded-xl text-xs font-semibold border border-[#2D2D44] transition-colors"
                >
                  <Plus size={16} /> Watchlist
                </button>
              </div>
            </div>

            {media.overview && (
              <p className="text-gray-400 text-sm mt-4 leading-relaxed line-clamp-3">
                {media.overview}
              </p>
            )}
          </div>
        </div>

        {/* Sidebar: Episodes List for TV Series */}
        <div className="lg:col-span-1 flex flex-col gap-4">
          {isTv ? (
            <div className="bg-[#13131a] border border-[#26263A] rounded-2xl p-4 flex flex-col max-h-[720px]">
              <div className="flex items-center justify-between mb-4 pb-3 border-b border-white/5">
                <h3 className="text-white text-sm font-bold flex items-center gap-2">
                  <Tv size={16} className="text-[#8B5CF6]" />
                  <span>Season {currentSeason}</span>
                </h3>
                <div className="flex items-center gap-2">
                  <div className="flex items-center bg-[#1a1a2e] rounded-lg p-0.5 border border-white/5">
                    <button
                      onClick={() => setViewMode('grid')}
                      className={`p-1 rounded ${viewMode === 'grid' ? 'bg-[#8B5CF6] text-white' : 'text-gray-400 hover:text-white'}`}
                      title="Grid View"
                    >
                      <LayoutGrid size={14} />
                    </button>
                    <button
                      onClick={() => setViewMode('list')}
                      className={`p-1 rounded ${viewMode === 'list' ? 'bg-[#8B5CF6] text-white' : 'text-gray-400 hover:text-white'}`}
                      title="List View"
                    >
                      <List size={14} />
                    </button>
                  </div>
                  <span className="text-xs text-gray-500 font-mono font-bold">
                    {episodes.length} Eps
                  </span>
                </div>
              </div>

              {viewMode === 'grid' ? (
                <div className="overflow-y-auto pr-1 custom-scrollbar">
                  <div className="grid grid-cols-4 sm:grid-cols-6 lg:grid-cols-4 gap-2">
                    {episodes.map(ep => {
                      const isCurrent = ep.episodeNumber === currentEpisode;
                      return (
                        <Link
                          key={ep.id}
                          to={`/watch-media/tv/${id}/${currentSeason}/${ep.episodeNumber}`}
                          className={`flex items-center justify-center py-2.5 rounded-xl font-mono text-xs font-bold transition-all ${
                            isCurrent
                              ? 'bg-[#8B5CF6] text-white shadow-lg shadow-[#8B5CF6]/30 border border-[#8B5CF6]'
                              : 'bg-[#1a1a2e] text-gray-400 hover:bg-[#252540] hover:text-white border border-[#2D2D44]'
                          }`}
                        >
                          {ep.episodeNumber}
                        </Link>
                      );
                    })}
                  </div>
                </div>
              ) : (
                <div className="overflow-y-auto space-y-2 flex-grow pr-1 custom-scrollbar">
                  {episodes.map(ep => {
                    const isCurrent = ep.episodeNumber === currentEpisode;
                    return (
                      <Link
                        key={ep.id}
                        to={`/watch-media/tv/${id}/${currentSeason}/${ep.episodeNumber}`}
                        className={`flex items-center gap-3 p-2.5 rounded-xl transition-all ${
                          isCurrent
                            ? 'bg-[#8B5CF6] text-white shadow-md'
                            : 'bg-[#1a1a2e] text-gray-300 hover:bg-[#252540] hover:text-white border border-transparent hover:border-[#2D2D44]'
                        }`}
                      >
                        <div className="w-8 h-8 rounded-lg bg-black/40 flex items-center justify-center font-mono font-bold text-xs flex-shrink-0">
                          {ep.episodeNumber}
                        </div>
                        <div className="flex-grow min-w-0">
                          <p className="text-xs font-semibold truncate leading-snug">
                            {ep.title}
                          </p>
                          {ep.runtime > 0 && (
                            <span className={`text-[10px] ${isCurrent ? 'text-white/80' : 'text-gray-500'}`}>
                              {ep.runtime} min
                            </span>
                          )}
                        </div>
                      </Link>
                    );
                  })}
                </div>
              )}
            </div>
          ) : (
            <div className="bg-[#13131a] border border-[#26263A] rounded-2xl p-4">
              <h3 className="text-white text-sm font-bold mb-3 flex items-center gap-2">
                <Film size={16} className="text-[#8B5CF6]" />
                <span>Movie Information</span>
              </h3>
              <div className="space-y-2.5 text-xs text-gray-400">
                <div className="flex justify-between">
                  <span>Status:</span>
                  <span className="text-white font-semibold">{media.status || 'Released'}</span>
                </div>
                {media.runtime > 0 && (
                  <div className="flex justify-between">
                    <span>Duration:</span>
                    <span className="text-white font-semibold">{media.runtime} minutes</span>
                  </div>
                )}
                {media.voteAverage > 0 && (
                  <div className="flex justify-between">
                    <span>Rating:</span>
                    <span className="text-amber-400 font-bold">{media.voteAverage.toFixed(1)} / 10</span>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {isWatchlistModalOpen && (
        <WatchlistModal
          isOpen={isWatchlistModalOpen}
          onClose={() => setIsWatchlistModalOpen(false)}
          anime={{
            id: media.id,
            title: media.title,
            coverImage: media.coverImage,
            format: media.format,
            averageScore: media.averageScore,
          }}
        />
      )}
    </motion.div>
  );
}
