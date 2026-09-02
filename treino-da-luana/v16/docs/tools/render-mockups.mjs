import path from "node:path";
import { createRequire } from "node:module";
import { readFile, mkdir, writeFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";

const require = createRequire(import.meta.url);
const runtimeModules = process.env.CODEX_PRIMARY_RUNTIME_NODE_MODULES
  || "/opt/codex/runtimes/codex-primary-runtime/dependencies/node/node_modules";
const sharp = require(path.join(runtimeModules, "sharp"));

const toolDir = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(toolDir, "../..");
const outDir = path.resolve(toolDir, "../mockups");
const exercisePhoto = path.join(root, "wear/src/main/assets/exercises/c_2.jpg");
const duoPhotoA = path.join(root, "wear/src/main/assets/exercises/c_0.jpg");
const duoPhotoB = path.join(root, "wear/src/main/assets/exercises/c_1.jpg");
const duoPhotoC = path.join(root, "wear/src/main/assets/exercises/c_3.jpg");

const C = {
  page: "#0b0b0c", bg: "#070707", obsidian: "#101010", card: "#1a1a1c",
  card2: "#242326", line: "#4a423e", white: "#f7f3ef", muted: "#aaa29d",
  orange: "#ff8a3d", orangeDark: "#5a301c", green: "#7bd494", red: "#f17c75"
};

const esc = value => String(value).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
const text = (x, y, value, size, color = C.white, weight = 700, anchor = "middle", spacing = 0) =>
  `<text x="${x}" y="${y}" fill="${color}" text-anchor="${anchor}" font-family="DejaVu Sans,Arial,sans-serif" font-size="${size}" font-weight="${weight}" letter-spacing="${spacing}">${esc(value)}</text>`;
const rect = (x, y, w, h, r = 10, fill = C.card, stroke = "none", sw = 0) =>
  `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${r}" fill="${fill}" stroke="${stroke}" stroke-width="${sw}"/>`;
const circle = (x, y, r, fill, stroke = "none", sw = 0) =>
  `<circle cx="${x}" cy="${y}" r="${r}" fill="${fill}" stroke="${stroke}" stroke-width="${sw}"/>`;

function screenHeader(title, back = true, duplas = true) {
  let out = "";
  if (back) {
    out += rect(48, 30, 40, 32, 16, C.card2, C.line, 1);
    out += text(68, 53, "‹", 26, C.orange, 900);
  }
  out += text(150, 49, title, 13, C.white, 900);
  if (duplas) {
    out += rect(208, 30, 45, 32, 16, C.card2, C.orange, 1);
    out += text(230.5, 50, "DUPLAS", 6.5, C.orange, 900, "middle", .3);
  }
  return out;
}

function fixedNavigation(left = "ANTERIOR", right = "PRÓXIMO") {
  return `${rect(61, 215, 178, 28, 14, C.card2, C.line, 1)}
    ${text(150, 234, `‹  ${left}`, 7.5, C.white, 900, "middle", .35)}
    ${rect(61, 247, 178, 31, 15, C.orange)}
    ${text(150, 268, `${right}  ›`, 8, C.bg, 900, "middle", .35)}`;
}

function progressPills(values) {
  return values.map((v, i) => {
    const x = 93 + i * 39;
    const done = v === "✓";
    return `${circle(x, 161, 16, done ? C.orange : C.card2, done ? C.orange : C.line, 1)}
      ${text(x, 166, v, 11, done ? C.bg : C.white, 900)}`;
  }).join("");
}

function exerciseThumb(photoBase64, y, name, progress, done = false) {
  return `<defs><clipPath id="thumb-${y}"><rect x="49" y="${y}" width="70" height="50" rx="12"/></clipPath></defs>
    <image x="49" y="${y}" width="70" height="50" preserveAspectRatio="xMidYMid slice" clip-path="url(#thumb-${y})" href="data:image/jpeg;base64,${photoBase64}"/>
    <rect x="49" y="${y}" width="70" height="50" rx="12" fill="none" stroke="${done ? C.orange : C.line}" stroke-width="1"/>
    ${text(130, y + 19, name, 8.2, C.white, 900, "start")}
    ${text(130, y + 37, progress, 7.2, done ? C.orange : C.muted, 800, "start")}`;
}

function duoList(photoA, photoB) {
  let out = screenHeader("TREINO C", true, false);
  out += text(150, 75, "DUPLA 1", 10, C.orange, 900, "middle", .8);
  out += rect(39, 82, 222, 140, 20, C.card, C.orange, 1.2);
  out += exerciseThumb(photoA, 91, "Puxada alta", "3 de 3 séries", true);
  out += exerciseThumb(photoB, 158, "Remada baixa", "2 de 3 séries", false);
  out += rect(264, 88, 4, 137, 2, "#353135");
  out += rect(264, 90, 4, 67, 2, C.orange);
  out += rect(61, 231, 178, 43, 18, C.card2, C.line, 1);
  out += text(150, 248, "DUPLA 2", 8.5, C.white, 900);
  out += text(150, 263, "Remada articulada + Rosca", 6.5, C.muted, 700);
  return out;
}

function duoListScrolled(photoA, photoB) {
  let out = screenHeader("TREINO C", true, false);
  out += text(150, 75, "DUPLA 2", 10, C.orange, 900, "middle", .8);
  out += rect(39, 82, 222, 140, 20, C.card, C.line, 1.2);
  out += exerciseThumb(photoA, 91, "Remada articulada", "2 de 3 séries", false);
  out += exerciseThumb(photoB, 158, "Rosca bíceps", "0 de 3 séries", false);
  out += rect(264, 88, 4, 137, 2, "#353135");
  out += rect(264, 133, 4, 67, 2, C.orange);
  out += rect(61, 231, 178, 43, 18, C.card2, C.line, 1);
  out += text(150, 248, "DUPLA 3", 8.5, C.white, 900);
  out += text(150, 263, "Unilateral + Rosca martelo", 6.5, C.muted, 700);
  return out;
}

function exerciseTop(photoBase64) {
  let out = screenHeader("TREINO C • 2A");
  out += text(150, 82, "Remada articulada", 15, C.white, 900);
  out += `<defs><clipPath id="photo"><rect x="47" y="91" width="206" height="102" rx="20"/></clipPath></defs>
    <image x="47" y="91" width="206" height="102" preserveAspectRatio="xMidYMid slice" clip-path="url(#photo)" href="data:image/jpeg;base64,${photoBase64}"/>
    <rect x="47" y="91" width="206" height="102" rx="20" fill="none" stroke="${C.orange}" stroke-width="1.5"/>`;
  out += text(150, 207, "Role para ver carga, séries e conclusão", 7, C.muted, 700);
  out += fixedNavigation();
  return out;
}

function exerciseScrolled() {
  let out = screenHeader("TREINO C • 2A");
  out += rect(36, 74, 110, 52, 16, C.card, C.line, 1);
  out += rect(154, 74, 110, 52, 16, C.card, C.line, 1);
  out += text(91, 91, "REPETIÇÕES", 6.5, C.muted, 900, "middle", .5);
  out += text(91, 116, "8–10", 19, C.orange, 900);
  out += text(209, 91, "CARGA", 6.5, C.muted, 900, "middle", .5);
  out += text(209, 116, "42 kg", 19, C.white, 900);
  out += text(150, 142, "SÉRIES", 8, C.muted, 900, "middle", .8);
  out += progressPills(["✓", "✓", "3"]);
  out += rect(61, 184, 178, 24, 12, C.orangeDark, C.orange, 1);
  out += text(150, 200, "CONCLUIR EXERCÍCIO", 7.5, C.orange, 900, "middle", .55);
  out += fixedNavigation();
  return out;
}

function instructionsModal(photoBase64) {
  let out = exerciseTop(photoBase64);
  out += `<circle cx="150" cy="150" r="150" fill="#000" opacity=".68"/>`;
  out += rect(38, 66, 224, 173, 22, C.card2, C.orange, 1.5);
  out += text(150, 91, "COMO EXECUTAR", 10, C.orange, 900, "middle", .75);
  out += text(150, 113, "Remada articulada", 13, C.white, 900);
  out += text(57, 140, "Apoie o peito e mantenha", 9, C.white, 650, "start");
  out += text(57, 158, "os ombros longe das orelhas.", 9, C.white, 650, "start");
  out += text(57, 184, "Conduza os cotovelos para trás", 9, C.white, 650, "start");
  out += text(57, 202, "e retorne com controle.", 9, C.white, 650, "start");
  out += rect(72, 213, 156, 36, 18, C.orange);
  out += text(150, 236, "FECHAR", 9, C.bg, 900, "middle", .6);
  return out;
}

function summary() {
  let out = screenHeader("TREINO FINALIZADO", false, false);
  out += circle(150, 98, 30, C.orangeDark, C.orange, 4);
  out += text(150, 109, "✓", 30, C.orange, 900);
  out += text(150, 142, "RESUMO DO TREINO", 10, C.white, 900, "middle", .55);
  out += rect(36, 153, 110, 55, 16, C.card, C.line, 1);
  out += rect(154, 153, 110, 55, 16, C.card, C.line, 1);
  out += text(91, 171, "TEMPO", 6.5, C.muted, 900, "middle", .5);
  out += text(91, 197, "00:48", 19, C.white, 900);
  out += text(209, 171, "EXERCÍCIOS", 6.5, C.muted, 900, "middle", .5);
  out += text(209, 197, "7 / 11", 19, C.orange, 900);
  out += text(150, 221, "4 exercícios ficaram pendentes", 7.5, C.muted, 700);
  out += rect(52, 232, 196, 35, 17, C.orange);
  out += text(150, 254, "SINCRONIZAR SAMSUNG HEALTH", 7.2, C.bg, 900, "middle", .35);
  out += text(150, 280, "Início automático em 10 s", 6.5, C.muted, 700);
  return out;
}

function device(content, number, title, subtitle) {
  const screenCx = 450;
  const screenCy = 474;
  const scale = 1.72;
  const screenSize = 300 * scale;
  const screenX = screenCx - screenSize / 2;
  const screenY = screenCy - screenSize / 2;
  return `<svg xmlns="http://www.w3.org/2000/svg" width="900" height="900" viewBox="0 0 900 900">
    <defs>
      <radialGradient id="page" cx="50%" cy="42%"><stop stop-color="#2b1a11"/><stop offset=".5" stop-color="#101011"/><stop offset="1" stop-color="#060607"/></radialGradient>
      <linearGradient id="metal" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#8a8f96"/><stop offset=".24" stop-color="#353940"/><stop offset=".7" stop-color="#111318"/><stop offset="1" stop-color="#666b73"/></linearGradient>
      <clipPath id="round"><circle cx="${screenCx}" cy="${screenCy}" r="258"/></clipPath>
    </defs>
    <rect width="900" height="900" fill="url(#page)"/>
    ${text(58, 55, `MOCKUP ${number}/5`, 15, C.orange, 900, "start", 2)}
    ${text(450, 101, title, 31, C.white, 900)}
    ${text(450, 132, subtitle, 15, C.muted, 600)}
    <rect x="342" y="151" width="216" height="82" rx="41" fill="#282b31"/>
    <rect x="342" y="704" width="216" height="152" rx="52" fill="#24272d"/>
    <rect x="168" y="190" width="564" height="564" rx="204" fill="url(#metal)" stroke="#7e838b" stroke-width="4"/>
    <rect x="720" y="360" width="25" height="92" rx="12" fill="#6b7078"/>
    <rect x="721" y="504" width="18" height="62" rx="9" fill="#50545b"/>
    <g clip-path="url(#round)">
      <circle cx="${screenCx}" cy="${screenCy}" r="258" fill="${C.bg}"/>
      <g transform="translate(${screenX} ${screenY}) scale(${scale})">${content}</g>
    </g>
    <circle cx="${screenCx}" cy="${screenCy}" r="258" fill="none" stroke="#030303" stroke-width="13"/>
    ${text(450, 884, "TREINO DA LUANA · GALAXY WATCH8", 13, C.muted, 800, "middle", 1.4)}
  </svg>`;
}

await mkdir(outDir, { recursive: true });
const photoBase64 = (await readFile(exercisePhoto)).toString("base64");
const duoPhotoABase64 = (await readFile(duoPhotoA)).toString("base64");
const duoPhotoBBase64 = (await readFile(duoPhotoB)).toString("base64");
const duoPhotoCBase64 = (await readFile(duoPhotoC)).toString("base64");
const mockups = [
  ["01-lista-duplas", "LISTA LIVRE DE DUPLAS", "Cards maiores com nome, foto e séries concluídas", duoList(duoPhotoABase64, duoPhotoBBase64)],
  ["02-exercicio-foto", "EXERCÍCIO · VISÃO INICIAL", "Foto maior e controles dentro da área circular", exerciseTop(photoBase64)],
  ["03-exercicio-series", "EXERCÍCIO · APÓS ROLAR", "Carga consultiva e séries realmente interativas", exerciseScrolled()],
  ["04-lista-rolada", "LISTA APÓS ROLAR", "Cada dupla permanece grande e pode ser aberta em qualquer ordem", duoListScrolled(photoBase64, duoPhotoCBase64)],
  ["05-resumo-final", "RESUMO FINAL", "Tempo no fim e sincronização manual com Samsung Health", summary()]
];

for (let i = 0; i < mockups.length; i++) {
  const [name, title, subtitle, content] = mockups[i];
  const svg = device(content, i + 1, title, subtitle);
  await writeFile(path.join(outDir, `${name}.svg`), svg, "utf8");
  await sharp(Buffer.from(svg)).png().toFile(path.join(outDir, `${name}.png`));
}

console.log(outDir);
