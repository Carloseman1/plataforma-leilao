/**
 * Traço do cavalo (SVG) — só visual da marca.
 * Separado para o HeritagePanel não ficar gigante.
 */

export default function HorseLineArt() {
  return (
    <svg viewBox="0 0 300 300" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
      <path
        className="horse-line"
        d="M78 230 C70 190 76 160 95 138 C88 118 92 96 108 78 C118 66 132 60 146 62 C150 48 162 38 176 40 C186 42 192 52 190 64 C204 64 216 74 218 88 C230 92 238 104 236 118 C246 124 250 138 244 150 C252 162 250 178 238 186 C240 202 232 216 216 220 C214 236 200 246 184 244 C182 256 170 264 158 260 L154 236 C140 240 126 236 118 224 C104 228 90 222 84 210 Z"
      />
      <path className="horse-line" d="M108 78 C104 62 108 46 120 36 C126 30 134 28 140 32" />
      <path className="horse-line" d="M154 236 L150 268 M184 244 L192 272" />
      <path className="horse-line" d="M84 210 L60 226 M78 230 L52 252" />
      <circle className="horse-line" cx="176" cy="62" r="2.4" />
    </svg>
  )
}
