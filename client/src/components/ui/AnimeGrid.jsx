import AnimeCard from './AnimeCard';
import SkeletonCard from './SkeletonCard';

export default function AnimeGrid({ animes, title, loading }) {
  return (
    <div className="w-full">
      {title && <h2 className="text-xl md:text-2xl font-bold mb-6 text-white">{title}</h2>}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 xl:grid-cols-6 gap-4 md:gap-6">
        {loading
          ? Array.from({ length: 12 }).map((_, i) => <SkeletonCard key={i} />)
          : animes?.map((anime) => <AnimeCard key={anime.id || anime.aniId} anime={anime} />)
        }
      </div>
    </div>
  );
}
