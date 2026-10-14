import { useState, useEffect } from 'react';
import { useInfiniteQuery, useQuery } from '@tanstack/react-query';
import { useNavigate, Link } from 'react-router-dom';
import { Carousel } from 'react-responsive-carousel';
import 'react-responsive-carousel/lib/styles/carousel.min.css';
import InfiniteScroll from 'react-infinite-scroll-component';
import parse from 'html-react-parser';
import { StarIcon, VideoIcon } from '@radix-ui/react-icons';
import { Spinner } from '@radix-ui/themes';
import { ChevronRight, Play, Film, Tv, Sparkles } from 'lucide-react';

import api from '../services/api';
import AnimeCard from '../components/ui/AnimeCard';
import { useMode, MODES } from '../context/ModeContext';

const getCurrentSeason = () => {
  const month = new Date().getMonth();
  if (month >= 0 && month <= 2) return 'WINTER';
  if (month >= 3 && month <= 5) return 'SPRING';
  if (month >= 6 && month <= 8) return 'SUMMER';
  return 'FALL';
};

export default function Home() {
  const navigate = useNavigate();
  const currentYear = new Date().getFullYear();
  const currentSeason = getCurrentSeason();
  const { mode, config } = useMode();

  // === ANIME MODE DATA ===
  const { data: topAiringRes } = useQuery({
    queryKey: ['top_airing'],
    enabled: mode === MODES.ANIME,
    queryFn: () => api.Anime.searchAnime({ 
      sort: 'POPULARITY_DESC', 
      status: 'RELEASING',
      season: currentSeason,
      year: currentYear,
      page: 1 
    }).then(res => res.data?.data?.media || [])
  });

  const [gridFilter, setGridFilter] = useState('ALL');
  const [topFilter, setTopFilter] = useState('Today');

  const {
    data: trendingPages,
    fetchNextPage: fetchNextTrending,
    hasNextPage: hasNextTrending,
  } = useInfiniteQuery({
    queryKey: ['recent_episodes', gridFilter],
    enabled: mode === MODES.ANIME,
    queryFn: ({ pageParam = 1 }) => 
      api.Anime.getRecentEpisodes({ page: pageParam, perPage: 20, filter: gridFilter })
        .then(res => res.data?.data?.media || []),
    initialPageParam: 1,
    getNextPageParam: (lastPage, allPages) => {
      if (!lastPage || lastPage.length === 0) return undefined;
      return allPages.length + 1;
    },
  });

  const { data: topAnimeRes } = useQuery({
    queryKey: ['top_anime_sidebar', topFilter],
    enabled: mode === MODES.ANIME,
    queryFn: () => {
      let sortParams = { sort: 'SCORE_DESC' };
      if (topFilter === 'Today') sortParams = { sort: 'TRENDING_DESC', status: 'RELEASING' };
      if (topFilter === 'Week') sortParams = { sort: 'POPULARITY_DESC', status: 'RELEASING' };
      if (topFilter === 'Month') sortParams = { sort: 'POPULARITY_DESC' };
      
      return api.Anime.searchAnime({ 
        ...sortParams,
        page: 1,
        perPage: 10
      }).then(res => res.data?.data?.media || []);
    }
  });

  // === KDRAMA & HOLLYWOOD MODE DATA ===
  const { data: mediaHomeRes, isLoading: loadingMedia } = useQuery({
    queryKey: ['media_home', mode],
    enabled: mode !== MODES.ANIME,
    queryFn: () => api.Media.getHome(mode).then(res => res.data?.data || null),
  });

  // Active dataset selection
  const topAiringAnime = mode === MODES.ANIME
    ? (topAiringRes || [])
    : (mediaHomeRes?.trending || []);

  const trendingAnime = mode === MODES.ANIME
    ? (trendingPages ? trendingPages.pages.flat() : [])
    : (mediaHomeRes?.trending || []);

  const topSidebarAnime = mode === MODES.ANIME
    ? (topAnimeRes || [])
    : (mode === MODES.KDRAMA ? (mediaHomeRes?.popularMovies || []) : (mediaHomeRes?.popularMovies || []));

  const sidebarTitle = mode === MODES.ANIME
    ? 'Top Anime'
    : (mode === MODES.KDRAMA ? 'Popular K-Movies' : 'Popular Movies');

  const mainSectionTitle = mode === MODES.ANIME
    ? 'Latest Episodes'
    : (mode === MODES.KDRAMA ? 'Trending K-Dramas' : 'Trending Movies & TV');

  return (
    <div className="min-h-screen bg-[#0a0a0f] text-gray-200 font-inter pb-20 pt-16">
      
      {/* Mode Indicator Banner */}
      <div className="max-w-[1600px] mx-auto px-4 md:px-8 pt-4 pb-2 flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs font-semibold text-gray-400">
          <span className="w-2 h-2 rounded-full" style={{ backgroundColor: config.accentColor }}></span>
          <span>Browsing {config.title}</span>
          <span className="text-gray-600">•</span>
          <span className="text-gray-500 hidden sm:inline">{config.tagline}</span>
        </div>
      </div>

      {/* Hero Carousel */}
      {topAiringAnime.length > 0 && (
        <div className="w-full relative mb-10 max-w-[1920px] mx-auto px-0 md:px-4 lg:px-8">
          <Carousel
            axis="horizontal"
            showArrows={true}
            showThumbs={false}
            showStatus={false}
            autoPlay
            interval={6000}
            infiniteLoop
            renderIndicator={false}
            emulateTouch
            className="w-full md:rounded-xl overflow-hidden shadow-2xl border-0 md:border border-white/5"
          >
            {topAiringAnime
              .filter(item => item.bannerImage || item.coverImage?.extraLarge)
              .slice(0, 10)
              .map((item) => {
                const titleText = item.title?.english || item.title?.romaji || item.name;
                const targetUrl = item.type
                  ? `/media/${item.type}/${item.id}`
                  : `/watch/${item.id}`;

                return (
                  <div
                    key={item.id}
                    className="relative h-[350px] md:h-[450px] lg:h-[500px] w-full cursor-pointer group"
                    onClick={() => navigate(targetUrl)}
                  >
                    <img
                      src={item.bannerImage || item.coverImage?.extraLarge}
                      alt={titleText}
                      className="absolute inset-0 w-full h-full object-cover"
                    />
                    
                    <div className="absolute inset-0 w-full md:w-[70%] bg-gradient-to-r from-[#0a0a0f] via-[#0a0a0f]/90 to-transparent z-10 pointer-events-none"></div>
                    <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0f] via-transparent to-transparent z-10 pointer-events-none"></div>
                    
                    <div className="absolute bottom-8 md:bottom-12 left-4 md:left-12 z-20 flex flex-col items-start justify-end w-[90%] md:w-[60%] text-left">
                      <h2 className="text-3xl md:text-5xl font-bold text-white line-clamp-2 mb-3 drop-shadow-lg">
                        {titleText}
                      </h2>
                      
                      <div className="flex flex-wrap gap-2 mb-4 text-[11px] md:text-xs font-bold text-white/90">
                         <span className="px-2 py-1 rounded flex items-center gap-1 text-white shadow-sm" style={{ backgroundColor: config.accentColor }}>
                           <VideoIcon /> {item.format || (item.type === 'tv' ? 'TV' : 'MOVIE')}
                         </span>
                         {item.averageScore && (
                           <span className="bg-white/10 px-2 py-1 rounded backdrop-blur-md flex items-center gap-1 border border-white/10">
                             <StarIcon className="text-yellow-400" /> {(item.averageScore/10).toFixed(1)}
                           </span>
                         )}
                         {item.year && (
                           <span className="bg-white/10 px-2 py-1 rounded backdrop-blur-md border border-white/10">
                             {item.year}
                           </span>
                         )}
                      </div>

                      {item.overview && (
                        <div className="text-xs md:text-sm text-gray-300 line-clamp-3 leading-relaxed mb-6 hidden sm:block max-w-2xl">
                          {item.overview}
                        </div>
                      )}
                      
                      <button className="text-white px-5 md:px-6 py-2 md:py-2.5 rounded-full flex items-center gap-2 font-semibold transition-transform hover:scale-105 text-sm md:text-base shadow-lg" style={{ backgroundColor: config.accentColor }}>
                        <Play fill="currentColor" size={18} /> Watch Now
                      </button>
                    </div>
                  </div>
                );
              })}
          </Carousel>
        </div>
      )}

      {/* Main Content: 2 Columns */}
      <div className="max-w-[1600px] mx-auto px-4 md:px-8 flex flex-col lg:flex-row gap-8">
        
        {/* Left Column: Trending Grid */}
        <div className="flex-1 w-full min-w-0">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6 border-b border-white/10 pb-3 gap-4">
            <h3 className="text-2xl font-bold text-white flex items-center gap-2 cursor-pointer hover:text-white transition-colors" onClick={() => navigate('/search')}>
              {mainSectionTitle} <ChevronRight className="w-6 h-6" style={{ color: config.accentColor }} />
            </h3>

            {mode === MODES.ANIME ? (
              <div className="flex gap-2 text-xs font-semibold overflow-x-auto pb-1 sm:pb-0">
                 <button onClick={() => setGridFilter('ALL')} className={`px-3 py-1.5 rounded-md whitespace-nowrap transition-colors ${gridFilter === 'ALL' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1a1a2e] text-gray-400 hover:text-white'}`}>All</button>
                 <button onClick={() => setGridFilter('SUB')} className={`px-3 py-1.5 rounded-md whitespace-nowrap transition-colors ${gridFilter === 'SUB' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1a1a2e] text-gray-400 hover:text-white'}`}>Sub</button>
                 <button onClick={() => setGridFilter('DUB')} className={`px-3 py-1.5 rounded-md whitespace-nowrap transition-colors ${gridFilter === 'DUB' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1a1a2e] text-gray-400 hover:text-white'}`}>Dub</button>
                 <button onClick={() => setGridFilter('CHINESE')} className={`px-3 py-1.5 rounded-md whitespace-nowrap transition-colors ${gridFilter === 'CHINESE' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1a1a2e] text-gray-400 hover:text-white'}`}>Chinese</button>
                 <button onClick={() => setGridFilter('TRENDING')} className={`px-3 py-1.5 rounded-md whitespace-nowrap transition-colors ${gridFilter === 'TRENDING' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1a1a2e] text-gray-400 hover:text-white'}`}>Trending</button>
              </div>
            ) : (
              <Link to="/search" className="text-xs font-bold hover:underline" style={{ color: config.accentColor }}>
                Explore All &rarr;
              </Link>
            )}
          </div>

          {mode === MODES.ANIME ? (
            <InfiniteScroll
              dataLength={trendingAnime.length}
              next={fetchNextTrending}
              hasMore={hasNextTrending}
              loader={
                <div className="flex justify-center items-center py-10 gap-3 w-full">
                  <Spinner size="3" /> <span className="text-gray-400">Loading more...</span>
                </div>
              }
              style={{ overflow: 'visible' }}
            >
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4 xl:grid-cols-5 gap-4 gap-y-8">
                {trendingAnime.map(anime => (
                  <AnimeCard key={anime.id} anime={anime} />
                ))}
              </div>
            </InfiniteScroll>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4 xl:grid-cols-5 gap-4 gap-y-8">
              {trendingAnime.map(item => (
                <AnimeCard key={item.id} anime={item} />
              ))}
            </div>
          )}
        </div>

        {/* Right Column: Sidebar */}
        <div className="w-full lg:w-[320px] xl:w-[350px] shrink-0 mt-8 lg:mt-0">
           <div className="mb-6 border-b border-white/10 pb-3 flex items-center justify-between">
              <h3 className="text-xl font-bold text-white flex items-center gap-2 cursor-pointer transition-colors" onClick={() => navigate('/search')}>
                 {sidebarTitle} <ChevronRight className="w-5 h-5" style={{ color: config.accentColor }} />
              </h3>
              {mode === MODES.ANIME && (
                <div className="flex gap-1 bg-[#1a1a2e] rounded-md p-1">
                   <button onClick={() => setTopFilter('Today')} className={`px-2 py-1 text-[11px] font-semibold rounded transition-colors ${topFilter === 'Today' ? 'bg-[#8B5CF6] text-white' : 'text-gray-400 hover:text-white'}`}>Today</button>
                   <button onClick={() => setTopFilter('Week')} className={`px-2 py-1 text-[11px] font-semibold rounded transition-colors ${topFilter === 'Week' ? 'bg-[#8B5CF6] text-white' : 'text-gray-400 hover:text-white'}`}>Week</button>
                   <button onClick={() => setTopFilter('Month')} className={`px-2 py-1 text-[11px] font-semibold rounded transition-colors ${topFilter === 'Month' ? 'bg-[#8B5CF6] text-white' : 'text-gray-400 hover:text-white'}`}>Month</button>
                </div>
              )}
           </div>
           
           <div className="flex flex-col gap-4 bg-[#13131a] p-4 rounded-lg border border-white/5 shadow-lg">
             {topSidebarAnime.map((item, index) => {
               const targetUrl = item.type
                 ? `/media/${item.type}/${item.id}`
                 : `/watch/${item.id}`;
               const titleText = item.title?.english || item.title?.romaji || item.name;

               return (
                <Link to={targetUrl} key={item.id} className="flex gap-4 group items-center py-1">
                   <div className="w-7 text-center font-bold text-xl text-gray-600 group-hover:text-white transition-colors shrink-0">
                     {index + 1}
                   </div>
                   <div className="w-14 h-20 shrink-0 overflow-hidden rounded relative bg-black">
                     <img
                       src={item.coverImage?.large || item.coverImage?.extraLarge}
                       className="w-full h-full object-cover group-hover:scale-110 transition-transform"
                       alt={titleText}
                       loading="lazy"
                     />
                   </div>
                   <div className="flex flex-col flex-1 min-w-0 justify-center">
                      <h4 className="text-white text-[13px] font-medium line-clamp-2 group-hover:text-white transition-colors leading-snug">
                        {titleText}
                      </h4>
                      <div className="flex items-center gap-2 mt-1.5 text-[11px] text-gray-400 font-medium">
                         {item.averageScore && (
                           <span className="flex items-center gap-1 text-yellow-500">
                             <StarIcon className="w-3 h-3" /> {(item.averageScore/10).toFixed(1)}
                           </span>
                         )}
                         <span className="w-1 h-1 bg-gray-600 rounded-full"></span>
                         <span>{item.format || (item.type === 'tv' ? 'TV' : 'MOVIE')}</span>
                         {item.year && (
                           <>
                             <span className="w-1 h-1 bg-gray-600 rounded-full"></span>
                             <span>{item.year}</span>
                           </>
                         )}
                      </div>
                   </div>
                </Link>
               );
             })}
           </div>
        </div>

      </div>
    </div>
  );
}
