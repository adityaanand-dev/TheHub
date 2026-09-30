/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        hub: {
          bg: '#070b16',
          panel: '#0d1527',
          card: '#111b2e',
          cardHover: '#16233b',
          border: 'rgba(148, 163, 184, 0.18)',
          accent: '#6366f1',
          accentHover: '#4f46e5'
        }
      }
    },
  },
  plugins: [],
}
