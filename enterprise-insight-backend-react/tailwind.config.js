/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        console: {
          950: '#080b0d',
          900: '#0d1115',
          850: '#11171d',
          800: '#171f27',
          700: '#25313b',
        },
      },
      boxShadow: {
        panel: '0 18px 50px rgba(0, 0, 0, 0.28)',
      },
    },
  },
  plugins: [],
}
