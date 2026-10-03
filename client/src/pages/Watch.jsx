import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Play, SkipForward, Maximize, Settings, Heart, Star, LayoutGrid } from 'lucide-react';
import api from '../services/api';
import VideoPlayer from '../components/player/VideoPlayer';
import LoadingSpinner from '../components/ui/LoadingSpinner';

export default function Watch() {
  const { animeId, episodeId } = useParams();
  const navigate = useNavigate();
  const [episodes, setEpisodes] = useState([]);
  const [anime, setAnime] = useState(null);
  const [loading, setLoading] = useState(true);
  
  const [embedUrl, setEmbedUrl] = useState(null);
  const [skipData, setSkipData] = useState(null);
  const [servers, setServers] = useState([]);
  const [currentServerId, setCurrentServerId] = useState(null);
  const [activeTab, setActiveTab] = useState('sub');
  const [loadingServer, setLoadingServer] = useState(false);

  const [autoPlay, setAutoPlay] = useState(() => localStorage.getItem('autoPlay') !== 'false');
  const [autoNext, setAutoNext] = useState(() => localStorage.getItem('autoNext') !== 'false');
  const [autoSkip, setAutoSkip] = useState(() => localStorage.getItem('autoSkip') === 'true');

  useEffect(() => {
    const fetchStreamData = async () => {
      try {
        setLoading(true);
        // First get episodes to know the default episode ID
        const [animeRes, epRes] = await Promise.all([
          api.Anime.getAnimeDetail(animeId).catch((err) => {
            console.error('Failed to fetch anime detail:', err);
            return { data: { data: null } };
          }),
          api.Anime.getEpisodes(animeId).catch((err) => {
            console.error('Failed to fetch episodes:', err);
            return { data: { data: [] } };
          })
        ]);
        
        const animeData = animeRes.data?.data || animeRes.data;
        let epData = epRes.data?.data || epRes.data || [];
        if (!Array.isArray(epData)) epData = [];
        
        const targetEpisodeId = episodeId || (epData.length > 0 ? epData[0].id : null);
        const targetEpisode = epData.find(ep => ep.id === targetEpisodeId) || epData[0];
        
        // Then get stream sources for the target episode using dataIds
        const sourceRes = (targetEpisode && targetEpisode.dataIds)
          ? await api.Stream.getStreamingSources(targetEpisode.dataIds).catch((err) => {
              console.error('Failed to fetch stream sources:', err);
              return { data: { data: null } };
            })
          : { data: null };
        
        const streamData = sourceRes.data?.data || sourceRes.data;
        
        setAnime(animeData);
        setEpisodes(epData);

        if (streamData) {
          setEmbedUrl(streamData.embedUrl);
          setSkipData(streamData.skipData);
          const srvs = streamData.servers || [];
          setServers(srvs);
          
          const hasSub = srvs.some(s => s.type === 'sub');
          const hasDub = srvs.some(s => s.type === 'dub');
          
          const pref = localStorage.getItem('defaultAudio') || 'sub';
          let tab = 'sub';
          if (pref === 'dub' && hasDub) tab = 'dub';
          else if (hasSub) tab = 'sub';
          else if (hasDub) tab = 'dub';
          
          setActiveTab(tab);
          
          if (srvs.length > 0) {
            const firstOfTab = srvs.find(s => s.type === tab) || srvs[0];
            setCurrentServerId(firstOfTab.linkId);
          }
        } else {
          setEmbedUrl(null);
          setSkipData(null);
          setServers([]);
        }
      } catch (error) {
        console.error('Stream data fetch error:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchStreamData();
  }, [animeId, episodeId]);

  const handleServerClick = async (server) => {
    if (server.linkId === currentServerId) return;
    try {
      setLoadingServer(true);
      setCurrentServerId(server.linkId);
      const res = await api.Stream.getEmbedUrl(server.linkId);
      const data = res.data?.data || res.data;
      const newUrl = data?.url || data?.embedUrl;
      if (newUrl) {
        setEmbedUrl(newUrl);
        if (data.skipData) setSkipData(data.skipData);
      }
    } catch (err) {
      console.error('Failed to switch server:', err);
    } finally {
      setLoadingServer(false);
    }
  };

  const handleTabSwitch = (tab) => {
    setActiveTab(tab);
    const tabServers = servers.filter(s => s.type === tab);
    if (tabServers.length > 0) {
      handleServerClick(tabServers[0]);
    }
  };

  const handleToggle = (key, setter) => {
    return (e) => {
      const checked = e.target.checked;
      setter(checked);
      localStorage.setItem(key, checked);
    };
  };

  const handleNextEpisode = () => {
    if (!autoNext || !episodes.length) return;
    const currentId = episodeId || (episodes[0] ? episodes[0].id : null);
    const currentIndex = episodes.findIndex(ep => ep.id === currentId);
    if (currentIndex !== -1 && currentIndex < episodes.length - 1) {
      const nextEp = episodes[currentIndex + 1];
      navigate(`/watch/${animeId}/${nextEp.id}`);
    }
  };

  if (loading) return <div className="pt-20 h-screen"><LoadingSpinner /></div>;
  if (!anime || Object.keys(anime).length === 0) {
    return (
      <div className="pt-20 h-screen flex flex-col items-center justify-center text-gray-400">
        <h2 className="text-2xl font-bold text-white mb-2">Anime Not Found</h2>
        <p>We couldn't load the details for this anime.</p>
        <Link to="/" className="mt-4 px-4 py-2 bg-[#8B5CF6] text-white rounded hover:bg-[#7C3AED] transition">Return Home</Link>
      </div>
    );
  }

  const targetEpisodeId = episodeId || (episodes.length > 0 ? episodes[0].id : null);
  const currentEpIndex = Math.max(0, episodes.findIndex(ep => ep.id === targetEpisodeId));
  const title = anime?.title?.english || anime?.title?.romaji || 'Anime';
  const description = anime?.description?.replace(/<[^>]*>?/gm, '');
  
  const currentTabServers = servers.filter(s => s.type === activeTab);

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-[1600px] mx-auto pt-20 px-2 md:px-6 flex flex-col lg:flex-row gap-6 min-h-screen text-gray-300"
    >
      {/* Left Column: Player & Episodes */}
      <div className="flex-grow w-full lg:w-[70%] flex flex-col space-y-4">
        
        {/* Video Player Box */}
        <div className="bg-[#13131a] md:rounded-lg overflow-hidden shadow-xl border border-white/5 relative">
          <div className="w-full aspect-video bg-black relative">
            {loadingServer ? (
              <div className="w-full h-full flex items-center justify-center text-gray-500">
                <LoadingSpinner />
              </div>
            ) : embedUrl ? (
              <VideoPlayer 
                url={embedUrl} 
                isEmbed={true}
                title={`${title} - Episode ${currentEpIndex + 1}`}
                skipData={skipData}
                onEnded={handleNextEpisode}
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-gray-500">
                Stream not available
              </div>
            )}
          </div>
          
          {/* Controls Bar */}
          <div className="bg-[#1a1a2e] px-4 py-3 flex items-center justify-center sm:justify-between text-xs sm:text-sm font-medium border-t border-white/5 flex-wrap gap-3">
            <div className="flex space-x-6">
              <label className="flex items-center space-x-2 cursor-pointer hover:text-white transition-colors">
                <input type="checkbox" className="accent-[#8B5CF6]" checked={autoPlay} onChange={handleToggle('autoPlay', setAutoPlay)} />
                <span>Auto Play</span>
              </label>
              <label className="flex items-center space-x-2 cursor-pointer hover:text-white transition-colors">
                <input type="checkbox" className="accent-[#8B5CF6]" checked={autoNext} onChange={handleToggle('autoNext', setAutoNext)} />
                <span>Auto Next</span>
              </label>
              <label className="flex items-center space-x-2 cursor-pointer hover:text-white transition-colors">
                <input type="checkbox" className="accent-[#8B5CF6]" checked={autoSkip} onChange={handleToggle('autoSkip', setAutoSkip)} />
                <span>Auto Skip</span>
              </label>
            </div>
            <div className="flex space-x-6 text-gray-400">
              <button className="flex items-center space-x-1 hover:text-white transition-colors"><Star size={14} /> <span>Light</span></button>
              <button className="flex items-center space-x-1 hover:text-white transition-colors"><Heart size={14} /> <span>Add Bookmark</span></button>
            </div>
          </div>
        </div>

        {/* Server Selection */}
        <div className="bg-[#13131a] p-5 rounded-lg border border-white/5 shadow-md flex flex-col items-center">
          <p className="text-sm mb-4 text-center font-inter">
            You're watching <span className="font-bold text-white">Episode {currentEpIndex + 1}</span>.<br/>
            <span className="text-gray-500 text-xs">If current server doesn't work, please try other servers below.</span>
          </p>
          
          <div className="w-full max-w-2xl flex flex-col gap-3 font-space-mono">
            {/* Sub/Dub Tabs */}
            <div className="flex gap-4 justify-center mb-2">
              {servers.some(s => s.type === 'sub') && (
                <button 
                  onClick={() => handleTabSwitch('sub')}
                  className={`px-4 py-1.5 rounded text-sm font-bold uppercase tracking-wider transition-colors ${activeTab === 'sub' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1f1f2e] text-gray-400 hover:text-white'}`}
                >
                  Sub
                </button>
              )}
              {servers.some(s => s.type === 'dub') && (
                <button 
                  onClick={() => handleTabSwitch('dub')}
                  className={`px-4 py-1.5 rounded text-sm font-bold uppercase tracking-wider transition-colors ${activeTab === 'dub' ? 'bg-[#8B5CF6] text-white' : 'bg-[#1f1f2e] text-gray-400 hover:text-white'}`}
                >
                  Dub
                </button>
              )}
            </div>
            
            <div className="flex flex-wrap items-center justify-center gap-2">
              {currentTabServers.length > 0 ? currentTabServers.map((server, idx) => (
                <button
                  key={idx}
                  onClick={() => handleServerClick(server)}
                  className={`px-4 py-1.5 rounded text-sm font-medium transition-colors ${
                    currentServerId === server.linkId 
                      ? 'bg-[#8B5CF6] text-white' 
                      : 'bg-[#252540] hover:bg-[#2D2D44] text-gray-300'
                  }`}
                >
                  {server.name || `Server ${idx + 1}`}
                </button>
              )) : (
                <span className="text-sm text-gray-500">No servers available for {activeTab}</span>
              )}
            </div>
          </div>
        </div>

        {/* Episodes Grid Box */}
        <div className="bg-[#13131a] rounded-lg border border-white/5 shadow-md overflow-hidden font-space-mono">
          <div className="bg-[#0a0a0f] px-4 py-3 flex items-center justify-between border-b border-white/5">
            <select className="bg-[#1f1f2e] border border-white/10 text-white text-sm rounded px-3 py-1.5 outline-none focus:border-[#8B5CF6]">
              <option>001-{(episodes.length).toString().padStart(3, '0')}</option>
            </select>
            <div className="flex items-center gap-2 text-gray-400">
              <button className="p-1 hover:text-white transition-colors bg-[#1f1f2e] rounded"><LayoutGrid size={16} /></button>
            </div>
          </div>
          <div className="p-4">
            <div className="grid grid-cols-5 sm:grid-cols-8 md:grid-cols-10 lg:grid-cols-12 gap-2">
              {episodes.map((ep) => {
                const isActive = ep.id === targetEpisodeId;
                return (
                  <Link
                    key={ep.id}
                    to={`/watch/${animeId}/${ep.id}`}
                    className={`flex items-center justify-center py-2 text-sm font-medium rounded transition-all ${
                      isActive 
                        ? 'bg-[#8B5CF6] text-white shadow-[0_0_10px_rgba(139,92,246,0.3)]' 
                        : 'bg-[#1f1f2e] text-gray-400 hover:bg-[#2D2D44] hover:text-white'
                    }`}
                  >
                    {ep.number}
                  </Link>
                );
              })}
            </div>
          </div>
        </div>
        
      </div>

      {/* Right Column: Anime Details Sidebar */}
      <div className="w-full lg:w-[30%] flex flex-col space-y-4">
        <div className="bg-[#13131a] p-4 rounded-lg border border-white/5 shadow-md flex flex-col font-inter">
          <div className="flex gap-4 mb-4">
            <img 
              src={anime?.coverImage?.extraLarge} 
              alt={title}
              className="w-28 h-40 object-cover rounded-md shadow-lg"
            />
            <div className="flex flex-col">
              <h2 className="text-lg font-bold text-white line-clamp-3 leading-tight mb-2">{title}</h2>
              <div className="flex gap-2 text-xs font-bold font-space-mono">
                <span className="bg-white/10 px-1.5 py-0.5 rounded text-gray-300">HD</span>
                <span className="bg-white/10 px-1.5 py-0.5 rounded text-gray-300">TV-14</span>
              </div>
            </div>
          </div>
          
          <div className="text-sm text-gray-400 mb-4 line-clamp-4 leading-relaxed">
            {description}
          </div>
          
          <div className="space-y-2 text-sm">
            <div className="flex"><span className="w-24 text-gray-500 font-medium">Type:</span> <span className="text-gray-300">{anime?.format || 'TV'}</span></div>
            <div className="flex"><span className="w-24 text-gray-500 font-medium">Country:</span> <span className="text-gray-300">Japan</span></div>
            <div className="flex"><span className="w-24 text-gray-500 font-medium">Premiered:</span> <span className="text-gray-300">{anime?.season} {anime?.seasonYear}</span></div>
            <div className="flex"><span className="w-24 text-gray-500 font-medium">Date aired:</span> <span className="text-gray-300">{anime?.seasonYear || '?'}</span></div>
            <div className="flex"><span className="w-24 text-gray-500 font-medium">Status:</span> <span className="text-gray-300">{anime?.status || 'Finished Airing'}</span></div>
            <div className="flex"><span className="w-24 text-gray-500 font-medium">Duration:</span> <span className="text-gray-300">{anime?.duration ? `${anime.duration} min/ep` : '?'}</span></div>
            <div className="flex"><span className="w-24 text-gray-500 font-medium">Episodes:</span> <span className="text-gray-300">{anime?.episodes || '?'}</span></div>
            <div className="flex"><span className="w-24 text-gray-500 font-medium">Studios:</span> <span className="text-gray-300 line-clamp-1">{anime?.studios?.nodes?.[0]?.name || '?'}</span></div>
            <div className="flex"><span className="w-24 text-gray-500 font-medium">Genre:</span> <span className="text-gray-300 line-clamp-2">{anime?.genres?.join(', ') || '?'}</span></div>
          </div>
          
          <div className="mt-6 pt-4 border-t border-white/5 flex flex-col items-center">
            <div className="flex text-yellow-500 gap-1 mb-1">
              {[1, 2, 3, 4, 5].map(i => <Star key={i} size={18} className={i <= Math.round((anime?.averageScore || 0) / 20) ? 'fill-current' : 'text-gray-600'} />)}
            </div>
            <span className="text-xs text-gray-400 font-space-mono">
              {(anime?.averageScore / 10).toFixed(1)} / 10
            </span>
          </div>
        </div>
      </div>
      
    </motion.div>
  );
}
