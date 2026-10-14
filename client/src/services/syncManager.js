import api from './api';

class SyncManager {
  constructor() {
    this.isSyncing = false;
  }

  getLocalPayload() {
    let history = [];
    let watchlists = [];
    try {
      history = JSON.parse(localStorage.getItem('aniverse_history') || '[]');
      watchlists = JSON.parse(localStorage.getItem('aniverse_watchlists') || '[]');
    } catch (e) {
      console.error('Failed to read localStorage for sync', e);
    }

    return {
      version: 1,
      client: 'desktop',
      syncedAt: Date.now(),
      history: history.map(item => ({
        animeId: item.animeId,
        title: item.title,
        coverUrl: item.coverImage || item.coverUrl || '',
        episodeNumber: item.episode || item.episodeNumber || 1,
        positionMs: item.time ? Math.round(item.time * 1000) : (item.positionMs || 0),
        durationMs: item.duration ? Math.round(item.duration * 1000) : (item.durationMs || 0),
        updatedAt: item.updatedAt ? new Date(item.updatedAt).getTime() : Date.now()
      })),
      watchlist: watchlists.flatMap(list => (list.items || []).map(item => ({
        animeId: item.id || item.animeId,
        title: item.title?.english || item.title?.romaji || item.title || '',
        coverUrl: item.coverImage?.large || item.coverImage || item.coverUrl || '',
        score: item.averageScore || item.score || null,
        format: item.format || 'TV',
        addedAt: item.addedAt || Date.now()
      })))
    };
  }

  mergeRemoteIntoLocal(remoteData) {
    if (!remoteData) return;

    // 1. Merge History (LWW based on updatedAt)
    try {
      const localHistory = JSON.parse(localStorage.getItem('aniverse_history') || '[]');
      const historyMap = new Map();

      // Load local history into map
      localHistory.forEach(item => {
        const timeVal = item.updatedAt ? new Date(item.updatedAt).getTime() : 0;
        historyMap.set(item.animeId, { ...item, _updatedTime: timeVal });
      });

      // Merge remote history
      if (Array.isArray(remoteData.history)) {
        remoteData.history.forEach(remoteItem => {
          const localItem = historyMap.get(remoteItem.animeId);
          const remoteTime = remoteItem.updatedAt || 0;

          if (!localItem || remoteTime > (localItem._updatedTime || 0)) {
            historyMap.set(remoteItem.animeId, {
              animeId: remoteItem.animeId,
              title: remoteItem.title,
              coverImage: remoteItem.coverUrl,
              episode: remoteItem.episodeNumber,
              time: Math.round((remoteItem.positionMs || 0) / 1000),
              duration: Math.round((remoteItem.durationMs || 0) / 1000),
              updatedAt: new Date(remoteTime).toISOString(),
              _updatedTime: remoteTime
            });
          }
        });
      }

      const mergedHistory = Array.from(historyMap.values()).map(({ _updatedTime, ...clean }) => clean);
      mergedHistory.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt));
      localStorage.setItem('aniverse_history', JSON.stringify(mergedHistory));
    } catch (e) {
      console.error('Failed to merge history', e);
    }

    // 2. Merge Watchlists
    try {
      let localWatchlists = JSON.parse(localStorage.getItem('aniverse_watchlists') || '[]');
      if (!Array.isArray(localWatchlists) || localWatchlists.length === 0) {
        localWatchlists = [{ id: 'default', name: 'My List', items: [] }];
      }

      const defaultList = localWatchlists.find(l => l.id === 'default') || localWatchlists[0];
      const existingIds = new Set((defaultList.items || []).map(i => i.id || i.animeId));

      if (Array.isArray(remoteData.watchlist)) {
        remoteData.watchlist.forEach(remoteItem => {
          if (!existingIds.has(remoteItem.animeId)) {
            defaultList.items.push({
              id: remoteItem.animeId,
              animeId: remoteItem.animeId,
              title: { english: remoteItem.title, romaji: remoteItem.title },
              coverImage: { large: remoteItem.coverUrl },
              format: remoteItem.format || 'TV',
              averageScore: remoteItem.score,
              addedAt: remoteItem.addedAt || Date.now()
            });
            existingIds.add(remoteItem.animeId);
          }
        });
      }

      localStorage.setItem('aniverse_watchlists', JSON.stringify(localWatchlists));
    } catch (e) {
      console.error('Failed to merge watchlists', e);
    }
  }

  async syncNow() {
    if (this.isSyncing) return { success: false, message: 'Sync already in progress' };
    this.isSyncing = true;

    try {
      // 1. Download remote data from Google Drive
      const downloadRes = await api.Sync.downloadGoogleSync();
      const remoteData = downloadRes.data?.data;

      // 2. Merge remote data into local storage
      if (remoteData) {
        this.mergeRemoteIntoLocal(remoteData);
      }

      // 3. Re-export merged payload and upload to Google Drive
      const updatedLocal = this.getLocalPayload();
      const uploadRes = await api.Sync.uploadGoogleSync(updatedLocal);

      return { success: true, lastSynced: uploadRes.data?.lastSynced || Date.now() };
    } catch (error) {
      console.error('Google Drive Sync error:', error);
      return { success: false, message: error.response?.data?.message || error.message };
    } finally {
      this.isSyncing = false;
    }
  }
}

export default new SyncManager();
