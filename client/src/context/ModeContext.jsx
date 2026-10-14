import { createContext, useContext, useState, useEffect } from 'react';

const ModeContext = createContext();

export const MODES = {
  ANIME: 'anime',
  KDRAMA: 'kdrama',
  HOLLYWOOD: 'hollywood',
};

export const MODE_CONFIG = {
  [MODES.ANIME]: {
    id: MODES.ANIME,
    name: 'Anime',
    title: 'Anime Universe',
    tagline: 'Stream trending, top airing & sub/dub Japanese anime and Donghua',
    accentColor: '#8B5CF6',
    badgeText: 'ANIME',
  },
  [MODES.KDRAMA]: {
    id: MODES.KDRAMA,
    name: 'K-Drama & Movies',
    title: 'Korean Cinema & Dramas',
    tagline: 'Trending Korean drama series, romance, thrillers, and award-winning K-movies',
    accentColor: '#EC4899',
    badgeText: 'K-DRAMA',
  },
  [MODES.HOLLYWOOD]: {
    id: MODES.HOLLYWOOD,
    name: 'Hollywood & Series',
    title: 'Hollywood Movies & TV',
    tagline: 'Blockbuster movies, trending US/Global TV shows, and top-rated cinema',
    accentColor: '#06B6D4',
    badgeText: 'HOLLYWOOD',
  },
};

export const ModeProvider = ({ children }) => {
  const [mode, setModeState] = useState(() => {
    return localStorage.getItem('aniverse_mode') || MODES.ANIME;
  });

  const setMode = (newMode) => {
    if (Object.values(MODES).includes(newMode)) {
      setModeState(newMode);
      localStorage.setItem('aniverse_mode', newMode);
      window.dispatchEvent(new CustomEvent('aniverse_mode_changed', { detail: { mode: newMode } }));
    }
  };

  useEffect(() => {
    const handleStorage = (e) => {
      if (e.key === 'aniverse_mode' && e.newValue) {
        setModeState(e.newValue);
      }
    };
    window.addEventListener('storage', handleStorage);
    return () => window.removeEventListener('storage', handleStorage);
  }, []);

  const config = MODE_CONFIG[mode] || MODE_CONFIG[MODES.ANIME];

  return (
    <ModeContext.Provider
      value={{
        mode,
        setMode,
        config,
        isAnime: mode === MODES.ANIME,
        isKDrama: mode === MODES.KDRAMA,
        isHollywood: mode === MODES.HOLLYWOOD,
      }}
    >
      {children}
    </ModeContext.Provider>
  );
};

export const useMode = () => {
  const context = useContext(ModeContext);
  if (!context) {
    throw new Error('useMode must be used within a ModeProvider');
  }
  return context;
};

export default ModeContext;
