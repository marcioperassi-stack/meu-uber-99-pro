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

## Novo teste: print escolhido pelo motorista, sem acesso a outras telas

A variante `demoDebug` inclui agora **reconhecimento de texto local (OCR)** via ML Kit com o modelo latino incluído no APK. O motorista, **com o carro parado**, escolhe um print na galeria ou compartilha uma imagem pelo menu Compartilhar do Android. O app lê os textos na imagem e tenta preencher automaticamente cinco campos de oferta: valor recebido, distância de busca, distância da viagem, tempo de busca e tempo da viagem. **É obrigatório conferir os campos e tocar em Analisar**, pois a leitura de imagens pode errar. Não há aprovação, rejeição ou leitura em tempo real da Uber/99.

O leitor interpreta m e km, aceita linhas como `4 min (825 m)` e `12 min (10,9 km)` e descarta valores acessórios como `R$ 1,65/km` e `R$ 3,00 adicional`. Em caso de valores/etapas ambíguos, não fornece decisão. A imagem é processada no aparelho, não é armazenada e não é enviada para servidores pelo app; a leitura requer ação voluntária do usuário.

Exemplo de print para conferir com veículo parado:

```text
R$ 19,40
R$ 1,65/km
4 min (825 m)
12 min (10,9 km)
R$ 3,00 adicional
```

Se o OCR identificar os dados acima: distância 11,725 km; tempo 16 min; faturamento bruto R$ 72,75/h e cerca de R$ 1,65/km → **CORRIDA RUIM** pelo critério de R$ 1,70/km. O exemplo serve para desenvolvimento e testes, não garante reconhecer todos os formatos de ofertas.

**Permissões:** o fluxo de importação usa apenas o seletor de documentos ou uma imagem explicitamente compartilhada. Não solicita Acessibilidade, captura de tela, leitura de fotos em massa, sobreposição, internet, localização, câmera ou microfone. O build automatizado inspeciona o manifesto final e executa testes unitários; confirmar o resultado é essencial.

**Distribuição:** a assinatura de APK `debug` do GitHub Actions pode variar entre compilações. Se o Android avisar que não é possível atualizar a demonstração anterior por incompatibilidade de assinatura, será necessário desinstalar **somente o app de demonstração** anterior antes de instalar o novo; não remover o site nem seus dados financeiros. Não desligar o Play Protect para instalar.

A função **realmente automática**, com janelinha sobre uma oferta que acabou de aparecer, segue como pesquisa separada. O código experimental de Acessibilidade continua **não distribuído**, dependente de análise de segurança, termos das plataformas e regras do Google Play. Não há promessa de viabilidade no Android.

## Duas versões Android

- `demoDebug`: **versão de demonstração**, com entrada manual **e leitura de print escolhido**, sem permissão de Acessibilidade, sem leitura de outros apps, sem janela por cima da Uber/99. O GitHub Actions compila **somente esta variante**, conferindo o manifesto de permissões.
- `automaticoDebug`: protótipo **não distribuído**, com leitor experimental limitado a ofertas da Uber Driver e 99. A leitura das telas reais não foi validada e pode não reconhecer formatos diferentes. Exige revisão das políticas, segurança, consentimento e testes antes de qualquer instalação ou divulgação.

A versão automática mostra um aviso visual quando consegue reconhecer valor, busca e viagem de uma oferta; não aceita, recusa ou toca em botões do motorista. Não acessa Waze. Não utiliza rede.

## Cuidados de instalação

O primeiro APK automático solicitava Acessibilidade e foi bloqueado pelo Play Protect. **Não desative nem contorne o Play Protect**. O usuário já confirmou que a demonstração sem Acessibilidade instalou e exibiu os avisos verde/vermelho, na lógica anterior. As novas metas brutas precisam ser testadas com o APK recompilado.

Para compilação pelo GitHub, o workflow `.github/workflows/copiloto-android.yml` executa os testes e gera `app-demo-debug.apk`, em *Artifacts* da execução correspondente. O nome atual do artefato é `meu-uber-99-demo-analise-print-sem-acessibilidade`. Não suponha que um build bem-sucedido valida o recurso automático.

## Próximos testes

1. Validar com dados fictícios na demonstração, incluindo os limites de R$ 1,70/km e R$ 40/h.
2. Confirmar que nenhuma regra antiga de custo influencia o resultado.
3. Revisar políticas e permissões antes de testar o leitor experimental em dispositivo apropriado, sem dados de passageiros e sem alterar proteções.
4. Não usar a tela de análise durante a direção.

O parser de ofertas reais ainda tem limitações: casos ambíguos ou sem dados suficientes retornam resultado incompleto, não um veredito inventado.
