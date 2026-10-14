import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { Plus, Trash2, Cloud, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';
import syncManager from '../services/syncManager';
import api from '../services/api';

export default function Watchlist() {
  const [lists, setLists] = useState([{ id: 'default', name: 'My List', items: [] }]);
  const [activeListId, setActiveListId] = useState('default');
  const [isCreating, setIsCreating] = useState(false);
  const [newListName, setNewListName] = useState('');
  const [isSyncing, setIsSyncing] = useState(false);
  const [googleConnected, setGoogleConnected] = useState(false);
  const [googleUser, setGoogleUser] = useState(null);

  const loadLists = () => {
    try {
      const stored = localStorage.getItem('aniverse_watchlists');
      if (stored) {
        setLists(JSON.parse(stored));
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    loadLists();

    // Check Google Drive connection & auto-sync
    api.Sync.getGoogleStatus().then(res => {
      if (res.data?.connected) {
        setGoogleConnected(true);
        setGoogleUser(res.data.user);
        handleSync();
      }
    }).catch(() => {});
  }, []);

  const handleSync = async () => {
    setIsSyncing(true);
    try {
      const res = await syncManager.syncNow();
      if (res.success) {
        loadLists();
        toast.success('Favorites synced with Google Drive!');
      } else {
        toast.error(res.message || 'Sync failed');
      }
    } catch (err) {
      toast.error('Sync failed');
    } finally {
      setIsSyncing(false);
    }
  };

  const saveLists = (newLists) => {
    setLists(newLists);
    localStorage.setItem('aniverse_watchlists', JSON.stringify(newLists));
  };

  const handleCreateList = (e) => {
    e.preventDefault();
    if (!newListName.trim()) return;
    const newList = {
      id: Date.now().toString(),
      name: newListName.trim(),
      items: []
    };
    saveLists([...lists, newList]);
    setNewListName('');
    setIsCreating(false);
    setActiveListId(newList.id);
    toast.success('List created!');
  };

  const handleDeleteList = (id) => {
    if (lists.length === 1) {
      return toast.error('You must have at least one list.');
    }
    if (window.confirm('Are you sure you want to delete this list?')) {
      const newLists = lists.filter(l => l.id !== id);
      saveLists(newLists);
      if (activeListId === id) setActiveListId(newLists[0].id);
      toast.success('List deleted.');
    }
  };

  const handleRemoveItem = (animeId) => {
    const newLists = lists.map(l => {
      if (l.id === activeListId) {
        return { ...l, items: l.items.filter(item => item.id !== animeId) };
      }
      return l;
    });
    saveLists(newLists);
  };

  const activeList = lists.find(l => l.id === activeListId) || lists[0];

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 min-h-screen"
    >
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <div className="flex items-center gap-3">
          <h1 className="text-3xl font-bold text-white">Watchlists</h1>
          {googleConnected && (
            <button
              onClick={handleSync}
              disabled={isSyncing}
              className="flex items-center gap-2 bg-[#8B5CF6]/20 hover:bg-[#8B5CF6]/30 text-[#A78BFA] border border-[#8B5CF6]/40 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-colors"
              title="Sync with Google Drive"
            >
              <RefreshCw size={13} className={isSyncing ? "animate-spin" : ""} />
              <span>{isSyncing ? "Syncing..." : "Sync with Phone"}</span>
            </button>
          )}
          {lists.length > 1 && (
            <button onClick={() => handleDeleteList(activeListId)} className="p-2 text-red-500 hover:bg-red-500/10 rounded-full transition-colors" title="Delete current list">
              <Trash2 size={18} />
            </button>
          )}
        </div>
        
        <div className="flex overflow-x-auto hide-scrollbar gap-2 pb-2 md:pb-0 items-center">
          {lists.map(list => (
            <button
              key={list.id}
              onClick={() => setActiveListId(list.id)}
              className={`whitespace-nowrap px-4 py-2 rounded-full text-sm font-medium transition-colors ${
                activeListId === list.id 
                  ? 'bg-[#8B5CF6] text-white' 
                  : 'bg-[#1a1a2e] text-gray-400 hover:bg-[#252540] hover:text-white border border-[#2D2D44]'
              }`}
            >
              {list.name}
            </button>
          ))}
          
          {isCreating ? (
            <form onSubmit={handleCreateList} className="flex items-center gap-2">
              <input 
                type="text" 
                value={newListName}
                onChange={e => setNewListName(e.target.value)}
                placeholder="List name..."
                className="bg-[#13131a] border border-[#2D2D44] rounded-full px-3 py-1.5 text-sm text-white focus:outline-none focus:border-[#8B5CF6]"
                autoFocus
                onBlur={() => !newListName && setIsCreating(false)}
              />
              <button type="submit" className="text-sm text-green-400 hover:text-green-300 font-medium">Save</button>
            </form>
          ) : (
            <button
              onClick={() => setIsCreating(true)}
              className="flex items-center gap-1 whitespace-nowrap px-3 py-1.5 rounded-full text-sm font-medium transition-colors border border-dashed border-gray-600 text-gray-400 hover:text-white hover:border-gray-400"
            >
              <Plus size={16} /> New List
            </button>
          )}
        </div>
      </div>

      {activeList.items.length > 0 ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 xl:grid-cols-6 gap-4 md:gap-6">
          {activeList.items.map(anime => (
            <div key={anime.id} className="relative group">
              <Link to={`/anime/${anime.id}`} className="block relative overflow-hidden rounded-lg aspect-[2/3] transition-all duration-300 group-hover:scale-105 group-hover:shadow-[0_0_20px_rgba(139,92,246,0.3)] bg-[#1a1a2e]">
                <img src={anime.image} alt={anime.title} className="w-full h-full object-cover" loading="lazy" />
                <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent"></div>
                <div className="absolute bottom-0 left-0 right-0 p-3">
                  <h3 className="text-white font-medium text-sm line-clamp-2 mb-2">{anime.title}</h3>
                </div>
              </Link>
              <button 
                onClick={(e) => { e.preventDefault(); handleRemoveItem(anime.id); }}
                className="absolute top-2 right-2 bg-black/60 hover:bg-red-500/80 text-white p-1.5 rounded-full opacity-0 group-hover:opacity-100 transition-all z-10"
                title="Remove from list"
              >
                <Trash2 size={14} />
              </button>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-[#1a1a2e] rounded-xl border border-[#2D2D44]">
          <h3 className="text-xl text-white font-semibold mb-2">Empty List</h3>
          <p className="text-gray-400 mb-6">Add anime to "{activeList.name}" to track them here.</p>
          <Link to="/search" className="inline-block px-6 py-3 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white rounded-lg font-medium transition-colors">
            Browse Anime
          </Link>
        </div>
      )}
    </motion.div>
  );
}
