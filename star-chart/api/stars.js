const https = require('https');

const OWNER = 'DP-Hridayan';
const REPO  = 'aShellYou';

// ── GitHub API ────────────────────────────────────────────────────────────────

function get(path, token, accept) {
  return new Promise((resolve, reject) => {
    const req = https.request(
      {
        hostname: 'api.github.com',
        path,
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: accept || 'application/vnd.github+json',
          'User-Agent': 'aShellYou-star-chart/1.0',
        },
      },
      res => {
        let body = '';
        res.on('data', c => (body += c));
        res.on('end', () => {
          try { resolve(JSON.parse(body)); }
          catch (e) { reject(new Error(`JSON parse error: ${body.slice(0, 200)}`)); }
        });
      }
    );
    req.on('error', reject);
    req.end();
  });
}

async function fetchAllStargazers(token) {
  // 1. Get total star count so we can parallelise page fetches
  const repo = await get(`/repos/${OWNER}/${REPO}`, token);
  const total = repo.stargazers_count || 0;
  if (total === 0) return [];

  const totalPages = Math.ceil(total / 100);

  // 2. Fetch all pages in parallel (fast even for large repos)
  const pageNums = Array.from({ length: totalPages }, (_, i) => i + 1);
  const pages = await Promise.all(
    pageNums.map(p =>
      get(
        `/repos/${OWNER}/${REPO}/stargazers?per_page=100&page=${p}`,
        token,
        'application/vnd.github.star+json'
      )
    )
  );

  // 3. Flatten, count per day, build cumulative array
  const dateCounts = {};
  for (const page of pages) {
    if (!Array.isArray(page)) continue;
    for (const entry of page) {
      const date = (entry.starred_at || '').split('T')[0];
      if (date) dateCounts[date] = (dateCounts[date] || 0) + 1;
    }
  }

  let cumulative = 0;
  return Object.keys(dateCounts)
    .sort()
    .map(date => ({ date, cumulative: (cumulative += dateCounts[date]) }));
}

// ── Adaptive time bucketing ───────────────────────────────────────────────────

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

function getBucketType(history) {
  const ageDays =
    (new Date(history[history.length - 1].date) - new Date(history[0].date)) / 86_400_000;
  if (ageDays < 90)   return 'week';
  if (ageDays < 365)  return 'month';
  if (ageDays < 1095) return 'month_year';
  return 'quarter';
}

function bucketKey(dateStr, type) {
  const d = new Date(dateStr);
  if (type === 'week') {
    const dow = d.getUTCDay();
    const diff = dow === 0 ? -6 : 1 - dow;
    const mon = new Date(d);
    mon.setUTCDate(d.getUTCDate() + diff);
    return mon.toISOString().split('T')[0];
  }
  if (type === 'month' || type === 'month_year') return dateStr.slice(0, 7);
  const q = Math.floor(d.getUTCMonth() / 3) + 1;
  return `${d.getUTCFullYear()}-Q${q}`;
}

function bucketLabel(key, type) {
  if (type === 'week') {
    const d = new Date(key);
    return `${MONTHS[d.getUTCMonth()]} ${d.getUTCDate()}`;
  }
  if (type === 'month') {
    return MONTHS[new Date(key + '-01').getUTCMonth()];
  }
  if (type === 'month_year') {
    const d = new Date(key + '-01');
    return `${MONTHS[d.getUTCMonth()]} '${String(d.getUTCFullYear()).slice(2)}`;
  }
  const [year, q] = key.split('-');
  return `${q} '${year.slice(2)}`;
}

function buildBuckets(history) {
  if (!history.length) return { counts: [], labels: [] };
  const type = getBucketType(history);
  const map = {};
  for (const e of history) {
    const k = bucketKey(e.date, type);
    map[k] = Math.max(map[k] || 0, e.cumulative);
  }
  const keys = Object.keys(map).sort();
  return {
    counts: keys.map(k => map[k]),
    labels: keys.map(k => bucketLabel(k, type)),
  };
}

// ── Milestones ────────────────────────────────────────────────────────────────

const NICE_STEPS = [100, 250, 500, 1_000, 2_500, 5_000, 10_000, 25_000, 50_000, 100_000];

function milestoneStep(total) {
  for (const s of NICE_STEPS) if (total / s <= 6) return s;
  return NICE_STEPS[NICE_STEPS.length - 1];
}

