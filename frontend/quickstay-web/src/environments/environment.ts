export const environment = {
  production: false,
  // Sesión VII: el frontend ya no le habla directo al backend (8080) —
  // pasa por el API Gateway (8000), que enruta hacia el monolito o hacia
  // payment-service según el path. Ver docs/session-07-evaluation.md.
  apiUrl: 'http://localhost:8000'
};

