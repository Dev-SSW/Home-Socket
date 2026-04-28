import fs from 'fs';
import path from 'path';

// \s : 공백, + : 하나 이상, 공백으로 나누고 문자열을 배열로 바꿈
// filter(Boolean) : 빈 값 제거
const versions = (process.env.VERSIONS || 'v1 v2 v3 v4').split(/\s+/).filter(Boolean);    // 마이그레이션 버전
const suites = (process.env.SUITES || 'read write').split(/\s+/).filter(Boolean);               // 테스트 종류
const resultsRoot = process.env.RESULTS_ROOT || 'tests/results';                                // JSON 폴더 위치
const reportsRoot = process.env.REPORTS_ROOT || 'tests/reports';                              // 보고서 저장 폴더 위치

// JSON 파일 읽기 ( 없으면 null 반환 )
function readJson(filePath) {
  if (!fs.existsSync(filePath)) return null;
  return JSON.parse(fs.readFileSync(filePath, 'utf-8'));
}

// metric 값 꺼내기 ( getValues(summary, 'http_req_duration') 라면, http_req_duration에 관련된 값을 추출함 )
function getValues(summary, metricName) {
  const metric = summary?.metrics?.[metricName];
  if (!metric) return null;
  return metric.values || metric;
}

// API 이름 추출 ( tag로 붙인 이름을 통해 api 별 결과표를 만들기 위함 )
function extractApiFromMetricName(name) {
  const match = name.match(/^http_req_duration\{api:([^}]+)\}$/);
  return match ? match[1] : null;
}

// 숫자 포맷 함수 (3개)
// 숫자로 변환된 값만 남기기 (isFinite)
function num(value) {
  return typeof value === 'number' && Number.isFinite(value) ? value : null;
}
// fmt(37.254) -> 37.25
function fmt(value, digits = 2) {
  const n = num(value);
  return n === null ? '' : n.toFixed(digits);
}
// pct(0.015) -> 1.50%
function pct(value) {
  const n = num(value);
  return n === null ? '' : `${(n * 100).toFixed(2)}%`;
}

// 개선율 계산 (ps95)
function improvement(before, after) {
  const b = num(before);
  const a = num(after);
  if (b === null || a === null || b === 0) return null;
  return ((b - a) / b) * 100;
}

// 판정 (개선, 회귀, 유지)
function verdict(change) {
  if (change === null) return '';
  if (change >= 5) return '개선';
  if (change <= -5) return '회귀';
  return '유지';
}

// 전체 결과 추출
const rows = [];
const apiRows = [];
for (const version of versions) {
  for (const suite of suites) {
    const filePath = path.join(resultsRoot, version, `${suite}-summary.json`);
    const summary = readJson(filePath);
    if (!summary) continue;

    const duration = getValues(summary, 'http_req_duration');           // 전체 평균 응답 시간
    const failed = getValues(summary, 'http_req_failed');                   // 전체 p95
    const checks = getValues(summary, 'checks');                            // 전체 실패율
    const httpReqs = getValues(summary, 'http_reqs');                     // 전체 요청 수
    const iterations = getValues(summary, 'iterations');                    // 전체 iterations 수

    // rows에 넣기
    rows.push({
      suite,
      version,
      api: 'ALL',
      count: httpReqs?.count ?? null,
      rate: httpReqs?.rate ?? null,
      iterations: iterations?.count ?? null,
      avg: duration?.avg ?? null,
      med: duration?.med ?? null,
      min: duration?.min ?? null,
      max: duration?.max ?? null,
      p90: duration?.['p(90)'] ?? null,
      p95: duration?.['p(95)'] ?? null,
      p99: duration?.['p(99)'] ?? null,
      failedRate: failed?.rate ?? null,
      checksRate: checks?.rate ?? null,
    });

    // API별 결과 추출
    for (const [metricName, metric] of Object.entries(summary.metrics || {})) {
      const api = extractApiFromMetricName(metricName);
      if (!api) continue;
      const values = metric.values || metric || {};
      apiRows.push({
        suite,
        version,
        api,
        count: values.count ?? null,
        rate: values.rate ?? null,
        iterations: null,
        avg: values.avg ?? null,
        med: values.med ?? null,
        min: values.min ?? null,
        max: values.max ?? null,
        p90: values['p(90)'] ?? null,
        p95: values['p(95)'] ?? null,
        p99: values['p(99)'] ?? null,
        failedRate: null,
        checksRate: null,
      });
    }
  }
}

