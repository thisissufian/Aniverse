import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Theme } from '@radix-ui/themes';

import '@radix-ui/themes/styles.css';
import '@fontsource/inter';
import '@fontsource/space-mono';
import './index.css';

import App from './App.jsx';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <Theme appearance="dark" accentColor="purple" radius="medium">
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </Theme>
    </QueryClientProvider>
  </React.StrictMode>,
);
