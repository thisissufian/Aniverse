import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { User, Settings, Link as LinkIcon, Unlink, Save, RefreshCw } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import api from '../services/api';

export default function Profile() {
  const { user, updateProfile } = useAuth();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [malStatus, setMalStatus] = useState({ connected: false, malUsername: null });
  const [syncing, setSyncing] = useState(false);
  const [preferences, setPreferences] = useState({
    autoSkipIntro: user?.preferences?.autoSkipIntro ?? false,
    autoNextEpisode: user?.preferences?.autoNextEpisode ?? true,
    defaultQuality: user?.preferences?.defaultQuality ?? 'auto',
    malAutoSync: user?.preferences?.malAutoSync ?? true,
  });

  useEffect(() => {
    // Check MAL callback result
    const malResult = searchParams.get('mal');
    if (malResult === 'success') {
      toast.success('MyAnimeList connected successfully!');
      window.history.replaceState({}, '', '/profile');
    } else if (malResult === 'error') {
      toast.error('Failed to connect MyAnimeList. Please try again.');
      window.history.replaceState({}, '', '/profile');
    }
    fetchMalStatus();
  }, [searchParams]);

  const fetchMalStatus = async () => {
    try {
      const res = await api.MAL.getStatus();
      setMalStatus(res.data);
    } catch {
      // Not logged in or error
    }
  };

  const handleToggle = (key) => {
    setPreferences(prev => ({ ...prev, [key]: !prev[key] }));
  };

  const handleSave = async () => {
    try {
      setLoading(true);
      await updateProfile({ preferences });
      toast.success('Preferences saved!');
    } catch {
      toast.error('Failed to save preferences');
    } finally {
      setLoading(false);
    }
  };

  const connectMal = async () => {
    try {
      const res = await api.MAL.getAuthUrl();
      if (res.data?.url) {
        window.location.href = res.data.url;
      }
    } catch {
      toast.error('Failed to start MAL connection');
    }
  };

  const disconnectMal = async () => {
    try {
      await api.MAL.disconnect();
      setMalStatus({ connected: false, malUsername: null });
      toast.success('Disconnected from MyAnimeList');
    } catch {
      toast.error('Failed to disconnect');
    }
  };

  const syncAll = async () => {
    try {
      setSyncing(true);
      const res = await api.MAL.syncAll();
      toast.success(res.data.message || 'Sync complete!');
    } catch {
      toast.error('Sync failed');
    } finally {
      setSyncing(false);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-4xl mx-auto px-4 sm:px-6 py-24 min-h-screen"
    >
      {/* Profile Header */}
      <div className="bg-[#1a1a2e] border border-[#2D2D44] rounded-2xl p-8 mb-8 flex items-center space-x-6">
        <div className="w-24 h-24 bg-gradient-to-br from-[#8B5CF6] to-[#7C3AED] rounded-full flex items-center justify-center shadow-lg shadow-[#8B5CF6]/20">
          <User size={48} className="text-white" />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-white">{user?.username || 'User'}</h1>
          <p className="text-gray-400 mt-1">Anime Enthusiast</p>
          {malStatus.connected && (
            <p className="text-sm text-[#A78BFA] mt-1 flex items-center gap-1">
              <span className="w-2 h-2 bg-green-400 rounded-full inline-block"></span>
              MAL: {malStatus.malUsername}
            </p>
          )}
        </div>
      </div>

      <div className="grid md:grid-cols-2 gap-8">
        {/* Preferences */}
        <div className="bg-[#1a1a2e] border border-[#2D2D44] rounded-2xl p-6 md:p-8">
          <div className="flex items-center space-x-2 mb-6 text-white">
            <Settings size={24} className="text-[#8B5CF6]" />
            <h2 className="text-xl font-bold">Preferences</h2>
          </div>

          <div className="space-y-6">
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

            <div>
              <h3 className="text-white font-medium mb-2">Default Video Quality</h3>
              <select
                value={preferences.defaultQuality}
                onChange={(e) => setPreferences({ ...preferences, defaultQuality: e.target.value })}
                className="w-full bg-[#13131a] border border-[#2D2D44] text-white rounded-lg p-3 focus:ring-2 focus:ring-[#8B5CF6] outline-none"
              >
                <option value="auto">Auto</option>
                <option value="1080p">1080p</option>
                <option value="720p">720p</option>
                <option value="480p">480p</option>
              </select>
            </div>

            <button
              onClick={handleSave}
              disabled={loading}
              className="w-full flex items-center justify-center space-x-2 bg-[#8B5CF6] hover:bg-[#7C3AED] text-white py-3 rounded-lg font-semibold transition-colors disabled:opacity-50 mt-4"
            >
              <Save size={18} />
              <span>{loading ? 'Saving...' : 'Save Preferences'}</span>
            </button>
          </div>
        </div>

        {/* MAL Integration */}
        <div className="bg-[#1a1a2e] border border-[#2D2D44] rounded-2xl p-6 md:p-8">
          <div className="flex items-center space-x-2 mb-6 text-white">
            <LinkIcon size={24} className="text-[#2E51A2]" />
            <h2 className="text-xl font-bold">MyAnimeList</h2>
          </div>

          <div className="space-y-6">
            {malStatus.connected ? (
              <div className="space-y-4">
                <div className="bg-[#13131a] border border-[#2D2D44] rounded-xl p-4">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 bg-[#2E51A2] rounded-full flex items-center justify-center">
                        <span className="text-white font-bold text-sm">MAL</span>
                      </div>
                      <div>
                        <p className="text-white font-medium">Connected as</p>
                        <p className="text-[#A78BFA]">{malStatus.malUsername}</p>
                      </div>
                    </div>
                    <button
                      onClick={disconnectMal}
                      className="text-gray-400 hover:text-red-400 p-2 transition-colors"
                      title="Disconnect"
                    >
                      <Unlink size={20} />
                    </button>
                  </div>

                  <div className="flex items-center justify-between pt-4 border-t border-[#2D2D44]">
                    <div>
                      <h3 className="text-white font-medium text-sm">Auto-sync to MAL</h3>
                      <p className="text-xs text-gray-400">Update progress automatically</p>
                    </div>
                    <button
                      onClick={() => handleToggle('malAutoSync')}
                      className={`w-10 h-5 rounded-full transition-colors relative ${preferences.malAutoSync ? 'bg-[#8B5CF6]' : 'bg-[#2D2D44]'}`}
                    >
                      <div className={`w-3.5 h-3.5 bg-white rounded-full absolute top-[3px] transition-transform ${preferences.malAutoSync ? 'translate-x-[22px]' : 'translate-x-1'}`}></div>
                    </button>
                  </div>
                </div>

                <button
                  onClick={syncAll}
                  disabled={syncing}
                  className="w-full flex items-center justify-center space-x-2 bg-[#2E51A2] hover:bg-[#254282] text-white py-3 rounded-lg font-semibold transition-colors disabled:opacity-50"
                >
                  <RefreshCw size={18} className={syncing ? 'animate-spin' : ''} />
                  <span>{syncing ? 'Syncing...' : 'Sync All to MAL'}</span>
                </button>
              </div>
            ) : (
              <div className="text-center py-6">
                <div className="w-16 h-16 bg-[#2E51A2]/20 rounded-full flex items-center justify-center mx-auto mb-4">
                  <LinkIcon size={32} className="text-[#2E51A2]" />
                </div>
                <h3 className="text-white font-medium mb-2">Connect Your Account</h3>
                <p className="text-gray-400 text-sm mb-6">Link your MyAnimeList account to sync your watchlist and episode progress automatically.</p>
                <button
                  onClick={connectMal}
                  className="w-full bg-[#2E51A2] hover:bg-[#254282] text-white py-3 rounded-lg font-semibold transition-colors flex items-center justify-center space-x-2"
                >
                  <LinkIcon size={18} />
                  <span>Connect MyAnimeList</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </motion.div>
  );
}
