import axios from 'axios';

const corsProxy = async (req, res, next) => {
  const { url: targetUrl, headers: reqHeaders } = req.query;

  if (!targetUrl) {
    return res.status(400).json({ success: false, message: 'Missing url parameter' });
  }

  try {
    let customHeaders = {};
    if (reqHeaders) {
      try {
        customHeaders = JSON.parse(reqHeaders);
      } catch (e) {
        // ignore invalid json headers
      }
    }

    const axiosConfig = {
      method: 'GET',
      url: targetUrl,
      responseType: 'stream',
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
        ...customHeaders
      }
    };

    const response = await axios(axiosConfig);

    res.set({
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, OPTIONS',
      'Content-Type': response.headers['content-type'] || 'application/vnd.apple.mpegurl'
    });

    if (response.headers['content-type']?.includes('mpegurl') || targetUrl.endsWith('.m3u8')) {
      let body = '';
      response.data.on('data', (chunk) => {
        body += chunk.toString();
      });

      response.data.on('end', () => {
        const baseUrl = new URL(targetUrl);
        let modifiedBody = body.split('\\n').map(line => {
          if (line.startsWith('#') || line.trim() === '') {
            return line;
          }
          try {
            const absoluteUrl = new URL(line, baseUrl).href;
            return `/api/stream/proxy/m3u8?url=${encodeURIComponent(absoluteUrl)}${reqHeaders ? '&headers=' + encodeURIComponent(reqHeaders) : ''}`;
          } catch (e) {
            return line;
          }
        }).join('\\n');
        res.send(modifiedBody);
      });
    } else {
      response.data.pipe(res);
    }
  } catch (error) {
    console.error('Proxy Error:', error.message);
    res.status(500).json({ success: false, message: 'Proxy request failed' });
  }
};

export default corsProxy;
