import { useEffect, useRef } from 'react';
import Artplayer from 'artplayer';
import Hls from 'hls.js';

export default function VideoPlayer({ url, poster, title, thumbnails, isEmbed, skipData }) {
  const containerRef = useRef(null);

  useEffect(() => {
    if (!url) return;

    if (isEmbed || url.includes('megaplay.buzz') || url.includes('stream') || url.includes('iframe') || url.includes('embtaku.pro') || url.includes('gogoplay') || url.includes('vidstreaming')) {
      // It's an iframe embed link, handled by render below
      return;
    }

    const art = new Artplayer({
      container: containerRef.current,
      url,
      poster: poster || '',
      title: title || 'Anime Episode',
      volume: 0.8,
      isLive: false,
      muted: false,
      autoplay: false,
      pip: true,
      autoSize: true,
      autoMini: true,
      screenshot: true,
      setting: true,
      loop: false,
      flip: true,
      playbackRate: true,
      aspectRatio: true,
      fullscreen: true,
      fullscreenWeb: true,
      subtitleOffset: true,
      miniProgressBar: true,
      mutex: true,
      backdrop: true,
      playsInline: true,
      autoPlayback: true,
      airplay: true,
      theme: '#8B5CF6',
    });

    if (url.includes('.m3u8')) {
      art.on('ready', () => {
        const hls = new Hls();
        hls.loadSource(url);
        hls.attachMedia(art.video);
      });
    }

    return () => {
      if (art && art.destroy) {
        art.destroy(false);
      }
    };
  }, [url, poster, title, isEmbed]);

  if (!url) return <div className="w-full h-full flex items-center justify-center bg-black text-gray-400 font-inter">Stream not available</div>;

  const iframeRef = useRef(null);

  useEffect(() => {
    if (!isEmbed && !url?.includes('megaplay') && !url?.includes('stream')) return;
    
    const handleMessage = (e) => {
      try {
        const msg = typeof e.data === 'string' ? JSON.parse(e.data) : e.data;
        if (msg?.event === 'PLAYER_READY' && skipData) {
          // Send skip data back to the iframe so it can display its own skip buttons
          iframeRef.current?.contentWindow?.postMessage(JSON.stringify({
            event: 'SKP_DATA',
            data: {
              value: [skipData.intro || [], skipData.outro || []],
              auto: false
            }
          }), '*');
        }
      } catch (err) {
        // Ignore parse errors from other extensions/messages
      }
    };
    
    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [url, isEmbed, skipData]);

  if (isEmbed || url?.includes('megaplay.buzz') || url?.includes('stream') || url?.includes('iframe') || url?.includes('embtaku.pro') || url?.includes('gogoplay') || url?.includes('vidstreaming')) {
    return (
      <iframe 
        key={url}
        ref={iframeRef}
        src={url} 
        allowFullScreen 
        allow="autoplay; fullscreen; picture-in-picture"
        className="w-full h-full border-none bg-black"
        title={title || "Video Player"}
      />
    );
  }

  return (
    <div 
      ref={containerRef} 
      className="w-full h-full bg-black"
    />
  );
}
