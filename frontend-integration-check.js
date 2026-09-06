/* Barmer Food Delivery — non-invasive frontend/backend integration diagnostics. */
(function () {
  'use strict';
  window.BFD_INTEGRATION = Object.freeze({
    backendAvailable: Boolean(window.BFD_SUPABASE?.ready),
    backend: window.BFD_SUPABASE || null,
    getStatus: function () {
      return window.BFD_SUPABASE?.ready ? 'backend' : 'demo';
    }
  });
})();
