# Hytale Industrial Revolution (HIR) - Era 1: Mecânica & Roda d'Água

Mod de automação e tecnologia para Hytale. 

---

## 1. Novidades da Versão: Roda d'Água (Water Wheel)

- **Bloco Colocável Personalizado:** `HIR_Water_Wheel` (Roda d'Água).
- **Modelo 3D Oficial:** [WaterWheel.blockymodel](file:///c:/Users/agnal/Desktop/mod%20hytalçe/src/main/resources/Common/Blocks/HIR/WaterWheel.blockymodel) com base de suporte em madeira, eixo central e pás giratórias estruturadas.
- **Textura Própria:** [WaterWheel_Texture.png](file:///c:/Users/agnal/Desktop/mod%20hytalçe/src/main/resources/Common/Blocks/HIR/WaterWheel_Texture.png) (madeira trabalhada com eixo reforçado em ferro).
- **Animação Visual Contínua:** [WaterWheel_Spin.blockyanim](file:///c:/Users/agnal/Desktop/mod%20hytalçe/src/main/resources/Common/Blocks/HIR/WaterWheel_Spin.blockyanim) (rotação contínua e suave de 360° em quaternions lineares).
- **Detecção de Água em Tempo Real:** Via `HIRWaterWheelManager`, o mod consulta a grade ECS do mundo e o componente `FluidSection` verificando blocos de água abaixo ou em contato com a máquina.
- **Controle de Rotação Visual:** Integrado à API oficial `BlockAnimationModule`:
  - **Com Água:** Velocidade ajustada para `1.0x` (girando).
  - **Sem Água:** Velocidade ajustada para `0.0x` (parada).

---

## 2. Comandos no Jogo

- `/hir` ou `/hir test`:
  Exibe o status do mod e as máquinas registradas da Era 1.
- `/hir wheel`:
  Varre um raio de 10 blocos ao redor do jogador, detecta todas as Rodas d'Água instaladas, testa a presença de água em tempo real, sincroniza a animação visual e reporta o diagnóstico no chat.

---

## 3. Como Executar e Testar

1. Execute `install-local.bat` (já executado e atualizado).
2. Abra o Hytale e entre no seu mundo criativo.
3. No inventário criativo, procure por **Roda d'Água** (`HIR_Water_Wheel`).
4. Posicione uma Roda d'Água em terra firme (seca) e outra na margem/dentro d'água.
5. Digite no chat:
   ```
   /hir wheel
   ```
6. O mod reportará cada bloco, ativando a rotação na que possui água e mantendo parada a que está seca!
