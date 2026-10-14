import { Link, useLocation } from 'react-router-dom';
import { Search, History, Settings, Tv, Film, Sparkles } from 'lucide-react';
import { useMode, MODES } from '../../context/ModeContext';

export default function Navbar() {
  const location = useLocation();
  const isActive = (path) => location.pathname === path;
  const { mode, setMode } = useMode();

  return (
    <nav className="fixed top-0 w-full z-50 bg-[#0a0a0f]/80 backdrop-blur-md border-b border-white/5 transition-all duration-300 font-inter">
      <div className="max-w-[1600px] mx-auto px-4 md:px-8 h-16 flex items-center justify-between">
        
        {/* Left Side: Logo & Mode Switcher */}
        <div className="flex items-center gap-6 md:gap-8">
          <Link to="/" className="flex items-center gap-2">
            <span className="text-2xl font-bold tracking-widest text-[#8B5CF6] font-space-mono">ANIVERSE</span>
          </Link>

          {/* Mode Switcher Pill */}
          <div className="flex items-center bg-[#13131a] p-1 rounded-xl border border-white/10">
            <button
              onClick={() => setMode(MODES.ANIME)}
              className={`flex items-center gap-1.5 px-3 py-1 rounded-lg text-xs font-bold transition-all ${
                mode === MODES.ANIME
                  ? 'bg-[#8B5CF6] text-white shadow-md shadow-[#8B5CF6]/30'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              <Sparkles size={13} />
              <span>Anime</span>
            </button>

            <button
              onClick={() => setMode(MODES.KDRAMA)}
              className={`flex items-center gap-1.5 px-3 py-1 rounded-lg text-xs font-bold transition-all ${
                mode === MODES.KDRAMA
                  ? 'bg-[#EC4899] text-white shadow-md shadow-[#EC4899]/30'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              <Tv size={13} />
              <span>K-Drama</span>
            </button>

            <button
              onClick={() => setMode(MODES.HOLLYWOOD)}
              className={`flex items-center gap-1.5 px-3 py-1 rounded-lg text-xs font-bold transition-all ${
                mode === MODES.HOLLYWOOD
                  ? 'bg-[#06B6D4] text-white shadow-md shadow-[#06B6D4]/30'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              <Film size={13} />
              <span>Hollywood</span>
            </button>
          </div>
          
          <div className="hidden lg:flex items-center gap-6">
            <Link to="/" className={`text-sm font-medium transition-colors ${isActive('/') ? 'text-white font-semibold' : 'text-gray-400 hover:text-white'}`}>
              Home
            </Link>
            <Link to="/search" className={`text-sm font-medium transition-colors ${isActive('/search') ? 'text-white font-semibold' : 'text-gray-400 hover:text-white'}`}>
              Browse
            </Link>
            <Link to="/watchlist" className={`text-sm font-medium transition-colors ${isActive('/watchlist') ? 'text-white font-semibold' : 'text-gray-400 hover:text-white'}`}>
              Watchlists
            </Link>
            <Link to="/history" className={`text-sm font-medium transition-colors ${isActive('/history') ? 'text-white font-semibold' : 'text-gray-400 hover:text-white'}`}>
              History
            </Link>
          </div>
        </div>

        {/* Right Side: Search & Settings */}
        <div className="flex items-center gap-6">
          <Link to="/search" className="text-gray-400 hover:text-white transition-colors">
            <Search size={20} />
          </Link>

          <div className="flex items-center gap-4">
            <Link to="/profile" className={`flex items-center gap-2 text-sm font-medium transition-colors ${isActive('/profile') ? 'text-[#8B5CF6]' : 'text-gray-400 hover:text-white'}`}>
              <Settings size={18} />
              <span className="hidden sm:inline">Settings</span>
            </Link>
          </div>
        </div>
        
      </div>
    </nav>
  );
}
