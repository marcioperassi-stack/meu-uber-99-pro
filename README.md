# Meu Uber/99 Pro

App web para controle financeiro de motoristas e análise **manual** de propostas da Uber, 99 e outros aplicativos.

## Analisar corrida — regra atual

A corrida é **✅ CORRIDA BOA** (verde) somente quando atinge **as duas metas ao mesmo tempo**:
- **R$ 1,70/km brutos ou mais**;
- **R$ 40,00/h brutos ou mais**.

Se falhar em qualquer uma, mostra **❌ CORRIDA RUIM** (vermelho).

O usuário informa o valor ofertado ao motorista, a distância e o tempo de busca e a distância e o tempo da viagem. O cálculo considera todo o deslocamento desde a busca até o fim da viagem:

- Km total = km até o passageiro + km com passageiro.
- Minutos totais = minutos até o passageiro + minutos de viagem.
- R$/km brutos = valor ofertado ÷ km total.
- R$/h brutos = valor ofertado ÷ (minutos totais ÷ 60).

**Não se descontam combustível, manutenção, desgaste, financiamento, pedágio ou qualquer outro custo para classificar ofertas.** Essas despesas continuam disponíveis normalmente no painel, nos lançamentos, no histórico e nos relatórios financeiros; **não** entram no analisador de corridas.

Exemplos com 2 km e 5 minutos de busca, mais 8 km e 25 minutos de viagem (10 km e 30 minutos totais):
- Oferta de R$ 30: R$ 3,00/km e R$ 60,00/h → **BOA**.
- Oferta de R$ 20: R$ 2,00/km e R$ 40,00/h → **BOA**.
- Oferta de R$ 15: R$ 1,50/km e R$ 30,00/h → **RUIM**.
- Oferta de R$ 17: R$ 1,70/km e R$ 34,00/h → **RUIM**.

## Recursos financeiros preservados

- **Painel:** ganhos, despesas, combustível, lucro, reserva de 10%, quilômetros e horas trabalhadas.
- **Lançamentos:** registro manual de ganhos e custos.
- **Histórico e relatórios:** consulta por período.
- **Backup:** exportação e restauração dos lançamentos em JSON.

A chave dos lançamentos `MEU_UBER_99_PRO_V3` não é alterada ou migrada. Preferências antigas de combustível/financiamento do analisador podem permanecer armazenadas no navegador, mas não são lidas pelo novo cálculo, nem afetam o veredito.

## Limites

A versão web não lê nem aceita automaticamente ofertas da Uber ou 99, não captura telas e não controla o Waze. Só analisa quando você toca no botão, com o veículo parado. Os valores são **brutos**, não representam lucro após despesas. A decisão sobre qualquer corrida continua com o motorista.

O protótipo Android experimental fica numa branch independente. A demonstração Android não lê outras telas nem requer Acessibilidade.
