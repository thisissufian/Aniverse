import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Play, Plus, Star } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function HeroBanner({ animes = [] }) {
  const [currentIndex, setCurrentIndex] = useState(0);

  useEffect(() => {
    if (!animes || animes.length === 0) return;
    const timer = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % Math.min(animes.length, 5));
    }, 8000);
    return () => clearInterval(timer);
  }, [animes]);

  if (!animes || animes.length === 0) {
    return <div className="w-full h-[60vh] bg-[#1a1a2e] animate-pulse"></div>;
  }

  const anime = animes[currentIndex];
  const title = anime.title?.english || anime.title?.romaji;
  const description = anime.description?.replace(/<[^>]*>?/gm, '') || 'No description available.';

  return (
    <div className="relative w-full h-[70vh] md:h-[80vh] overflow-hidden">
      <AnimatePresence mode="wait">
        <motion.div
          key={currentIndex}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 1 }}
          className="absolute inset-0"
        >
          <img 
            src={anime.coverImage?.extraLarge || anime.bannerImage || anime.image} 
            alt={title} 
            className="w-full h-full object-cover"
          />
          <div className="absolute inset-0 bg-gradient-to-r from-black via-black/70 to-transparent"></div>
          <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0f] via-transparent to-transparent"></div>
        </motion.div>
      </AnimatePresence>

      <div className="absolute inset-0 flex items-center max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="w-full md:w-3/4 lg:w-2/3 space-y-4 md:space-y-6 z-10">
          <motion.div
            key={`content-${currentIndex}`}
            initial={{ y: 30, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.2, duration: 0.6, ease: "easeOut" }}
          >
            <div className="flex items-center space-x-4 mb-3">
              {anime.averageScore && (
                <div className="flex items-center bg-black/40 backdrop-blur-md px-3 py-1 rounded-full border border-white/10">
                  <Star size={16} className="text-yellow-400 fill-current mr-1.5" />
                  <span className="font-bold text-white text-sm">{(anime.averageScore / 10).toFixed(1)}</span>
                </div>
              )}
              {anime.format && (
                <span className="text-gray-300 font-medium text-sm bg-black/40 backdrop-blur-md px-3 py-1 rounded-full border border-white/10">{anime.format}</span>
              )}
              {anime.status && (
                <span className="text-[#A78BFA] font-bold text-sm bg-[#8B5CF6]/20 backdrop-blur-md px-3 py-1 rounded-full border border-[#8B5CF6]/30 uppercase tracking-wide">{anime.status.replace(/_/g, ' ')}</span>
              )}
            </div>

            <h1 className="text-4xl md:text-5xl lg:text-7xl font-extrabold text-white leading-[1.1] drop-shadow-lg line-clamp-2 mb-4">
              {title}
            </h1>

            <div className="flex flex-wrap gap-2 mt-4">
              {anime.genres?.slice(0, 4).map((genre) => (
                <span key={genre} className="px-3 py-1.5 bg-white/5 hover:bg-white/10 backdrop-blur-md border border-white/10 rounded-lg text-sm text-gray-200 transition-colors cursor-default">
                  {genre}
                </span>
              ))}
            </div>

            <p className="text-gray-300/90 mt-6 line-clamp-3 text-sm md:text-base lg:text-lg max-w-2xl leading-relaxed drop-shadow-md">
              {description}
            </p>

            <div className="flex items-center space-x-4 mt-8">
              <Link 
                to={`/watch/${anime.id || anime.aniId}`}
                className="group flex items-center space-x-2 bg-white text-black hover:bg-gray-200 px-8 py-3.5 rounded-xl font-bold transition-all transform hover:scale-105 shadow-[0_0_20px_rgba(255,255,255,0.3)]"
              >
                <Play size={22} className="fill-current group-hover:text-[#8B5CF6] transition-colors" />
                <span>Play Now</span>
              </Link>
              <button className="flex items-center space-x-2 bg-black/40 hover:bg-black/60 backdrop-blur-md border border-white/20 text-white px-8 py-3.5 rounded-xl font-bold transition-all">
                <Plus size={22} />
                <span>More Info</span>
              </button>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