function fmtMilestone(n) {
  if (n >= 1_000_000) return `\u2605 ${Math.floor(n / 1_000_000)}M`;
  if (n >= 1_000) {
    const v = n / 1_000;
    return `\u2605 ${Number.isInteger(v) ? v : v.toFixed(1)}K`;
  }
  return `\u2605 ${n.toLocaleString()}`;
}

function getMilestones(counts) {
  const total = counts[counts.length - 1] || 0;
  const step = milestoneStep(total);
  const result = [];
  const seen = new Set();
  for (let thresh = step; thresh <= total; thresh += step) {
    for (let i = 0; i < counts.length; i++) {
      if (counts[i] >= thresh && !seen.has(thresh)) {
        seen.add(thresh);
        result.push({ i, label: fmtMilestone(thresh) });
        break;
      }
    }
  }
  return result;
}

// ── SVG ───────────────────────────────────────────────────────────────────────

const THEMES = {
  light: {
    bg: '#ffffff', border: '#e0e0e0', grid: '#f0f0f0',
    axisText: '#5f6368', titleText: '#1c1b1f',
    line: '#1a73e8', gradTop: '#1a73e8', gradBot: '#ffffff',
    dot: '#1a73e8', pillBg: '#e8f0fe', pillBorder: '#1a73e8', pillText: '#1c1b1f',
  },
  dark: {
    bg: '#1e1e2e', border: '#313244', grid: '#2a2a3e',
    axisText: '#a6adc8', titleText: '#cdd6f4',
    line: '#89b4fa', gradTop: '#89b4fa', gradBot: '#1e1e2e',
    dot: '#89b4fa', pillBg: '#1e3a5f', pillBorder: '#89b4fa', pillText: '#cdd6f4',
  },
};

function niceCeil(v) {
  if (v <= 0) return 100;
  const mag = Math.pow(10, String(Math.floor(v)).length - 1);
  return Math.ceil(v / mag) * mag;
}

function fmtY(v) {
  if (v >= 1_000_000) return `${Math.floor(v / 1_000_000)}M`;
  if (v >= 1_000) { const k = v / 1_000; return `${Number.isInteger(k) ? k : k.toFixed(1)}K`; }
  return String(v);
}

function smoothPath(xs, ys) {
  if (!xs.length) return '';
  if (xs.length === 1) return `M ${xs[0].toFixed(1)} ${ys[0].toFixed(1)}`;
  const d = [`M ${xs[0].toFixed(1)} ${ys[0].toFixed(1)}`];
  for (let i = 1; i < xs.length; i++) {
    const cx = ((xs[i - 1] + xs[i]) / 2).toFixed(1);
    d.push(`C ${cx} ${ys[i-1].toFixed(1)} ${cx} ${ys[i].toFixed(1)} ${xs[i].toFixed(1)} ${ys[i].toFixed(1)}`);
  }
  return d.join(' ');
}