fs.mkdirSync(reportsRoot, { recursive: true });

// rows 결과와 api별 결과 합치기
const allRows = [...rows, ...apiRows];
const csvHeader = [
  'suite', 'version', 'api', 'count', 'rate', 'iterations',
  'avg', 'med', 'min', 'max', 'p90', 'p95', 'p99', 'failedRate', 'checksRate',
];

// CSV 헤더 설정
const csv = [
  csvHeader.join(','),
  ...allRows.map((r) => csvHeader.map((key) => r[key] ?? '').join(',')),
].join('\n');
// 파일 저장
fs.writeFileSync(path.join(reportsRoot, 'summary.csv'), csv);

// 마크다운 표 생성
function markdownTable(title, tableRows) {
  let md = `\n## ${title}\n\n`;
  if (tableRows.length === 0) {
    md += '결과 파일이 없습니다.\n';
    return md;
  }

  md += '| Suite | Version | API | Count | RPS | Avg | Med | P90 | P95 | P99 | Max | Failed | Checks |\n';
  md += '|---|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n';
  for (const r of tableRows) {
    md += `| ${r.suite} | ${r.version.toUpperCase()} | ${r.api} | ${r.count ?? ''} | ${fmt(r.rate)} | ${fmt(r.avg)} | ${fmt(r.med)} | ${fmt(r.p90)} | ${fmt(r.p95)} | ${fmt(r.p99)} | ${fmt(r.max)} | ${pct(r.failedRate)} | ${pct(r.checksRate)} |\n`;
  }
  return md;
}

// 비교표 생성
function comparisonTable(title, tableRows) {
  const keys = [...new Set(tableRows.map((r) => `${r.suite}::${r.api}`))].sort();

  let md = `\n## ${title}\n\n`;

  if (keys.length === 0) {
    md += '비교할 결과가 없습니다.\n';
    return md;
  }

  const versionHeaders = versions.map((version) => `${version.toUpperCase()} p95`);
  const stepHeaders = [];

  for (let i = 0; i < versions.length - 1; i++) {
    stepHeaders.push(`${versions[i].toUpperCase()}→${versions[i + 1].toUpperCase()}`);
  }

  const firstVersion = versions[0];
  const lastVersion = versions[versions.length - 1];
  const totalHeader = `${firstVersion.toUpperCase()}→${lastVersion.toUpperCase()}`;

  md += `| Suite | API | ${versionHeaders.join(' | ')} | ${stepHeaders.join(' | ')} | ${totalHeader} | 판정 |\n`;
  md += `|---|---|${versions.map(() => '---:').join('|')}|${stepHeaders.map(() => '---:').join('|')}|---:|---|\n`;

  for (const key of keys) {
    const [suite, api] = key.split('::');

    const byVersion = Object.fromEntries(
      tableRows
        .filter((r) => r.suite === suite && r.api === api)
        .map((r) => [r.version, r])
    );

    const p95Values = versions.map((version) => byVersion[version]?.p95 ?? null);

    const stepChanges = [];
    for (let i = 0; i < p95Values.length - 1; i++) {
      stepChanges.push(improvement(p95Values[i], p95Values[i + 1]));
    }

    const totalChange = improvement(p95Values[0], p95Values[p95Values.length - 1]);

    md += `| ${suite} | ${api} | `;
    md += `${p95Values.map((value) => fmt(value)).join(' | ')} | `;
    md += `${stepChanges.map((change) => `${fmt(change)}%`).join(' | ')} | `;
    md += `${fmt(totalChange)}% | ${verdict(totalChange)} |\n`;
  }

  return md;
}

// 최종 md 생성
let md = '# Performance Migration Report\n';
md += `\n- Generated at: ${new Date().toISOString()}\n`;
md += `- Versions: ${versions.map((v) => v.toUpperCase()).join(', ')}\n`;
md += `- Suites: ${suites.join(', ')}\n`;
md += '\n단위: latency는 ms, 변화율은 p95 기준입니다. 양수는 개선, 음수는 악화입니다.\n';

md += markdownTable('Suite Summary', rows);
md += comparisonTable('Suite p95 Comparison', rows);
md += markdownTable('API Summary', apiRows);
md += comparisonTable('API p95 Comparison', apiRows);

fs.writeFileSync(path.join(reportsRoot, 'summary.md'), md);
console.log(md);
