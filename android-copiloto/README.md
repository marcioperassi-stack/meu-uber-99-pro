# Meu Uber/99 Pro — protótipo Android

Esta branch preserva o aplicativo web na raiz e mantém o projeto Android em `android-copiloto/`.

## Critério de classificação — faturamento bruto

Uma oferta é **CORRIDA BOA** somente quando atende **simultaneamente**:

- **R$ 1,70 por km ou mais** de valor bruto ofertado;
- **R$ 40,00 por hora ou mais** de valor bruto ofertado.

Distância total = km de busca + km de viagem. Tempo total = minutos de busca + minutos de viagem. O motor calcula valor da oferta ÷ distância total e valor da oferta ÷ (tempo total em horas), **sem deduzir etanol, desgaste, pedágio, manutenção, financiamento ou outros custos**. Se qualquer meta falhar, mostra **CORRIDA RUIM**.

Exemplo com 2 km/5 min de busca e 8 km/25 min de viagem:
- R$ 30,00 → R$ 3,00/km e R$ 60,00/h → **boa**.
- R$ 20,00 → R$ 2,00/km e R$ 40,00/h → **boa**.
- R$ 17,00 → R$ 1,70/km e R$ 34,00/h → **ruim**.
- R$ 15,00 → R$ 1,50/km e R$ 30,00/h → **ruim**.

Não se altera o painel financeiro, os lançamentos e o backup do aplicativo web.

## Duas versões Android

- `demoDebug`: **versão de demonstração**, com entrada manual. Sem permissão de Acessibilidade, sem leitura de outros apps, sem janela por cima da Uber/99. O GitHub Actions compila **somente esta variante**, conferindo o manifesto de permissões.
- `automaticoDebug`: protótipo **não distribuído**, com leitor experimental limitado a ofertas da Uber Driver e 99. A leitura das telas reais não foi validada e pode não reconhecer formatos diferentes. Exige revisão das políticas, segurança, consentimento e testes antes de qualquer instalação ou divulgação.

A versão automática mostra um aviso visual quando consegue reconhecer valor, busca e viagem de uma oferta; não aceita, recusa ou toca em botões do motorista. Não acessa Waze. Não utiliza rede.

## Cuidados de instalação

O primeiro APK automático solicitava Acessibilidade e foi bloqueado pelo Play Protect. **Não desative nem contorne o Play Protect**. O usuário já confirmou que a demonstração sem Acessibilidade instalou e exibiu os avisos verde/vermelho, na lógica anterior. As novas metas brutas precisam ser testadas com o APK recompilado.

Para compilação pelo GitHub, o workflow `.github/workflows/copiloto-android.yml` gera `app-demo-debug.apk`, que pode ser obtido em *Artifacts* da execução correspondente. Não suponha que um build bem-sucedido valida o recurso automático.

## Próximos testes

1. Validar com dados fictícios na demonstração, incluindo os limites de R$ 1,70/km e R$ 40/h.
2. Confirmar que nenhuma regra antiga de custo influencia o resultado.
3. Revisar políticas e permissões antes de testar o leitor experimental em dispositivo apropriado, sem dados de passageiros e sem alterar proteções.
4. Não usar a tela de análise durante a direção.

O parser de ofertas reais ainda tem limitações: casos ambíguos ou sem dados suficientes retornam resultado incompleto, não um veredito inventado.
