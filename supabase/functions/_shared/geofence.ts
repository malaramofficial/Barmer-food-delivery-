const BARMER_LAT = 25.752146;
const BARMER_LNG = 71.396106;
const DEFAULT_RADIUS_KM = 35;

export function barmerRadiusKm(): number {
  const value = Number(Deno.env.get('BFD_BARMER_RADIUS_KM') ?? DEFAULT_RADIUS_KM);
  return Number.isFinite(value) && value > 0 && value <= 200 ? value : DEFAULT_RADIUS_KM;
}

export function distanceKm(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371;
  const rad = Math.PI / 180;
  const dLat = (lat2 - lat1) * rad;
  const dLng = (lng2 - lng1) * rad;
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(lat1 * rad) * Math.cos(lat2 * rad) * Math.sin(dLng / 2) ** 2;
  return 2 * R * Math.asin(Math.min(1, Math.sqrt(a)));
}

export function isInBarmer(lat: number, lng: number): boolean {
  if (!Number.isFinite(lat) || !Number.isFinite(lng) || lat < -90 || lat > 90 || lng < -180 || lng > 180) return false;
  return distanceKm(BARMER_LAT, BARMER_LNG, lat, lng) <= barmerRadiusKm();
}

export function geofenceInfo(lat: number, lng: number) {
  return { latitude: lat, longitude: lng, center_latitude: BARMER_LAT, center_longitude: BARMER_LNG, radius_km: barmerRadiusKm(), distance_from_center_km: Number(distanceKm(BARMER_LAT, BARMER_LNG, lat, lng).toFixed(2)) };
}
