window.BFD_CONFIG = window.BFD_CONFIG || {
  supabaseUrl: '',
  supabaseAnonKey: '',
  mapProvider: 'openstreetmap',
  mapsApiKey: '',
  paymentProvider: 'test',
  paymentKey: ''
};

window.BFD_READY = Boolean(BFD_CONFIG.supabaseUrl && BFD_CONFIG.supabaseAnonKey);
