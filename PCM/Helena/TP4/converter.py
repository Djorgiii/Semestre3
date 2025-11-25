import json

INPUT_FILE = "2025_ar_mapa_resultados (1).json"
OUTPUT_FILE = "elections.json"

print("➡ A abrir ficheiro:", INPUT_FILE)

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    txt = f.read().strip()

print("📏 Tamanho do texto lido:", len(txt))

# O ficheiro é uma sequência de objetos JSON e nulls, não um array.
# Transformamos em array: [ {...}, {...}, null, {...}, ... ]
if not txt.startswith("["):
    txt = "[" + txt.rstrip(",\n\r\t ") + "]"

# Pequena limpeza: garantir que há vírgulas entre objetos }{ → },{
txt = txt.replace("}\n{", "},{").replace("}\r\n{", "},{")

try:
    raw = json.loads(txt)
except Exception as e:
    print("❌ ERRO ao fazer json.loads:")
    print(repr(e))
    raise SystemExit(1)

print("✅ JSON carregado. Número de elementos na lista:", len(raw))

# -------------------------
# 1. Primeiro objeto: mapa id -> nome de círculo
# -------------------------
id_to_name = raw[0]          # {"1": "Aveiro", ...}
circulos = {k: v for k, v in id_to_name.items()}

print("🗺  Círculos eleitorais:", circulos)

# -------------------------
# Função helper para converter valores em inteiro
# -------------------------
def to_int(v):
    if v is None:
        return 0
    if isinstance(v, (int, float)):
        return int(v)
    if isinstance(v, str):
        v = v.strip()
        if v in ("-", "c.r."):
            return 0
        return int(v)
    return 0

# -------------------------
# Aliases de nomes
# -------------------------
ALIASES = {
    "Votos Val. Exp. (VVE)": "VVE",
    "PPD/PSD.CDS-PP": "AD",
    "PPD/PSD.CDS-PP.PPM": "AD",
}

resultado = {}

# -------------------------
# Percorrer todos os objetos
# -------------------------
for idx, obj in enumerate(raw[1:], start=1):
    if obj is None:
        continue

    label = obj.get("Círculo")

    # ignorar rodapés, observações e textos
    if not label:
        continue
    if label.startswith("Observações"):
        continue

    # queremos só linhas com números por círculo:
    # - "Inscritos" (sem Column2)
    # - ou Column2 == "Número"
    has_numbers = "1" in obj
    if not has_numbers:
        continue

    col2 = obj.get("Column2")
    if label != "Inscritos" and col2 != "Número":
        # percentagens, mandatos, etc → ignorar
        continue

    key = ALIASES.get(label, label)
    print(f"➡ A processar linha {idx}: {label}  → chave '{key}'")

    row = {}
    for cid, nome in circulos.items():
        if cid in obj:
            row[nome] = to_int(obj[cid])

    if "Total" in obj:
        row["Total"] = to_int(obj["Total"])

    if key in resultado:
        # somar (caso da AD que junta duas linhas)
        for nome, val in row.items():
            resultado[key][nome] = resultado[key].get(nome, 0) + val
    else:
        resultado[key] = row

# -------------------------
# Guardar JSON final
# -------------------------
with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    json.dump(resultado, f, ensure_ascii=False, indent=2)

print("\n✅ Ficheiro limpo gravado em:", OUTPUT_FILE)
print("🔑 Chaves disponíveis no JSON final:", ", ".join(resultado.keys()))
