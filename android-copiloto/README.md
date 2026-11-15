# Meu Uber/99 Pro — experimento Android inspirado no conceito de semáforo

Este diretório **não substitui** o site existente. A versão web na raiz permanece como está, com seus lançamentos, histórico e backup. Este é um **protótipo Android independente**, numa branch de desenvolvimento.

## Objetivo

Quando uma oferta da Uber Driver ou da 99 Motorista apresentar valor, quilômetros e minutos em texto acessível, avaliar localmente e mostrar **CORRIDA BOA** (verde) ou **CORRIDA RUIM** (vermelho) em uma faixa sobre a tela. O aviso nunca deve tocar em botões de aceitar/recusar ou controlar o Waze. Inspirado no **conceito público** de semáforo de valores do GigU, **sem copiar código, marca ou interface proprietária**.

## Estado real deste protótipo (14/11/2026)

- [x] Projeto Android em Kotlin criado em pasta separada.
- [x] Tela de autorização com explicação sobre acesso às ofertas.
- [x] Serviço limitado aos identificadores da Uber Driver (`com.ubercab.driver`) e 99 Motorista (`com.app99.driver`).
- [x] Parser **conservador** para textos com rótulos separados `Busca ... km ... min` e `Viagem ... km ... min`; layouts diferentes são ignorados.
- [x] Motor financeiro com metas de R$ 40/h brutos, R$ 25/h de sobra e R$ 1/km de sobra.
- [x] Custos provisórios iguais aos do site: etanol a R$ 3,62/l; 9 km/l; R$ 0,25/km de desgaste; financiamento de R$ 2.252/mês; referência de 300 km × dias no mês.
- [x] Aviso sobreposto via `TYPE_ACCESSIBILITY_OVERLAY`, não interativo, com ocultação automática após 8 segundos.
- [x] Tela de demonstração com dados fictícios.
- [ ] **Não foi compilado nem testado em um aparelho real.** Não há APK validado.
- [ ] Validação da extração dos formatos reais de ofertas da Uber e da 99.
- [ ] Sincronização das preferências editáveis da versão web com a versão Android.
- [ ] Revisão de política do Google Play, testes de acessibilidade e distribuição.

## Dados e privacidade

O usuário deve marcar autorização dentro do app e ativar manualmente o serviço nas configurações de Acessibilidade do Android. O app não solicita INTERNET, não grava dados da oferta, não toca na tela por conta própria, não ativa permissões sozinho e não lê outras aplicações. Não usar o atributo `isAccessibilityTool=true`: este é um auxiliar financeiro para motoristas, **não** uma ferramenta para pessoas com deficiência.

Para publicação no Google Play, documentar o uso da AccessibilityService na ficha do aplicativo e preencher a declaração da API, incluindo divulgação proeminente e consentimento afirmativo no próprio app. Também verificar regras da Uber e 99 antes de disponibilizar comercialmente. Referência: https://support.google.com/googleplay/android-developer/answer/10964491?hl=pt-BR

## Limites da análise

O parser só retorna oferta se conseguir identificar **um único valor monetário e duas seções rotuladas com km e tempo**. Caso contrário, o serviço não exibe veredito. A precisão depende do conteúdo que o Android disponibiliza. Em telas com texto inacessível, layouts diferentes, várias tarifas, corrida com parada, pedágio, bônus e outros casos não previstos, será necessário desenvolver suporte específico e testar cuidadosamente.

A falta de análise **não** significa corrida ruim nem boa. Cálculos provisórios não substituem avaliação financeira completa.

## Como construir e testar

Abrir a pasta `android-copiloto` no Android Studio com SDK Android 35 e JDK 17; sincronizar o Gradle e compilar `:app:assembleDebug`. O repositório ainda não tem Gradle Wrapper e o APK não foi testado. Habilitar o serviço somente em **dispositivo de teste** e com o carro parado, jamais depender dele numa corrida real antes da validação.

### Cenário fictício, 30 dias no mês

- Oferta R$ 30,00; busca 2 km e 5 min; viagem 8 km e 25 min → 10 km, 30 min, R$ 60/h brutos, R$ 20,98 de sobra, R$ 41,95/h de sobra. **Boa**.
- Mesma distância e tempo, oferta R$ 20,00 → R$ 40/h brutos, mas somente R$ 21,95/h de sobra. **Ruim**, embora a meta bruta tenha sido atingida.

Como os meses têm 28 a 31 dias, o rateio real muda com o calendário e esses números são exemplos para mês de 30 dias.

## Próxima validação essencial

Conferir em um Android real se as ofertas da Uber e da 99 expõem valor, distância de busca, distância de viagem e duração de ambas as etapas em nós acessíveis. Sem isso, a leitura automática não pode ser garantida. Não coletar capturas de tela com nomes/endereços de passageiros.
