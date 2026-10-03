import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Play, Plus, Star, Calendar, Clock, Monitor } from 'lucide-react';
import api from '../services/api';
import GenreChip from '../components/ui/GenreChip';
import AnimeCarousel from '../components/ui/AnimeCarousel';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import WatchlistModal from '../components/ui/WatchlistModal';

export default function AnimeDetail() {
  const { id } = useParams();
  const [anime, setAnime] = useState(null);
  const [episodes, setEpisodes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isDescExpanded, setIsDescExpanded] = useState(false);
  const [activeTab, setActiveTab] = useState('sub');
  const [isWatchlistModalOpen, setIsWatchlistModalOpen] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        // Mock fallback to prevent crashes if API is not yet running
        const [animeRes, epRes] = await Promise.all([
          api.Anime.getAnimeDetail(id).catch(() => ({ data: { data: {} } })),
          api.Anime.getEpisodes(id).catch(() => ({ data: { data: [] } }))
        ]);
        setAnime(animeRes.data?.data || animeRes.data || {});
        setEpisodes(epRes.data?.data || epRes.data || []);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  if (loading) return <LoadingSpinner />;
  if (!anime || Object.keys(anime).length === 0) return <div className="text-center text-white py-24">Anime not found</div>;

  const title = anime.title?.english || anime.title?.romaji;
  const description = anime.description?.replace(/<[^>]*>?/gm, '');

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="pb-10"
    >
      {/* Banner */}
      <div className="relative w-full h-[40vh] md:h-[50vh]">
        <img 
          src={anime.bannerImage || anime.coverImage?.extraLarge} 
          alt="Banner" 
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0f] via-[#0a0a0f]/80 to-transparent"></div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-32 relative z-10">
        <div className="flex flex-col md:flex-row gap-8">
          {/* Poster & Actions */}
          <div className="w-48 md:w-64 flex-shrink-0 mx-auto md:mx-0">
            <img 
              src={anime.coverImage?.extraLarge} 
              alt={title} 
              className="w-full rounded-lg shadow-2xl shadow-[#8B5CF6]/20 mb-6"
            />
            <div className="flex flex-col gap-3">
              {episodes.length > 0 && (
                <Link
                  to={`/watch/${id}/${episodes[0].id}`}
                  className="w-full flex items-center justify-center space-x-2 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white py-3 rounded-lg font-semibold transition-colors"
                >
                  <Play size={18} className="fill-current" />
                  <span>Watch Episode 1</span>
                </Link>
              )}
              <button 
                onClick={() => setIsWatchlistModalOpen(true)}
                className="w-full flex items-center justify-center space-x-2 bg-[#252540] hover:bg-[#2D2D44] text-white py-3 rounded-lg font-semibold transition-colors border border-[#2D2D44]"
              >
                <Plus size={18} />
                <span>Add to Watchlist</span>
              </button>
            </div>
          </div>

          {/* Info */}
          <div className="flex-grow pt-4 md:pt-32 text-center md:text-left">
            <h1 className="text-3xl md:text-5xl font-bold text-white mb-2">{title}</h1>
            {anime.title?.native && (
              <p className="text-gray-400 text-lg mb-4">{anime.title.native}</p>
            )}

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
              {anime.averageScore && (
                <div className="bg-[#1a1a2e]/60 backdrop-blur-md border border-white/5 hover:bg-[#252540]/80 transition-colors p-3 rounded-xl flex flex-col items-center justify-center">
                  <div className="flex items-center text-yellow-400 mb-1">
                    <Star size={16} className="fill-current mr-1" />
                    <span className="font-bold text-white text-lg leading-none">{(anime.averageScore / 10).toFixed(1)}</span>
                  </div>
                  <span className="text-[11px] text-gray-400 uppercase tracking-wider font-semibold">Score</span>
                </div>
              )}
              {anime.format && (
                <div className="bg-[#1a1a2e]/60 backdrop-blur-md border border-white/5 hover:bg-[#252540]/80 transition-colors p-3 rounded-xl flex flex-col items-center justify-center">
                  <div className="flex items-center text-[#A78BFA] mb-1">
                    <Monitor size={16} className="mr-1.5" />
                    <span className="font-bold text-white text-base leading-none">{anime.format}</span>
                  </div>
                  <span className="text-[11px] text-gray-400 uppercase tracking-wider font-semibold">Format</span>
                </div>
              )}
              {anime.seasonYear && (
                <div className="bg-[#1a1a2e]/60 backdrop-blur-md border border-white/5 hover:bg-[#252540]/80 transition-colors p-3 rounded-xl flex flex-col items-center justify-center">
                  <div className="flex items-center text-[#A78BFA] mb-1">
                    <Calendar size={16} className="mr-1.5" />
                    <span className="font-bold text-white text-base leading-none">{anime.seasonYear}</span>
                  </div>
                  <span className="text-[11px] text-gray-400 uppercase tracking-wider font-semibold">Year</span>
                </div>
              )}
              {(anime.duration || anime.episodes) && (
                <div className="bg-[#1a1a2e]/60 backdrop-blur-md border border-white/5 hover:bg-[#252540]/80 transition-colors p-3 rounded-xl flex flex-col items-center justify-center">
                  <div className="flex items-center text-[#A78BFA] mb-1">
                    <Clock size={16} className="mr-1.5" />
                    <span className="font-bold text-white text-base leading-none">{anime.duration ? `${anime.duration}m` : `${anime.episodes} EP`}</span>
                  </div>
                  <span className="text-[11px] text-gray-400 uppercase tracking-wider font-semibold">{anime.duration ? 'Duration' : 'Episodes'}</span>
                </div>
              )}
            </div>

            <div className="flex flex-wrap justify-center md:justify-start gap-2 mb-6">
              {anime.genres?.map(genre => <GenreChip key={genre} genre={genre} />)}
            </div>

            <div className="mb-8">
              <h3 className="text-white font-medium mb-2">Synopsis</h3>
              <p className={`text-gray-300 text-sm md:text-base leading-relaxed ${!isDescExpanded && 'line-clamp-4'}`}>
                {description}
              </p>
              {description?.length > 200 && (
                <button 
                  onClick={() => setIsDescExpanded(!isDescExpanded)}
                  className="text-[#8B5CF6] hover:text-[#A78BFA] text-sm mt-2 font-medium"
                >
                  Read {isDescExpanded ? 'Less' : 'More'}
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Episodes Section */}
        {episodes.length > 0 && (
          <div className="mt-16">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6 gap-4">
              <h2 className="text-2xl font-bold text-white">Episodes</h2>
              
              {/* Sub/Dub Segmented Control */}
              <div className="bg-[#1a1a2e] p-1 rounded-lg inline-flex border border-white/5 shadow-inner">
                <button 
                  onClick={() => setActiveTab('sub')}
                  className={`px-6 py-1.5 rounded-md text-sm font-medium transition-all ${activeTab === 'sub' ? 'bg-[#8B5CF6] text-white shadow-lg' : 'text-gray-400 hover:text-white'}`}
                >
                  Sub
                </button>
                <button 
                  onClick={() => setActiveTab('dub')}
                  className={`px-6 py-1.5 rounded-md text-sm font-medium transition-all ${activeTab === 'dub' ? 'bg-[#8B5CF6] text-white shadow-lg' : 'text-gray-400 hover:text-white'}`}
                >
                  Dub
                </button>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {episodes.map((ep) => (
                <Link
                  key={ep.id}
                  to={`/watch/${id}/${ep.id}?type=${activeTab}`}
                  className="group relative overflow-hidden rounded-xl bg-[#1a1a2e] border border-[#2D2D44] hover:border-[#8B5CF6]/50 transition-all shadow-lg hover:shadow-[#8B5CF6]/10 hover:-translate-y-1"
                >
                  <div className="aspect-video relative overflow-hidden bg-[#13131a]">
                    <img 
                      src={ep.image || anime.coverImage?.extraLarge} 
                      alt={`Episode ${ep.number}`} 
                      className="w-full h-full object-cover opacity-80 group-hover:opacity-100 group-hover:scale-105 transition-all duration-500"
                    />
                    <div className="absolute inset-0 bg-black/40 group-hover:bg-transparent transition-colors duration-300"></div>
                    
                    {/* Play Button Overlay */}
                    <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                      <div className="w-12 h-12 bg-[#8B5CF6]/90 rounded-full flex items-center justify-center shadow-lg backdrop-blur-sm">
                        <Play size={20} className="text-white fill-current ml-1" />
                      </div>
                    </div>

                    <div className="absolute bottom-2 left-2 bg-black/70 backdrop-blur-md px-2 py-1 rounded text-xs font-bold text-white">
                      EP {ep.number}
                    </div>
                  </div>
                  <div className="p-3">
                    <span className="block text-sm font-medium text-gray-200 truncate group-hover:text-white transition-colors" title={ep.title}>
                      {ep.title || `Episode ${ep.number}`}
                    </span>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* Recommendations */}
        {anime.recommendations?.length > 0 && (
          <div className="mt-16">
            <AnimeCarousel title="Recommended" animes={anime.recommendations} />
          </div>
        )}
      </div>

      {anime && (
        <WatchlistModal 
          isOpen={isWatchlistModalOpen} 
          onClose={() => setIsWatchlistModalOpen(false)} 
          anime={anime} 
        />
      )}
    </motion.div>
  );
}
