import { useState, useRef, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Settings, Save, Download, Upload, User, Trash2, Cloud, RefreshCw, LogOut, CheckCircle, Tv, Film, Sparkles, Check } from 'lucide-react';
import toast from 'react-hot-toast';
import api from '../services/api';
import syncManager from '../services/syncManager';
import { useMode, MODES, MODE_CONFIG } from '../context/ModeContext';

export default function Profile() {
  const { mode, setMode } = useMode();
  const fileInputRef = useRef(null);
  const [preferences, setPreferences] = useState({
    autoSkipIntro: localStorage.getItem('autoSkip') === 'true',
    autoNextEpisode: localStorage.getItem('autoNext') !== 'false',
    autoPlay: localStorage.getItem('autoPlay') !== 'false',
    defaultAudio: localStorage.getItem('defaultAudio') || 'sub',
  });

  const [googleSync, setGoogleSync] = useState({
    connected: false,
    user: null,
    lastSynced: null,
  });
  const [isSyncing, setIsSyncing] = useState(false);

  useEffect(() => {
    fetchGoogleStatus();

    const handleMessage = (event) => {
      if (event.data?.type === 'GOOGLE_DRIVE_CONNECTED') {
        toast.success(`Google Drive connected as ${event.data.user?.email || 'User'}!`);
        fetchGoogleStatus();
        syncManager.syncNow().then(res => {
          if (res.success) toast.success('Initial cloud sync completed!');
        });
      }
    };

    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, []);

  const fetchGoogleStatus = async () => {
    try {
      const res = await api.Sync.getGoogleStatus();
      if (res.data?.success) {
        setGoogleSync({
          connected: res.data.connected,
          user: res.data.user,
          lastSynced: res.data.lastSynced,
        });
      }
    } catch (e) {
      console.error('Failed to get Google status', e);
    }
  };

  const handleConnectGoogle = async () => {
    try {
      const res = await api.Sync.getGoogleAuthUrl();
      if (res.data?.url) {
        const width = 500;
        const height = 650;
        const left = window.screen.width / 2 - width / 2;
        const top = window.screen.height / 2 - height / 2;
        window.open(
          res.data.url,
          'google_oauth_popup',
          `width=${width},height=${height},top=${top},left=${left},toolbar=no,menubar=no`
        );
      }
    } catch (e) {
      toast.error('Failed to initiate Google login');
    }
  };

  const handleDisconnectGoogle = async () => {
    if (!window.confirm('Disconnect Google Drive sync? Your local data will not be affected.')) return;
    try {
      await api.Sync.disconnectGoogle();
      setGoogleSync({ connected: false, user: null, lastSynced: null });
      toast.success('Google Drive disconnected');
    } catch (e) {
      toast.error('Failed to disconnect');
    }
  };

  const handleSyncNow = async () => {
    setIsSyncing(true);
    const res = await syncManager.syncNow();
    setIsSyncing(false);
    if (res.success) {
      toast.success('Cloud sync complete! Watchlist & history updated.');
      fetchGoogleStatus();
    } else {
      toast.error(res.message || 'Sync failed');
    }
  };

  const handleToggle = (key) => {
    const newValue = !preferences[key];
    setPreferences(prev => ({ ...prev, [key]: newValue }));
    
    // Some keys might need mapping
    if (key === 'autoSkipIntro') localStorage.setItem('autoSkip', newValue);
    else if (key === 'autoNextEpisode') localStorage.setItem('autoNext', newValue);
    else if (key === 'autoPlay') localStorage.setItem('autoPlay', newValue);
    else localStorage.setItem(key, newValue);
  };

  const handleSave = () => {
    toast.success('Preferences saved locally!');
  };

  const exportData = () => {
    try {
      const data = {
        history: JSON.parse(localStorage.getItem('aniverse_history') || '[]'),
        watchlists: JSON.parse(localStorage.getItem('aniverse_watchlists') || '[]'),
        preferences: preferences
      };
      
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'aniverse_save_data.json';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      
      toast.success('Save file exported successfully!');
    } catch (e) {
      toast.error('Failed to export data');
      console.error(e);
    }
  };

  const importData = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const data = JSON.parse(e.target.result);
        if (data.history) {
          localStorage.setItem('aniverse_history', JSON.stringify(data.history));
        }
        if (data.watchlists) {
          localStorage.setItem('aniverse_watchlists', JSON.stringify(data.watchlists));
        }
        if (data.preferences) {
          if (data.preferences.autoSkipIntro !== undefined) localStorage.setItem('autoSkip', data.preferences.autoSkipIntro);
          if (data.preferences.autoNextEpisode !== undefined) localStorage.setItem('autoNext', data.preferences.autoNextEpisode);
          if (data.preferences.autoPlay !== undefined) localStorage.setItem('autoPlay', data.preferences.autoPlay);
          if (data.preferences.defaultAudio !== undefined) localStorage.setItem('defaultAudio', data.preferences.defaultAudio);
          setPreferences({ ...preferences, ...data.preferences });
        }
        toast.success('Save file imported successfully!');
        setTimeout(() => window.location.reload(), 1000);
      } catch (err) {
        toast.error('Invalid save file');
        console.error(err);
      }
    };
    reader.readAsText(file);
  };

  const clearData = () => {
    if (window.confirm('Are you sure you want to delete all local history and settings? This cannot be undone.')) {
      localStorage.removeItem('aniverse_history');
      localStorage.removeItem('aniverse_watchlists');
      toast.success('Local data cleared!');
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-4xl mx-auto px-4 sm:px-6 py-24 min-h-screen"
    >
      <div className="flex items-center space-x-4 mb-8">
        <div className="w-16 h-16 rounded-full bg-[#1a1a2e] border-2 border-[#8B5CF6] flex items-center justify-center p-1">
          <User size={32} className="text-[#8B5CF6]" />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-white">Local Profile</h1>
          <p className="text-gray-400 mt-1">Data saved entirely on your device</p>
        </div>
      </div>

      {/* Entertainment Mode Switcher Card */}
      <div className="bg-[#1a1a2e] border border-[#2D2D44] rounded-2xl p-6 md:p-8 mb-8 shadow-xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
          <div>
            <h2 className="text-xl font-bold text-white flex items-center gap-2">
              <Sparkles size={20} className="text-[#8B5CF6]" />
              Entertainment Mode Choice
            </h2>
            <p className="text-gray-400 text-xs mt-1">
              Switching modes instantly updates your Home catalog, Search filters, and Watch History to that category.
            </p>
          </div>
          <span className="text-xs font-bold text-[#A78BFA] bg-[#8B5CF6]/15 border border-[#8B5CF6]/30 px-3 py-1 rounded-full self-start sm:self-auto">
            Active: {MODE_CONFIG[mode]?.name}
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-4">
          {/* 1. Anime Mode */}
          <div
            onClick={() => {
              setMode(MODES.ANIME);
              toast.success('Switched to Anime Universe mode!');
            }}
            className={`cursor-pointer rounded-xl p-4 border transition-all flex flex-col justify-between ${
              mode === MODES.ANIME
                ? 'bg-[#8B5CF6]/15 border-[#8B5CF6] shadow-lg shadow-[#8B5CF6]/20'
                : 'bg-[#13131a] border-[#26263A] hover:border-gray-500'
            }`}
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <div className="w-9 h-9 rounded-lg bg-[#8B5CF6]/20 flex items-center justify-center text-[#8B5CF6]">
                  <Sparkles size={18} />
                </div>
                {mode === MODES.ANIME && <Check size={18} className="text-[#8B5CF6]" />}
              </div>
              <h3 className="text-white font-bold text-sm">Anime</h3>
              <p className="text-gray-400 text-xs mt-1 leading-relaxed">
                AniList catalog, sub/dub Japanese anime, top airing broadcasts, and Donghua.
              </p>
            </div>
            <div className="mt-3 text-[11px] font-semibold text-[#8B5CF6]">AniList + MegaPlay</div>
          </div>

          {/* 2. KDrama & Movies Mode */}
          <div
            onClick={() => {
              setMode(MODES.KDRAMA);
              toast.success('Switched to K-Drama & Movies mode!');
            }}
            className={`cursor-pointer rounded-xl p-4 border transition-all flex flex-col justify-between ${
              mode === MODES.KDRAMA
                ? 'bg-[#EC4899]/15 border-[#EC4899] shadow-lg shadow-[#EC4899]/20'
                : 'bg-[#13131a] border-[#26263A] hover:border-gray-500'
            }`}
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <div className="w-9 h-9 rounded-lg bg-[#EC4899]/20 flex items-center justify-center text-[#EC4899]">
                  <Tv size={18} />
                </div>
                {mode === MODES.KDRAMA && <Check size={18} className="text-[#EC4899]" />}
              </div>
              <h3 className="text-white font-bold text-sm">K-Drama & Movies</h3>
              <p className="text-gray-400 text-xs mt-1 leading-relaxed">
                Korean drama series, romantic comedies, thrillers, and award-winning Korean cinema.
              </p>
            </div>
            <div className="mt-3 text-[11px] font-semibold text-[#EC4899]">TMDB Korean + Multi-Server</div>
          </div>

          {/* 3. Hollywood & TV Series Mode */}
          <div
            onClick={() => {
              setMode(MODES.HOLLYWOOD);
              toast.success('Switched to Hollywood & Series mode!');
            }}
            className={`cursor-pointer rounded-xl p-4 border transition-all flex flex-col justify-between ${
              mode === MODES.HOLLYWOOD
                ? 'bg-[#06B6D4]/15 border-[#06B6D4] shadow-lg shadow-[#06B6D4]/20'
                : 'bg-[#13131a] border-[#26263A] hover:border-gray-500'
            }`}
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <div className="w-9 h-9 rounded-lg bg-[#06B6D4]/20 flex items-center justify-center text-[#06B6D4]">
                  <Film size={18} />
                </div>
                {mode === MODES.HOLLYWOOD && <Check size={18} className="text-[#06B6D4]" />}
              </div>
              <h3 className="text-white font-bold text-sm">Hollywood & Series</h3>
              <p className="text-gray-400 text-xs mt-1 leading-relaxed">
                Hollywood blockbusters, trending TV series, top-rated movies, and global releases.
              </p>
            </div>
            <div className="mt-3 text-[11px] font-semibold text-[#06B6D4]">TMDB Global + Multi-Server</div>
          </div>
        </div>
      </div>

      <div className="grid md:grid-cols-2 gap-8">
        <div className="bg-[#1a1a2e] border border-[#2D2D44] rounded-2xl p-6 md:p-8">
          <div className="flex items-center space-x-2 mb-6 text-white">
            <Settings size={24} className="text-[#8B5CF6]" />
            <h2 className="text-xl font-bold">Player Preferences</h2>
          </div>

          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-white font-medium">Auto-play</h3>
                <p className="text-sm text-gray-400">Play video automatically when loaded</p>
              </div>
              <button
                onClick={() => handleToggle('autoPlay')}
                className={`w-12 h-6 rounded-full transition-colors relative ${preferences.autoPlay ? 'bg-[#8B5CF6]' : 'bg-[#2D2D44]'}`}
              >
                <div className={`w-4 h-4 bg-white rounded-full absolute top-1 transition-transform ${preferences.autoPlay ? 'translate-x-7' : 'translate-x-1'}`}></div>
              </button>
            </div>

            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-white font-medium">Auto-next Episode</h3>
                <p className="text-sm text-gray-400">Play next episode automatically</p>
              </div>
              <button
                onClick={() => handleToggle('autoNextEpisode')}
                className={`w-12 h-6 rounded-full transition-colors relative ${preferences.autoNextEpisode ? 'bg-[#8B5CF6]' : 'bg-[#2D2D44]'}`}
              >
                <div className={`w-4 h-4 bg-white rounded-full absolute top-1 transition-transform ${preferences.autoNextEpisode ? 'translate-x-7' : 'translate-x-1'}`}></div>
              </button>
            </div>

            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-white font-medium">Auto-skip Intro</h3>
                <p className="text-sm text-gray-400">Automatically skip anime openings</p>
              </div>
              <button
                onClick={() => handleToggle('autoSkipIntro')}
                className={`w-12 h-6 rounded-full transition-colors relative ${preferences.autoSkipIntro ? 'bg-[#8B5CF6]' : 'bg-[#2D2D44]'}`}
              >
                <div className={`w-4 h-4 bg-white rounded-full absolute top-1 transition-transform ${preferences.autoSkipIntro ? 'translate-x-7' : 'translate-x-1'}`}></div>
              </button>
            </div>

            <div>
              <h3 className="text-white font-medium mb-2">Preferred Audio</h3>
              <select
                value={preferences.defaultAudio}
                onChange={(e) => {
                  setPreferences({ ...preferences, defaultAudio: e.target.value });
                  localStorage.setItem('defaultAudio', e.target.value);
                }}
                className="w-full bg-[#13131a] border border-[#2D2D44] text-white rounded-lg p-3 focus:ring-2 focus:ring-[#8B5CF6] outline-none"
              >
                <option value="sub">Japanese (Sub)</option>
                <option value="dub">English (Dub)</option>
              </select>
            </div>

            <button
              onClick={handleSave}
              className="w-full flex items-center justify-center space-x-2 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white py-3 rounded-lg font-semibold transition-colors mt-4"
            >
              <Save size={18} />
              <span>Save Preferences</span>
            </button>
          </div>
        </div>

        <div className="bg-[#1a1a2e] border border-[#2D2D44] rounded-2xl p-6 md:p-8">
          <div className="flex items-center space-x-2 mb-6 text-white">
            <Save size={24} className="text-[#8B5CF6]" />
            <h2 className="text-xl font-bold">Data Management</h2>
          </div>

          <div className="space-y-6">
            <div className="text-center py-6">
              <div className="w-16 h-16 bg-[#8B5CF6]/20 rounded-full flex items-center justify-center mx-auto mb-4">
                <Download size={32} className="text-[#8B5CF6]" />
              </div>
              <h3 className="text-white font-medium mb-2">Local Save File</h3>
              <p className="text-gray-400 text-sm mb-6">Backup your watch history and preferences to a local file on your computer.</p>
              
              <div className="space-y-3">
                <button
                  onClick={exportData}
                  className="w-full bg-[#8B5CF6] hover:bg-[#7C3AED] text-white py-3 rounded-lg font-semibold transition-colors flex items-center justify-center space-x-2"
                >
                  <Download size={18} />
                  <span>Export Save File</span>
                </button>
                
                <input 
                  type="file" 
                  ref={fileInputRef} 
                  onChange={importData} 
                  accept=".json" 
                  className="hidden" 
                />
                
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="w-full bg-[#252540] hover:bg-[#2D2D44] text-white py-3 rounded-lg font-semibold transition-colors flex items-center justify-center space-x-2"
                >
                  <Upload size={18} />
                  <span>Import Save File</span>
                </button>

                <button
                  onClick={clearData}
                  className="w-full bg-red-500/10 hover:bg-red-500/20 text-red-500 py-3 rounded-lg font-semibold transition-colors flex items-center justify-center space-x-2 mt-4"
                >
                  <Trash2 size={18} />
                  <span>Clear All Data</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Google Drive Cross-Device Cloud Sync Card */}
      <div className="mt-8 bg-[#1a1a2e] border border-[#2D2D44] rounded-2xl p-6 md:p-8">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
          <div className="flex items-center space-x-3">
            <div className="w-12 h-12 bg-[#06B6D4]/15 rounded-xl flex items-center justify-center">
              <Cloud size={28} className="text-[#06B6D4]" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-white flex items-center gap-2">
                Google Drive Cloud Sync
                {googleSync.connected && (
                  <span className="flex items-center gap-1 text-xs font-semibold bg-emerald-500/20 text-emerald-400 px-2.5 py-0.5 rounded-full">
                    <CheckCircle size={12} /> Connected
                  </span>
                )}
              </h2>
              <p className="text-gray-400 text-sm">
                Seamlessly sync exact-second timestamps, watch history, and watchlists with your Android phone
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {googleSync.connected ? (
              <>
                <button
                  onClick={handleSyncNow}
                  disabled={isSyncing}
                  className="bg-[#8B5CF6] hover:bg-[#7C3AED] disabled:opacity-50 text-white px-5 py-2.5 rounded-xl font-semibold text-sm transition-colors flex items-center space-x-2"
                >
                  <RefreshCw size={16} className={isSyncing ? 'animate-spin' : ''} />
                  <span>{isSyncing ? 'Syncing...' : 'Sync Now'}</span>
                </button>
                <button
                  onClick={handleDisconnectGoogle}
                  className="bg-red-500/10 hover:bg-red-500/20 text-red-400 px-4 py-2.5 rounded-xl font-semibold text-sm transition-colors flex items-center space-x-1.5"
                >
                  <LogOut size={16} />
                  <span>Disconnect</span>
                </button>
              </>
            ) : (
              <button
                onClick={handleConnectGoogle}
                className="bg-[#8B5CF6] hover:bg-[#7C3AED] text-white px-6 py-3 rounded-xl font-semibold text-sm transition-all shadow-lg shadow-[#8B5CF6]/20 flex items-center space-x-2"
              >
                <Cloud size={18} />
                <span>Connect Google Drive</span>
              </button>
            )}
          </div>
        </div>

        {googleSync.connected && (
          <div className="bg-[#13131a] rounded-xl p-4 border border-[#26263A] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-sm">
            <div className="flex items-center space-x-3">
              {googleSync.user?.picture ? (
                <img src={googleSync.user.picture} alt="" className="w-8 h-8 rounded-full" />
              ) : (
                <div className="w-8 h-8 rounded-full bg-[#8B5CF6]/20 flex items-center justify-center text-[#8B5CF6] font-bold">
                  {googleSync.user?.email?.[0]?.toUpperCase() || 'G'}
                </div>
              )}
              <div>
                <p className="text-white font-medium">{googleSync.user?.name || 'Google Account'}</p>
                <p className="text-gray-400 text-xs">{googleSync.user?.email}</p>
              </div>
            </div>
            <div className="text-gray-400 text-xs">
              Last synced: {googleSync.lastSynced ? new Date(googleSync.lastSynced).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Not yet synced'}
            </div>
          </div>
        )}
      </div>
    </motion.div>
  );
}
