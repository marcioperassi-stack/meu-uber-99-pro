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
4. Ajuste os custos do seu carro (preço do combustível, km/l e desgaste por km).
5. Ajuste suas metas de **lucro líquido** por quilômetro e por hora.
6. Toque em **Analisar corrida** para ver lucro, custos, lucro por km/hora e a oferta mínima estimada para as duas metas.

Os campos de combustível, consumo, desgaste e metas começam com **valores de exemplo** e são salvos apenas no navegador após uma análise. Os lançamentos financeiros permanecem armazenados separadamente sob a chave `MEU_UBER_99_PRO_V3`, sem migração ou alteração.

### Como o cálculo funciona

- Distância total = km para buscar o passageiro + km da viagem.
- Tempo total = minutos para buscar o passageiro + minutos da viagem.
- Combustível = distância total / consumo (km/l) × preço por litro.
- Desgaste = distância total × custo de manutenção/desgaste por km.
- Custos = combustível + desgaste + extras informados.
- Lucro estimado = oferta ao motorista − custos.
- Lucro por km = lucro estimado / distância total.
- Lucro por hora = lucro estimado / (tempo total / 60).
- Oferta mínima estimada = custos + maior valor entre (meta por km × distância total) e (meta por hora × horas totais).

Uma oferta só aparece como **Dentro das suas metas** quando possui lucro positivo e atende **às duas metas**.

## Limites importantes

O analisador não lê nem aceita corridas automaticamente, não captura dados da Uber ou 99 e não controla o Waze. A simulação só acontece quando você toca no botão. Não inclui automaticamente impostos, tempo parado, deslocamento vazio após a corrida e outras despesas omitidas. Use com o veículo parado. A decisão de aceitar qualquer oferta continua com o motorista.

Este projeto funciona como uma página HTML; não precisa de servidor nem de chaves de API para o uso básico.
