import { useRef } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import AnimeCard from './AnimeCard';
import SkeletonCard from './SkeletonCard';

export default function AnimeCarousel({ animes, title, loading }) {
  const scrollRef = useRef(null);

  const scroll = (direction) => {
    if (scrollRef.current) {
      const { scrollLeft, clientWidth } = scrollRef.current;
      const scrollTo = direction === 'left' ? scrollLeft - clientWidth : scrollLeft + clientWidth;
      scrollRef.current.scrollTo({ left: scrollTo, behavior: 'smooth' });
    }
  };

  return (
    <div className="relative py-4">
      {title && <h2 className="text-xl md:text-2xl font-bold mb-4 text-white px-4 md:px-8">{title}</h2>}
      
      <div className="group relative">
        <button 
          onClick={() => scroll('left')}
          className="absolute left-0 top-1/2 -translate-y-1/2 z-10 bg-black/50 hover:bg-[#8B5CF6]/80 text-white p-2 md:p-3 rounded-r-lg opacity-0 group-hover:opacity-100 transition-all disabled:opacity-0 hidden md:block"
        >
          <ChevronLeft size={24} />
        </button>
        
        <div 
          ref={scrollRef}
          className="flex overflow-x-auto gap-4 px-4 md:px-8 snap-x snap-mandatory hide-scrollbar pb-4"
        >
          {loading
            ? Array.from({ length: 8 }).map((_, i) => (
                <div key={i} className="flex-none w-36 md:w-48 lg:w-56 snap-start">
                  <SkeletonCard />
                </div>
              ))
            : animes?.map((anime) => (
                <div key={anime.id || anime.aniId} className="flex-none w-36 md:w-48 lg:w-56 snap-start">
                  <AnimeCard anime={anime} />
                </div>
              ))
          }
        </div>

        <button 
          onClick={() => scroll('right')}
          className="absolute right-0 top-1/2 -translate-y-1/2 z-10 bg-black/50 hover:bg-[#8B5CF6]/80 text-white p-2 md:p-3 rounded-l-lg opacity-0 group-hover:opacity-100 transition-all hidden md:block"
        >
          <ChevronRight size={24} />
        </button>
      </div>
    </div>
  );
}
