# 🇵🇹 Dashboard Legislativas 2025

Este projeto consiste num **dashboard interativo** desenvolvido com **Quarto** e **OJS (Observable JavaScript)** para a análise e visualização de dados das Eleições Legislativas de 2025.

A aplicação tira partido da reatividade do OJS para permitir filtragem de dados em tempo real diretamente no browser, sem necessidade de um servidor Python ou R.

## 👥 Identificação dos Autores

* **Nome do Aluno:** Jorge Gonçalves
* **Número de Aluno:** 52345
* **Curso/Disciplina:** LEIM

## 🚀 Instruções de Execução

Este dashboard corre inteiramente no lado do cliente (browser).

### Pré-requisitos
Para visualizar e editar o projeto, apenas necessita de ter instalado:
1.  **Quarto CLI** (Interface de Linha de Comandos do Quarto).
2.  Um editor de texto (ex: VS Code com a extensão Quarto).

### Como executar
1.  Abra o terminal na pasta do projeto.
2.  Execute o seguinte comando para iniciar o servidor local:
    ```bash
    quarto preview dashboard.qmd
    ```
3.  O dashboard abrirá automaticamente no seu browser (geralmente em `http://localhost:xxxx`).


## 🛠️ Tecnologias Utilizadas

* **Framework:** [Quarto](https://quarto.org/) (Formato Dashboard)
* **Linguagem de Script:** [OJS (Observable JavaScript)](https://quarto.org/docs/interactive/ojs/)
* **Bibliotecas de Visualização:**
    * **Plotly.js** (Gráficos interativos e Sunburst)
    * **Observable Inputs** (Para os filtros e seletores)
* **Estrutura de Dados:** Ficheiros CSV/JSON locais carregados via `FileAttachment`.

## 📊 Funcionalidades Implementadas

O dashboard organiza a informação em três abas principais ("Rows") para diferentes níveis de análise:

### 1. Visão Geral (Resumo)
* **KPIs Dinâmicos:** Cartões que mostram o Vencedor Nacional e a Taxa de Abstenção, atualizados conforme os filtros.
* **Resumo:** Visualização rápida dos totais nacionais.

### 2. Mapa
* Visualização geográfica dos resultados eleitorais (Distritos/Concelhos).

### 3. Detalhes (Análise Profunda)
* **Gráfico Sunburst:** Permite explorar a hierarquia dos votos (Nacional > Distrito > Partido).
* **Tabelas Interativas:**
    * *Detalhe Distrito:* Lista filtrada de votos por partido.
    * *Vencedores Nacionais:* Ranking global dos partidos.

### Filtros Globais (Sidebar)
* **Filtro de Distrito:** Permite isolar os dados de um distrito específico (ex: Aveiro, Lisboa).
* **Filtro de Partido:** Foca as visualizações e métricas num partido específico.
