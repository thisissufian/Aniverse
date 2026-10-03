import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Play, Plus } from 'lucide-react';
import WatchlistModal from './WatchlistModal';

export default function AnimeCard({ anime }) {
  const [isWatchlistModalOpen, setIsWatchlistModalOpen] = useState(false);
  const title = anime.title?.english || anime.title?.romaji;
  const eps = anime.episodes || '?';
  const format = anime.format || 'TV';

  return (
    <Link to={`/watch/${anime.id}`} className="group flex flex-col gap-2 w-full">
      <div className="relative aspect-[2/3] rounded overflow-hidden bg-[#13131a] border border-white/5 shadow-md">
        <img
          src={anime.coverImage?.extraLarge || anime.coverImage?.large}
          alt={title}
          className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
          loading="lazy"
        />
        
        {/* Top right badges */}
        <div className="absolute top-2 right-2 flex flex-col gap-1 items-end z-20">
          {anime.averageScore && (
            <div className="bg-[#8B5CF6] text-white text-[11px] font-bold px-1.5 py-0.5 rounded shadow-sm">
              {(anime.averageScore / 10).toFixed(1)}
            </div>
          )}
        </div>

        {/* Bottom badges */}
        <div className="absolute bottom-2 left-2 flex gap-1 z-20">
          <div className="bg-[#EAEAEA] text-black text-[11px] font-bold px-1.5 py-0.5 rounded-sm flex items-center shadow-sm">
            SUB {eps}
          </div>
          <div className="bg-[#8B5CF6] text-white text-[11px] font-bold px-1.5 py-0.5 rounded-sm flex items-center shadow-sm">
            {format}
          </div>
        </div>

        {/* Gradient */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent pointer-events-none z-10" />
        
        {/* Hover Overlay */}
        <div className="absolute inset-0 z-20 flex flex-col items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-black/50 backdrop-blur-[2px]">
          <Play size={48} className="text-white drop-shadow-lg fill-white/80 mb-4" />
          <button 
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              setIsWatchlistModalOpen(true);
            }}
            className="flex items-center gap-1 bg-[#252540]/90 hover:bg-[#8B5CF6] text-white text-xs font-semibold py-1.5 px-3 rounded-full transition-colors backdrop-blur-md"
          >
            <Plus size={14} /> Add to List
          </button>
        </div>
      </div>
      
      {/* Info */}
      <div className="flex flex-col">
        <h3 className="text-white text-[14px] font-medium line-clamp-1 group-hover:text-[#8B5CF6] transition-colors font-inter">
          {title}
        </h3>
        <span className="text-gray-400 text-xs font-inter mt-0.5">
          {anime.seasonYear ? `${anime.seasonYear} • ` : ''}{anime.status?.toLowerCase().replace('_', ' ') || ''}
        </span>
      </div>
      
      {isWatchlistModalOpen && (
        <WatchlistModal 
          isOpen={isWatchlistModalOpen} 
          onClose={() => setIsWatchlistModalOpen(false)} 
          anime={anime} 
        />
      )}
    </Link>
  );
}
