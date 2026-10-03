import { useState, useRef } from 'react';
import { motion } from 'framer-motion';
import { Settings, Save, Download, Upload, User, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Profile() {
  const fileInputRef = useRef(null);
  const [preferences, setPreferences] = useState({
    autoSkipIntro: localStorage.getItem('autoSkip') === 'true',
    autoNextEpisode: localStorage.getItem('autoNext') !== 'false',
    autoPlay: localStorage.getItem('autoPlay') !== 'false',
    defaultAudio: localStorage.getItem('defaultAudio') || 'sub',
  });

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
    </motion.div>
  );
}