function generateSVG(history, themeName) {
  const t = THEMES[themeName] || THEMES.light;
  const { counts, labels } = buildBuckets(history);
  const n = counts.length;
  if (n === 0) return '<svg xmlns="http://www.w3.org/2000/svg" width="820" height="280"><text x="410" y="140" text-anchor="middle" font-family="sans-serif" fill="#888">No data yet</text></svg>';

  const total = counts[n - 1];
  const W = 820, H = 280;
  const PL = 58, PR = 24, PT = 30, PB = 48;
  const CW = W - PL - PR, CH = H - PT - PB;
  const GRID = 5, MAX_TICKS = 10;

  const yMax = niceCeil(total * 1.08);
  const px = i => n <= 1 ? PL + CW / 2 : PL + (i / (n - 1)) * CW;
  const py = c => PT + CH - (c / yMax) * CH;

  const xs = counts.map((_, i) => px(i));
  const ys = counts.map(c => py(c));
  const lineD = smoothPath(xs, ys);
  const areaD = `${lineD} L ${xs[n-1].toFixed(1)} ${(PT+CH).toFixed(1)} L ${xs[0].toFixed(1)} ${(PT+CH).toFixed(1)} Z`;

  let pLen = 100;
  for (let i = 1; i < xs.length; i++) pLen += Math.hypot(xs[i]-xs[i-1], ys[i]-ys[i-1]);
  pLen = Math.ceil(pLen * 1.3);

  const stepX = Math.max(1, Math.ceil(n / MAX_TICKS));
  const xTicks = [];
  for (let i = 0; i < n; i += stepX) xTicks.push(i);
  if (!xTicks.includes(n - 1)) xTicks.push(n - 1);

  const yVals = Array.from({length: GRID + 1}, (_, i) => Math.round(yMax * i / GRID));
  const milestones = getMilestones(counts);
  const th = themeName;

  const o = [];
  o.push(`<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}">`);
  o.push(`<style>
  .ax{font:11px sans-serif;fill:${t.axisText}}
  .ti{font:bold 13px sans-serif;fill:${t.titleText}}
  .ms{font:bold 10px sans-serif;fill:${t.pillText}}
  #ln{stroke-dasharray:${pLen};stroke-dashoffset:${pLen};animation:draw 1.6s cubic-bezier(.4,0,.2,1) forwards}
  @keyframes draw{to{stroke-dashoffset:0}}
</style>`);
  o.push(`<defs>
  <linearGradient id="gr" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0%" stop-color="${t.gradTop}" stop-opacity="0.28"/>
    <stop offset="100%" stop-color="${t.gradBot}" stop-opacity="0"/>
  </linearGradient>
  <clipPath id="cp"><rect x="${PL}" y="${PT}" width="${CW}" height="${CH}"/></clipPath>
</defs>`);

  // Background
  o.push(`<rect width="${W}" height="${H}" rx="12" fill="${t.bg}"/>`);
  o.push(`<rect width="${W}" height="${H}" rx="12" fill="none" stroke="${t.border}" stroke-width="1"/>`);

  // Y gridlines
  for (const v of yVals) {
    const y = py(v).toFixed(1);
    o.push(`<line x1="${PL}" y1="${y}" x2="${PL+CW}" y2="${y}" stroke="${t.grid}" stroke-width="1"/>`);
    o.push(`<text x="${PL-6}" y="${(py(v)+4).toFixed(1)}" text-anchor="end" class="ax">${fmtY(v)}</text>`);
  }

  // X ticks
  for (const i of xTicks) {
    const x = px(i).toFixed(1);
    o.push(`<line x1="${x}" y1="${PT+CH}" x2="${x}" y2="${PT+CH+4}" stroke="${t.axisText}" stroke-width="1"/>`);
    o.push(`<text x="${x}" y="${PT+CH+16}" text-anchor="middle" class="ax">${labels[i]}</text>`);
  }

  // Area + line
  o.push(`<path d="${areaD}" fill="url(#gr)" clip-path="url(#cp)"/>`);
  o.push(`<path id="ln" d="${lineD}" fill="none" stroke="${t.line}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" clip-path="url(#cp)"/>`);

  // Milestones
  for (let idx = 0; idx < milestones.length; idx++) {
    const { i: bi, label } = milestones[idx];
    const mx = px(bi).toFixed(1);
    const my = py(counts[bi]);
    const above = idx % 2 === 0;
    let ly = above ? my - 18 : my + 26;
    ly = Math.max(PT + 14, Math.min(ly, PT + CH - 4));
    const lw = label.length * 7 + 10;
    const lx = (px(bi) - lw / 2).toFixed(1);
    o.push(`<circle cx="${mx}" cy="${my.toFixed(1)}" r="4" fill="${t.dot}" stroke="${t.bg}" stroke-width="1.5"/>`);
    o.push(`<rect x="${lx}" y="${(ly-12).toFixed(1)}" width="${lw}" height="16" rx="8" fill="${t.pillBg}" stroke="${t.pillBorder}" stroke-width="0.8"/>`);
    o.push(`<text x="${mx}" y="${ly.toFixed(1)}" text-anchor="middle" class="ms">${label}</text>`);
  }

  // Watermark
  o.push(`<text x="${W-PR}" y="${PT+14}" text-anchor="end" class="ti">&#9733; ${total.toLocaleString()} stars</text>`);
  o.push('</svg>');
  return o.join('\n');
}

// ── Handler ───────────────────────────────────────────────────────────────────

module.exports = async function handler(req, res) {
  const token = process.env.GITHUB_TOKEN;
  if (!token) {
    res.status(500).setHeader('Content-Type', 'text/plain').send('GITHUB_TOKEN not set');
    return;
  }

  const theme = req.query.theme === 'dark' ? 'dark' : 'light';

  try {
    const history = await fetchAllStargazers(token);
    const svg = generateSVG(history, theme);
    res
      .setHeader('Content-Type', 'image/svg+xml')
      .setHeader('Cache-Control', 'public, max-age=21600, stale-while-revalidate=86400')
      .status(200)
      .send(svg);
  } catch (err) {
    console.error(err);
    res.status(500).setHeader('Content-Type', 'text/plain').send(`Error: ${err.message}`);
  }
};
