import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Check } from 'lucide-react';
import toast from 'react-hot-toast';

export default function WatchlistModal({ isOpen, onClose, anime }) {
  const [lists, setLists] = useState([]);

  useEffect(() => {
    if (isOpen) {
      try {
        const stored = localStorage.getItem('aniverse_watchlists');
        if (stored) {
          setLists(JSON.parse(stored));
        } else {
          const defaultList = [{ id: 'default', name: 'My List', items: [] }];
          setLists(defaultList);
          localStorage.setItem('aniverse_watchlists', JSON.stringify(defaultList));
        }
      } catch (e) {}
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const toggleList = (listId) => {
    const newLists = lists.map(list => {
      if (list.id === listId) {
        const exists = list.items.some(i => i.id === anime.id);
        if (exists) {
          return { ...list, items: list.items.filter(i => i.id !== anime.id) };
        } else {
          return { ...list, items: [...list.items, {
            id: anime.id,
            title: anime.title?.english || anime.title?.romaji,
            image: anime.coverImage?.extraLarge || anime.coverImage?.large
          }] };
        }
      }
      return list;
    });
    setLists(newLists);
    localStorage.setItem('aniverse_watchlists', JSON.stringify(newLists));
    toast.success('Watchlist updated!');
  };

  return (
    <AnimatePresence>
      <motion.div 
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4"
        onClick={onClose}
      >
        <motion.div 
          initial={{ scale: 0.95, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.95, opacity: 0 }}
          onClick={e => e.stopPropagation()}
          className="bg-[#1a1a2e] border border-[#2D2D44] rounded-xl w-full max-w-sm overflow-hidden"
        >
          <div className="flex justify-between items-center p-4 border-b border-[#2D2D44]">
            <h3 className="text-white font-bold text-lg">Save to...</h3>
            <button onClick={onClose} className="text-gray-400 hover:text-white transition-colors">
              <X size={20} />
            </button>
          </div>
          <div className="p-2 max-h-64 overflow-y-auto">
            {lists.map(list => {
              const inList = list.items.some(i => i.id === anime.id);
              return (
                <button 
                  key={list.id}
                  onClick={() => toggleList(list.id)}
                  className="w-full flex items-center justify-between p-3 rounded-lg hover:bg-[#252540] transition-colors text-left group"
                >
                  <span className="text-gray-200 group-hover:text-white font-medium">{list.name}</span>
                  {inList && <Check size={18} className="text-[#8B5CF6]" />}
                </button>
              );
            })}
          </div>
          <div className="p-4 border-t border-[#2D2D44] bg-[#13131a]">
            <p className="text-xs text-gray-500 text-center">Manage your lists in the Watchlist tab.</p>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
