import { Link, useLocation } from 'react-router-dom';
import { Search } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
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
          </div>
        </div>

        {/* Right Side: Search & Auth */}
        <div className="flex items-center gap-6">
          <Link to="/search" className="text-gray-400 hover:text-white transition-colors">
            <Search size={20} />
          </Link>

          {user ? (
            <div className="flex items-center gap-4">
              <Link to="/watchlist" className={`text-sm font-medium transition-colors ${isActive('/watchlist') ? 'text-white' : 'text-gray-400 hover:text-white'}`}>Watchlist</Link>
              <Link to="/profile" className={`text-sm font-medium transition-colors ${isActive('/profile') ? 'text-white' : 'text-gray-400 hover:text-white'}`}>Profile</Link>
              <button onClick={logout} className="text-sm font-medium text-gray-400 hover:text-white transition-colors">Logout</button>
            </div>
          ) : (
            <div className="flex items-center gap-4">
              <Link to="/login" className="text-sm font-medium text-gray-300 hover:text-white transition-colors">
                Login
              </Link>
              <Link to="/register" className="text-sm font-medium px-4 py-1.5 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white rounded transition-colors shadow-lg shadow-[#8B5CF6]/20">
                Register
              </Link>
            </div>
          )}
        </div>
        
      </div>
    </nav>
  );
}
