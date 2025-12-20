const fs = require('fs');
const Genius = require("genius-lyrics");
const Client = new Genius.Client("E6Hp1DNtAjFGfAoj0q2laNh9FqFkawCGlBmlIoN4U6usKR79B75t2UkuPyqUw8kO");

async function atualizarLetras() {
    try {
        const rawData = fs.readFileSync('data.json');
        let musicas = JSON.parse(rawData);

        console.log("Iniciando a busca de letras...");

        for (let song of musicas) {
            // Só procura se não houver letra ou se houver erro anterior
            if (!song.Lyrics || song.Lyrics === "Letra não encontrada." || song.Lyrics === "Erro na busca.") {
                
                // LIMPEZA: Remove parênteses e traços que baralham o Genius
                let nomeLimpo = song.Música.split('(')[0].split('-')[0].trim();
                console.log(`A procurar: ${nomeLimpo} - ${song.Artista}`);
                
                try {
                    const searches = await Client.songs.search(`${nomeLimpo} ${song.Artista}`);
                    
                    if (searches.length > 0) {
                        const lyrics = await searches[0].lyrics();
                        song.Lyrics = lyrics;
                        console.log("✅ Letra encontrada!");
                    } else {
                        song.Lyrics = "Letra não encontrada.";
                        console.log("❌ Não encontrada.");
                    }
                } catch (error) {
                    console.error(`⚠️ Erro na API para ${song.Música}:`, error.message);
                    song.Lyrics = "Erro na busca.";
                }
            }
        }

        fs.writeFileSync('data.json', JSON.stringify(musicas, null, 4), 'utf-8');
        console.log("✅ data.json atualizado!");

    } catch (err) {
        console.error("Erro fatal:", err);
    }
}

atualizarLetras();