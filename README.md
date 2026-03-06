# 🌻 Fazenda do Fernandinho

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/Status-Concluído-success?style=for-the-badge)
![Discipline](https://img.shields.io/badge/Disciplina-Estrutura_de_Dados-blue?style=for-the-badge)

> Um simulador de fazenda completo desenvolvido em Java puro (Swing/AWT), inspirado em *Stardew Valley*. O projeto foca na aplicação prática de **Estruturas de Dados** para o gerenciamento do motor do jogo.

---

## 📖 Sobre o Projeto

**Fazenda do Fernandinho** é um jogo de estratégia e simulação agrícola em visão *top-down*. O jogador assume o papel do Fernandinho, gerenciando recursos, energia, espaço e economia. 

O projeto foi construído do zero sem o uso de engines externas (como Unity ou Godot), utilizando apenas as bibliotecas gráficas nativas do Java para renderização procedural, sistema de *tiles* (matrizes) e controle de estado.

---

## ✨ Funcionalidades e Mecânicas

### 🛠️ Ferramentas & Agricultura
* **Enxada:** Prepara a terra nos blocos de grama.
* **Regador:** Essencial para o crescimento das plantas. Possui um sistema de **cargas de água** (esvazia após 3 usos) e precisa ser reabastecido interagindo com o lago.
* **Foice:** Ferramenta de limpeza para remover plantas mortas, sementes indesejadas e destruir cercas.
* **Plantações:** Sementes de Abóbora, Berries e Árvores. Cada uma com seu tempo de crescimento, valor de mercado e texturas visuais próprias.

### 💰 RPG & Economia
* **Fernandinho Coins (F-Coins):** O sistema monetário do jogo. O jogador guarda suas colheitas no armazém da casa e elas são vendidas automaticamente durante a noite.
* **Sistema de Energia:** Cada ação (arar, regar, limpar) consome energia da barra do jogador. Se a energia chegar a zero, o jogador fica exausto.
* **Hotbar Dinâmica:** Inventário flutuante com suporte a até 8 itens/ferramentas.

### 🌍 Mundo Vivo e Visuais
* **Gráficos Suavizados:** Uso de *Anti-Aliasing* para gráficos vetoriais mais agradáveis.
* **Ciclo Dia/Noite:** Ao dormir, a tela de transição exibe o lucro do dia, a energia é restaurada e o céu altera entre Sol/Nuvens e Lua/Estrelas.
* **Animações:** O personagem possui animações de caminhada e os elementos do cenário (como a água do lago e a fumaça da chaminé) são dinâmicos.

---

## 🧩 Estrutura de Dados Aplicada

O grande diferencial técnico deste projeto é a implementação manual de uma **Tabela Hash (Hash Table)** com tratamento de colisões por encadeamento (*Chaining*).

* **Onde está?** Arquivos `GameHashTable.java` e `Item.java`.
* **Para que serve?** Ela atua como o **Registro de Itens (Item Registry)** do jogo. Em vez de percorrer listas lentas, o jogo busca as propriedades complexas de qualquer item ou semente instantaneamente (complexidade **O(1)**) através de sua chave de texto (ID). Isso garante que o laço de repetição do jogo (*Game Loop* a 60 FPS) não sofra quedas de desempenho ao processar interações.

---

## 🎮 Controles

| Tecla | Ação |
| :---: | :--- |
| **W, A, S, D** | Movimentar o Fernandinho |
| **1 a 8** | Selecionar Ferramenta/Item na Hotbar |
| **ESPAÇO** | Usar item ativo / Interagir / Encher o Regador (no lago) |
| **ENTER** | Dormir (Salvar o progresso, vender colheitas e passar para o próximo dia) |

---

## 🛠️ Como Compilar e Rodar

Certifique-se de ter o **Java JDK 8+** instalado. Recomenda-se rodar pelo terminal para que os arquivos `.class` fiquem separados organizadamente.

1. Clone o repositório:
   ```bash
   git clone [https://github.com/matheusleaomesquitarincon/fazendafernandinho.git](https://github.com/matheusleaomesquitarincon/fazendafernandinho.git)
Entre na pasta principal do projeto:

Bash
cd fazendafernandinho
Compile os códigos da pasta src para a pasta bin:

Bash
javac -d bin src/*.java
Execute o jogo:

Bash
java -cp bin JavaValley
👥 Integrantes do Grupo
Desenvolvido com ☕ e código por:

Matheus Leão Mesquita Rincon
