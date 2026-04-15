import socket
import sys
import threading

# Configurações por omissão do servidor ⚙️
DEFAULT_HOST = "localhost"
DEFAULT_PORT = 5025

def main():
    # Inicializa as variáveis com os valores por omissão
    host = DEFAULT_HOST
    port = DEFAULT_PORT

    # Verifica se foram passados argumentos via linha de comando (ex: python Jogador.py 127.0.0.1 5025)
    # Em Java: if (args.length == 2) 🖥️
    if len(sys.argv) == 3:
        host = sys.argv[1]
        port = int(sys.argv[2])

    try:
        # Tenta estabelecer a ligação TCP com o servidor
        # O 'with' funciona como o 'try-with-resources' do Java, garantindo que o socket fecha sozinho 🛡️
        with socket.create_connection((host, port)) as sock:
            print(f"🐍 Python -> Ligação estabelecida: {sock.getpeername()}")

            # Criamos "files" a partir do socket para ler e escrever como se fossem texto (Streams)
            # reader equivale ao BufferedReader; writer equivale ao PrintWriter 📝
            reader = sock.makefile('r', encoding='utf-8')
            writer = sock.makefile('w', encoding='utf-8')

            # Definição da tarefa que vai ficar a "ouvir" o servidor em background
            def tarefa_leitura():
                try:
                    # Lê o socket linha a linha (while (line = is.readLine()) != null) 📥
                    for line in reader:
                        linha_limpa = line.strip()
                        if not linha_limpa: # Salta linhas vazias como no teu código Java
                            continue
                        print(linha_limpa)
                except Exception as e:
                    print(f"Ligação cancelada remotamente pelo servidor! {e}", file=sys.stderr)

            # Criamos e iniciamos a Thread de leitura 🧵
            # daemon=True garante que a thread morre se o programa principal fechar
            thread_leitura = threading.Thread(target=tarefa_leitura, daemon=True)
            thread_leitura.start()

            # Loop infinito para ler o que o utilizador escreve e enviar para o servidor
            # Equivale ao for(;;) do teu código Java 🔄
            while True:
                try:
                    # Lê a entrada do utilizador (Scanner.nextLine()) ⌨️
                    mensagem = input()
                    
                    # Envia a mensagem para o servidor com uma quebra de linha (os.println)
                    writer.write(mensagem + "\n")
                    
                    # Força o envio imediato dos dados (true no construtor do PrintWriter) 🚀
                    writer.flush()
                    
                except (EOFError, KeyboardInterrupt):
                    # Permite sair do loop de forma limpa com Ctrl+C
                    print("\nEncerrando o cliente...")
                    break

    except Exception as e:
        # Captura erros de ligação (IOException do Java) ⚠️
        print(f"Erro na ligação: {e}", file=sys.stderr)

if __name__ == "__main__":
    # Ponto de entrada do script 🚩
    main()