import { Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AnimatePresence } from 'framer-motion';
import { AuthProvider } from './context/AuthContext';
import { ModeProvider } from './context/ModeContext';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';
import Home from './pages/Home';
import Search from './pages/Search';
import AnimeDetail from './pages/AnimeDetail';
import Watch from './pages/Watch';
import MediaDetail from './pages/MediaDetail';
import WatchMedia from './pages/WatchMedia';
import Login from './pages/Login';
import Register from './pages/Register';
import Watchlist from './pages/Watchlist';
import History from './pages/History';
import Profile from './pages/Profile';

import NowPlayingBar from './components/layout/NowPlayingBar';

function Layout({ children }) {
  return (
    <div className="flex flex-col min-h-screen">
      <Navbar />
      <main className="flex-grow">{children}</main>
      <NowPlayingBar />
      <Footer />
    </div>
  );
}

import { ErrorBoundary } from './components/ErrorBoundary';

function App() {
  return (
    <ErrorBoundary>
    <AuthProvider>
    <ModeProvider>
      <Toaster 
        position="top-right" 
        toastOptions={{ 
          style: { 
            background: 'var(--color-bg-surface)', 
            color: 'var(--color-text-primary)',
            border: '1px solid var(--color-border)'
          } 
        }} 
      />
      <Layout>
        <AnimatePresence mode="wait">
          <ErrorBoundary>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/search" element={<Search />} />
            <Route path="/anime/:id" element={<AnimeDetail />} />
            <Route path="/media/:type/:id" element={<MediaDetail />} />
            <Route path="/watch/:animeId" element={<Watch />} />
            <Route path="/watch/:animeId/:episodeId" element={<Watch />} />
            <Route path="/watch-media/:type/:id" element={<WatchMedia />} />
            <Route path="/watch-media/:type/:id/:season/:episode" element={<WatchMedia />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/watchlist" element={<Watchlist />} />
            <Route path="/history" element={<History />} />
            <Route path="/profile" element={<Profile />} />
          </Routes>
          </ErrorBoundary>
        </AnimatePresence>
      </Layout>
    </ModeProvider>
    </AuthProvider>
    </ErrorBoundary>
  );
}

export default App;
