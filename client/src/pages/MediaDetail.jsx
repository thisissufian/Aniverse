import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Play, Plus, Star, Calendar, Clock, Film, Tv, ArrowLeft } from 'lucide-react';
import api from '../services/api';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import WatchlistModal from '../components/ui/WatchlistModal';
import { useMode } from '../context/ModeContext';

export default function MediaDetail() {
  const { type, id } = useParams();
  const { config } = useMode();
  const [media, setMedia] = useState(null);
  const [episodes, setEpisodes] = useState([]);
  const [selectedSeason, setSelectedSeason] = useState(1);
  const [loading, setLoading] = useState(true);
  const [isWatchlistModalOpen, setIsWatchlistModalOpen] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const res = await api.Media.getDetail(type, id);
        const data = res.data?.data || res.data;
        setMedia(data);

        if (type === 'tv') {
          const firstSeason = (data.seasons && data.seasons.length > 0) ? data.seasons[0].seasonNumber : 1;
          setSelectedSeason(firstSeason);
          const epRes = await api.Media.getEpisodes(id, firstSeason);
          setEpisodes(epRes.data?.data || []);
        }
      } catch (err) {
        console.error('Failed to load media details:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [type, id]);

  const handleSeasonChange = async (seasonNum) => {
    setSelectedSeason(seasonNum);
    try {
      const epRes = await api.Media.getEpisodes(id, seasonNum);
      setEpisodes(epRes.data?.data || []);
    } catch (e) {
      console.error('Failed to load season episodes:', e);
    }
  };

  if (loading) return <LoadingSpinner />;
  if (!media) return <div className="text-center text-white py-24">Media not found</div>;

  const title = media.title?.english || media.name;
  const originalTitle = media.title?.romaji || media.title?.native;
  const isTv = type === 'tv';

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="pb-16 font-inter"
    >
      {/* Banner */}
      <div className="relative w-full h-[45vh] md:h-[55vh]">
        <img
          src={media.bannerImage || media.coverImage?.extraLarge}
          alt={title}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0f] via-[#0a0a0f]/80 to-transparent"></div>
        <div className="absolute top-20 left-4 md:left-8 z-20">
          <Link
            to="/"
            className="inline-flex items-center gap-2 bg-black/60 hover:bg-black/80 text-white px-4 py-2 rounded-xl text-sm font-semibold backdrop-blur-md border border-white/10 transition-colors"
          >
            <ArrowLeft size={16} /> Back to Browse
          </Link>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-36 relative z-10">
        <div className="flex flex-col md:flex-row gap-8">
          {/* Poster & Actions */}
          <div className="w-52 md:w-64 flex-shrink-0 mx-auto md:mx-0">
            <img
              src={media.coverImage?.extraLarge}
              alt={title}
              className="w-full rounded-2xl shadow-2xl shadow-black/80 border border-white/10 mb-6"
            />
            <div className="flex flex-col gap-3">
              <Link
                to={isTv ? `/watch-media/tv/${id}/${selectedSeason}/1` : `/watch-media/movie/${id}`}
                className="w-full flex items-center justify-center space-x-2 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white py-3.5 rounded-xl font-bold transition-all shadow-lg shadow-[#8B5CF6]/30"
              >
                <Play size={18} className="fill-current" />
                <span>{isTv ? 'Watch S1 E1' : 'Play Movie'}</span>
              </Link>

              <button
                onClick={() => setIsWatchlistModalOpen(true)}
                className="w-full flex items-center justify-center space-x-2 bg-[#1a1a2e] hover:bg-[#252540] text-white py-3 rounded-xl font-semibold transition-colors border border-[#2D2D44]"
              >
                <Plus size={18} />
                <span>Add to Watchlist</span>
              </button>
            </div>
          </div>

          {/* Info Section */}
          <div className="flex-grow pt-4 md:pt-36 text-center md:text-left">
            <div className="flex flex-wrap items-center justify-center md:justify-start gap-2 mb-2">
              <span className="bg-[#8B5CF6]/20 text-[#A78BFA] border border-[#8B5CF6]/30 text-xs font-bold px-2.5 py-0.5 rounded-md">
                {isTv ? 'TV SERIES' : 'MOVIE'}
              </span>
              {media.year && (
                <span className="bg-white/5 text-gray-300 text-xs font-semibold px-2.5 py-0.5 rounded-md border border-white/10">
                  {media.year}
                </span>
              )}
              {media.voteAverage > 0 && (
                <span className="flex items-center gap-1 bg-amber-400/10 text-amber-400 text-xs font-bold px-2.5 py-0.5 rounded-md border border-amber-400/20">
                  <Star size={12} className="fill-current" /> {media.voteAverage.toFixed(1)}
                </span>
              )}
            </div>

            <h1 className="text-3xl md:text-5xl font-extrabold text-white mb-2 leading-tight">
              {title}
            </h1>
            {originalTitle && originalTitle !== title && (
              <p className="text-gray-400 text-lg mb-4 font-medium">{originalTitle}</p>
            )}

            <p className="text-gray-300 text-sm md:text-base leading-relaxed mb-6 max-w-3xl">
              {media.overview || 'No description available for this title.'}
            </p>

            {/* Cast Highlight */}
            {media.cast && media.cast.length > 0 && (
              <div className="mb-8">
                <h3 className="text-white text-sm font-bold uppercase tracking-wider text-gray-400 mb-3">
                  Starring Cast
                </h3>
                <div className="flex flex-wrap gap-2">
                  {media.cast.map(c => (
                    <span
                      key={c.id}
                      className="bg-[#13131a] text-gray-300 text-xs font-medium px-3 py-1.5 rounded-lg border border-[#26263A]"
                    >
                      {c.name} <span className="text-gray-500">as {c.character}</span>
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* TV Series Episode Browser */}
        {isTv && (
          <div className="mt-12 bg-[#13131a] border border-[#26263A] rounded-2xl p-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
              <h2 className="text-xl font-bold text-white flex items-center gap-2">
                <Tv size={20} className="text-[#8B5CF6]" /> Episodes
              </h2>

              {/* Seasons Selector */}
              {media.seasons && media.seasons.length > 1 && (
                <div className="flex flex-wrap gap-2">
                  {media.seasons.map(s => (
                    <button
                      key={s.id}
                      onClick={() => handleSeasonChange(s.seasonNumber)}
                      className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all ${
                        selectedSeason === s.seasonNumber
                          ? 'bg-[#8B5CF6] text-white shadow-md shadow-[#8B5CF6]/30'
                          : 'bg-[#1a1a2e] text-gray-400 hover:text-white border border-[#2D2D44]'
                      }`}
                    >
                      {s.name || `Season ${s.seasonNumber}`}
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Episode Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {episodes.map(ep => (
                <Link
                  key={ep.id}
                  to={`/watch-media/tv/${id}/${selectedSeason}/${ep.episodeNumber}`}
                  className="bg-[#1a1a2e] border border-[#26263A] hover:border-[#8B5CF6] rounded-xl overflow-hidden transition-all group flex flex-col"
                >
                  <div className="relative aspect-video bg-black overflow-hidden">
                    {ep.stillPath ? (
                      <img
                        src={ep.stillPath}
                        alt={ep.title}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-gray-600">
                        <Film size={28} />
                      </div>
                    )}
                    <div className="absolute inset-0 bg-black/30 group-hover:bg-black/10 transition-colors flex items-center justify-center">
                      <div className="w-9 h-9 rounded-full bg-[#8B5CF6] text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity shadow-lg">
                        <Play size={16} className="fill-current ml-0.5" />
                      </div>
                    </div>
                    <span className="absolute bottom-2 left-2 bg-black/80 text-white font-mono text-[10px] font-bold px-1.5 py-0.5 rounded">
                      EP {ep.episodeNumber}
                    </span>
                  </div>
                  <div className="p-3 flex-grow flex flex-col justify-between">
                    <h4 className="text-white text-sm font-semibold line-clamp-1 group-hover:text-[#A78BFA] transition-colors">
                      {ep.title}
                    </h4>
                    {ep.overview && (
                      <p className="text-gray-400 text-xs line-clamp-2 mt-1">{ep.overview}</p>
                    )}
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* Recommendations */}
        {media.recommendations && media.recommendations.length > 0 && (
          <div className="mt-12">
            <h2 className="text-2xl font-bold text-white mb-6">You Might Also Like</h2>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
              {media.recommendations.map(r => (
                <Link
                  key={r.id}
                  to={`/media/${r.type}/${r.id}`}
                  className="group bg-[#13131a] rounded-xl overflow-hidden border border-[#26263A] hover:border-[#8B5CF6] transition-all"
                >
                  <div className="aspect-[2/3] overflow-hidden bg-black relative">
                    <img
                      src={r.coverImage?.large}
                      alt={r.title?.english}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                    />
                  </div>
                  <div className="p-2.5">
                    <h4 className="text-white text-xs font-semibold line-clamp-1 group-hover:text-[#A78BFA]">
                      {r.title?.english}
                    </h4>
                    <p className="text-gray-500 text-[10px] mt-0.5">{r.year || ''}</p>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}
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
