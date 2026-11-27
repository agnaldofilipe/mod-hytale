# HIR — Hytale Industrial Revolution (Era 1: Mecânica)

Mod de tecnologia e energia mecânica para Hytale.

---

## 1. O que foi Implementado

### A. Roda d'Água (`HIR_Water_Wheel`)
- **Bloco Colocável:** com modelo 3D próprio (`WaterWheel.blockymodel`) e texturas de madeira rústica com aro de ferro.
- **Detecção de Água:** consulta nativa aos dados de fluidos do Hytale (`FluidSection`).
- **Giro Condicionado:** aciona animação de rotação contínua quando em contato com água e interrompe quando seca.

### B. Eixo Mecânico (`HIR_Mechanical_Shaft`)
- **Bloco Colocável:** com modelo 3D próprio (`MechanicalShaft.blockymodel`) e suporte em pedestal de madeira/ferro com eixo giratório horizontal.
- **Conectável em Cadeia:** conecta-se horizontalmente à Roda d'Água e a outros eixos colocados em sequência (Eixo 1 → Eixo 2 → Eixo 3).
- **Transmissão de Energia:** recebe força cinética da fonte e propaga pela rede.
- **Animação Sincronizada:** gira a 1.0x se a rede estiver energizada e para (0.0x) se a conexão for interrompida ou a roda parar.

### C. Sistema Unificado de Energia (`HIREnergySystem`)
- Gerenciamento baseado em grafos (Busca em Largura / BFS) conectando geradores (fontes cinéticas), transmissores (eixos) e futuros consumidores.
- Cálculo de unidades de estresse/potência mecânica (`16 SU` por roda d'água ativa).
- Sincronização em tempo real via `BlockAnimationModule` do servidor.

---

## 2. O que Realmente Funciona vs O que é Visual

1. **O que Realmente Funciona (Lógica e Mecânica):**
   - Detecção real de blocos de água no mundo via ECS do Hytale (`FluidSection`).
   - Mapeamento topológico de conexão: a rede identifica se o eixo está realmente encostado na Roda d'Água ou em outro eixo.
   - Interrupção de circuito: se você quebrar um eixo no meio da linha ou secar a água da roda, a rede detecta a interrupção e para a rotação de toda a linha restante.
   - Cálculo e distribuição de Stress Units (SU).
   - Comandos interativos de inspeção no chat.

2. **O que é Visual:**
   - A rotação do eixo e da roda é controlada via interpolação de velocidade no `BlockAnimationModule`.
   - A aparência de encaixe é obtida pelo alinhamento das pontas do eixo que atravessam exatamente as bordas do bloco (32 unidades).

---

## 3. Como Testar no Jogo

1. Inicie o Hytale e entre no seu mundo criativo.
2. Abra o inventário criativo e pegue:
   - **Roda d'Água** (`HIR_Water_Wheel`)
   - **Eixo Mecânico** (`HIR_Mechanical_Shaft`)
   - Balde d'água (caso queira colocar água manualmente).
3. **Teste 1: Sistema Energizado:**
   - Coloque a **Roda d'Água** tocando na água (ou coloque água embaixo dela).
   - Coloque **Eixos Mecânicos** encostados lateralmente na roda, formando uma linha reta (ex: 3 eixos em sequência).
   - Digite no chat:
     ```text
     /hir network
     ```
   - O chat reportará:
     ```text
     [HIR] REDE DE ENERGIA MECANICA (ERA 1)
       Status Geral: ENERGIZADA (Em Movimento)
       Potencia Gerada: 16 SU (Stress Units)
       Velocidade de Rotacao: 1.0x
       Rodas d'Agua com Agua: 1
       Eixos Mecanicos Conectados: 3
       Componentes na Rede:
         -> [Fonte (Roda)] em (X, Y, Z) | GIRANDO
         -> [Transmissor (Eixo)] em (X+1, Y, Z) | GIRANDO
         -> [Transmissor (Eixo)] em (X+2, Y, Z) | GIRANDO
         -> [Transmissor (Eixo)] em (X+3, Y, Z) | GIRANDO
     ```
4. **Teste 2: Interrupção da Conexão:**
   - Quebre o primeiro eixo (logo após a roda).
   - Digite `/hir network` novamente perto dos outros eixos.
   - Eles constatarão a desconexão da fonte de água e pararão de girar!
