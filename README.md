# Meu Uber/99 Pro

Aplicativo web responsivo para controle financeiro de motoristas e análise **manual** de propostas de corridas.

## Recursos

- **Painel:** ganhos, despesas, combustível, lucro, reserva de 10%, quilômetros e horas trabalhadas.
- **Lançamentos:** registro manual de ganhos e custos.
- **Histórico e relatórios:** consulta por período.
- **Backup:** exportação e restauração em JSON.
- **Analisar corrida:** simulação do resultado financeiro de uma oferta da Uber, 99 ou outro aplicativo.

## Como testar o analisador

1. Abra o aplicativo e entre em **Analisar corrida**.
2. Informe o valor ofertado ao motorista, quilômetros e minutos para buscar o passageiro e realizar a corrida.
3. Informe pedágios ou despesas extras não reembolsadas.
4. Para o **Sandero 2021 1.0 no etanol**, toque em **Usar referência do Sandero (9,0 km/l)** e informe o preço real do litro pago no posto. O botão não altera os seus lançamentos financeiros.
5. Ajuste os outros custos do seu carro (desgaste por km) e, se souber, substitua o consumo de referência pelo consumo real medido.
6. Confira a meta de **R$ 40,00/h de faturamento bruto** (editável), além das duas metas de sobra após os custos.
7. Toque em **Analisar corrida** para ver o faturamento por hora, sobra por hora após os custos, e a oferta mínima para alcançar as três metas.

O consumo inicial de etanol é **9,0 km/l** (referência urbana do Sandero Zen 2021 1.0) e deve ser ajustado ao consumo real quando possível. O preço do combustível é preenchido pelo motorista; os demais custos e metas são **valores de exemplo** e são salvos apenas no navegador após uma análise. Os lançamentos financeiros permanecem armazenados separadamente sob a chave `MEU_UBER_99_PRO_V3`, sem migração ou alteração.

### Como o cálculo funciona

- Distância total = km para buscar o passageiro + km da viagem.
- Tempo total = minutos para buscar o passageiro + minutos da viagem.
- Combustível = distância total / consumo (km/l) × preço por litro.
- Desgaste = distância total × custo de manutenção/desgaste por km.
- Custos = combustível + desgaste + extras informados.
- Lucro estimado = oferta ao motorista − custos.
- Lucro por km = lucro estimado / distância total.
- Lucro por hora = lucro estimado / (tempo total / 60).
- Oferta mínima estimada = maior entre (faturamento mínimo por hora × horas totais) e [custos + maior valor entre (meta por km × distância total) e (meta de sobra por hora × horas totais)].

O analisador exibe um aviso grande em **verde: ✅ CORRIDA BOA**, somente quando há sobra positiva, o faturamento bruto alcança ao menos **R$ 40,00/h** e ambas as metas de sobra por km e por hora são atingidas. Caso não alcance qualquer uma dessas metas, mostra **vermelho: ❌ CORRIDA RUIM**. Quando falta previsão de km mensais para distribuir um financiamento existente, não aprova nem rejeita: aparece **amarelo: ⚠️ ANÁLISE INCOMPLETA**. O resultado é indicativo, não uma aceitação ou recusa automática de ofertas.

## Financiamento do Sandero

A parcela mensal informada pelo motorista é **R$ 2.252,00**. Ela aparece como compromisso fixo de caixa no analisador e é ajustável. Para distribuí-la por corrida, informe a **previsão de quilômetros de trabalho no mês**.

O motorista informou uma faixa de **250 a 350 km por dia** e que trabalha **todos os dias**. Ao abrir o app, o analisador usa inicialmente a quantidade de dias do **mês atual**, inclusive em meses de 28, 29, 30 ou 31 dias, e calcula um cenário entre os extremos, usando **300 km/dia** como média. Exemplo: em um mês de 30 dias, projeta 7.500 a 10.500 km (média de 9.000 km), ou R$ 0,25/km aproximadamente para reservar a parcela. Na virada do mês, atualiza a referência; durante o mesmo mês respeita o ajuste manual do motorista. É uma previsão, não uma garantia de que todos os dias serão trabalhados.

- Rateio da parcela na oferta = (parcela mensal / quilômetros mensais previstos) × quilômetros totais da corrida, incluindo deslocamento até o passageiro.
- Sobra de caixa estimada após rateio = valor ofertado − custos variáveis da corrida − rateio da parcela.
- Oferta mínima para as três metas = maior entre (meta bruta por hora × horas totais) e [custos variáveis + rateio da parcela + maior valor entre (meta por km × km totais) e (meta líquida por hora × horas totais)].
- Com financiamento e sem a previsão de km mensais, o analisador exibe o resultado *antes da parcela* e **não** classifica a oferta como dentro das metas.
- O rateio é uma ferramenta de planejamento do dinheiro necessário para pagar a parcela; não é despesa operacional ou custo contábil equivalente. Uma prestação pode conter principal e juros.
- A parcela não é lançada automaticamente no histórico financeiro para evitar duplicidade se o motorista já registrá-la em outra categoria.

## Limites importantes

O analisador não lê nem aceita corridas automaticamente, não captura dados da Uber ou 99 e não controla o Waze. A simulação só acontece quando você toca no botão. Não inclui automaticamente impostos, tempo parado, deslocamento vazio após a corrida e outras despesas omitidas. Use com o veículo parado. A decisão de aceitar qualquer oferta continua com o motorista.

Este projeto funciona como uma página HTML; não precisa de servidor nem de chaves de API para o uso básico.

## Fonte do consumo de referência

Inmetro, PBE Veicular 2021 (atualizado em 01/02/2022): Sandero Life 1.0 — 9,1 km/l na cidade e 9,7 km/l na estrada com etanol; Sandero Zen 1.0 — 9,0 km/l na cidade e 9,6 km/l na estrada. Valores laboratoriais ajustados; a experiência real pode ser diferente. Fonte: https://www.gov.br/inmetro/pt-br/assuntos/regulamentacao/avaliacao-da-conformidade/programa-brasileiro-de-etiquetagem/tabelas-de-eficiencia-energetica/veiculos-automotivos-pbe-veicular/veiculos-leves-2021/@@download/file
