import { Link, useLocation } from 'react-router-dom';
import { Search, History, Settings } from 'lucide-react';

export default function Navbar() {
  const location = useLocation();
  const isActive = (path) => location.pathname === path;

  return (
    <nav className="fixed top-0 w-full z-50 bg-[#0a0a0f]/80 backdrop-blur-md border-b border-white/5 transition-all duration-300 font-inter">
      <div className="max-w-[1600px] mx-auto px-4 md:px-8 h-16 flex items-center justify-between">
        
        {/* Left Side: Logo & Links */}
        <div className="flex items-center gap-10">
          <Link to="/" className="flex items-center gap-2">
            <span className="text-2xl font-bold tracking-widest text-[#8B5CF6] font-space-mono">ANIVERSE</span>
          </Link>
          
          <div className="hidden md:flex items-center gap-6">
            <Link to="/" className={`text-sm font-medium transition-colors ${isActive('/') ? 'text-white' : 'text-gray-400 hover:text-white'}`}>
              Home
            </Link>
            <Link to="/search" className={`text-sm font-medium transition-colors ${isActive('/search') ? 'text-white' : 'text-gray-400 hover:text-white'}`}>
              Browse
            </Link>
            <Link to="/history" className={`text-sm font-medium transition-colors ${isActive('/history') ? 'text-white' : 'text-gray-400 hover:text-white'}`}>
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
